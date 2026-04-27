package com.example.videocompress.service;

import com.example.videocompress.config.VideoCompressProperties;
import com.example.videocompress.model.CompressTask;
import com.example.videocompress.model.VideoFileInfo;
import com.example.videocompress.model.VideoMetadata;
import com.example.videocompress.util.FileSizeUtils;
import com.example.videocompress.util.PathSafeUtils;
import com.example.videocompress.util.ProcessUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class FfmpegService {

    private final VideoCompressProperties properties;
    private final FfprobeService ffprobeService;

    public FfmpegService(VideoCompressProperties properties, FfprobeService ffprobeService) {
        this.properties = properties;
        this.ffprobeService = ffprobeService;
    }

    public void compress(CompressTask task, VideoFileInfo video) throws Exception {
        Path source = PathSafeUtils.ensureInside(properties.inputDir(), Path.of(video.getAbsolutePath()));
        Path output = PathSafeUtils.ensureInside(properties.outputDir(), Path.of(task.getOutputPath()));
        if (!Files.isRegularFile(properties.ffmpegPath())) {
            throw new IOException("未找到 ffmpeg.exe: " + properties.getFfmpegPath());
        }
        Files.createDirectories(output.getParent());

        List<String> command = CompressCommandFactory.buildCommand(
                properties.ffmpegPath().toString(),
                source.toString(),
                output.toString(),
                task.getProfile()
        );
        Process process = new ProcessBuilder(command).start();
        StringBuilder errorOutput = new StringBuilder();
        ProcessUtils.readAsync(process.getErrorStream(), line -> {
            synchronized (errorOutput) {
                errorOutput.append(line).append(System.lineSeparator());
            }
        });
        ProcessUtils.readAsync(process.getInputStream(), line -> updateProgress(task, line, video.getDurationSeconds()));

        boolean finished = process.waitFor(14, TimeUnit.DAYS);
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("压缩进程超时");
        }
        if (process.exitValue() != 0) {
            throw new IOException("FFmpeg 压缩失败: " + errorOutput);
        }
        validateOutput(task, source, output);
        task.setProgress(100);
        task.setEndTime(LocalDateTime.now());
    }

    private void updateProgress(CompressTask task, String line, Double durationSeconds) {
        if (durationSeconds == null || durationSeconds <= 0 || line == null) {
            return;
        }
        String value = null;
        if (line.startsWith("out_time_ms=")) {
            value = line.substring("out_time_ms=".length());
        } else if (line.startsWith("out_time_us=")) {
            value = line.substring("out_time_us=".length());
        }
        if (value == null) {
            return;
        }
        try {
            double currentSeconds = Long.parseLong(value.trim()) / 1_000_000.0;
            int percent = (int) Math.floor(Math.min(99, Math.max(0, currentSeconds / durationSeconds * 100)));
            task.setProgress(Math.max(task.getProgress(), percent));
        } catch (NumberFormatException ignored) {
        }
    }

    private void validateOutput(CompressTask task, Path source, Path output) throws Exception {
        if (!Files.isRegularFile(output)) {
            throw new IOException("压缩输出文件不存在");
        }
        long outputSize = Files.size(output);
        if (outputSize <= 0) {
            throw new IOException("压缩输出文件大小为 0");
        }
        VideoMetadata metadata = ffprobeService.probe(output);
        if (metadata.getDurationSeconds() == null || metadata.getDurationSeconds() <= 0) {
            throw new IOException("压缩输出文件无法被 ffprobe 正常识别");
        }
        long sourceSize = Files.size(source);
        task.setOutputSizeBytes(outputSize);
        task.setOutputSizeText(FileSizeUtils.format(outputSize));
        task.setSavePercent((sourceSize - outputSize) * 100.0 / sourceSize);
        if (outputSize >= sourceSize) {
            throw new IOException("压缩后文件比源文件更大，不建议使用");
        }
    }
}
