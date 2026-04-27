package com.example.videocompress.service;

import com.example.videocompress.config.VideoCompressProperties;
import com.example.videocompress.model.SystemCheckResult;
import com.example.videocompress.util.ProcessUtils;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SystemService {

    private final VideoCompressProperties properties;
    private final DirectoryService directoryService;

    public SystemService(VideoCompressProperties properties, DirectoryService directoryService) {
        this.properties = properties;
        this.directoryService = directoryService;
    }

    public SystemCheckResult check() {
        SystemCheckResult result = new SystemCheckResult();
        boolean ffmpegExists = Files.isRegularFile(properties.ffmpegPath());
        boolean ffprobeExists = Files.isRegularFile(properties.ffprobePath());
        result.setFfmpegExists(ffmpegExists);
        result.setFfprobeExists(ffprobeExists);
        result.setFfmpegPath(properties.getFfmpegPath());
        result.setFfprobePath(properties.getFfprobePath());
        result.setInputDirExists(Files.isDirectory(properties.inputDir()));
        result.setOutputDirExists(Files.isDirectory(properties.outputDir()));
        result.setBackupDirExists(Files.isDirectory(properties.backupDir()));
        result.setFfmpegVersion(ffmpegExists
                ? ProcessUtils.firstLine(List.of(properties.ffmpegPath().toString(), "-version"), 5)
                : "");
        result.setFfprobeVersion(ffprobeExists
                ? ProcessUtils.firstLine(List.of(properties.ffprobePath().toString(), "-version"), 5)
                : "");
        boolean ok = ffmpegExists
                && ffprobeExists
                && Boolean.TRUE.equals(result.getInputDirExists())
                && Boolean.TRUE.equals(result.getOutputDirExists())
                && Boolean.TRUE.equals(result.getBackupDirExists());
        result.setMessage(ok ? "系统环境正常" : "请检查 ffmpeg/ffprobe 文件和 data 目录");
        return result;
    }

    public Map<String, String> dirs() {
        return directoryService.dirs();
    }
}
