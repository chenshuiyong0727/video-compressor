package com.example.videocompress.service;

import com.example.videocompress.config.VideoCompressProperties;
import com.example.videocompress.model.VideoFileInfo;
import com.example.videocompress.model.VideoMetadata;
import com.example.videocompress.util.DateTimeUtils;
import com.example.videocompress.util.FileSizeUtils;
import com.example.videocompress.util.PathSafeUtils;
import com.example.videocompress.util.VideoNameUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class VideoScanService {

    private final VideoCompressProperties properties;
    private final DirectoryService directoryService;
    private final FfprobeService ffprobeService;
    private final Map<String, VideoFileInfo> videoRegistry = new ConcurrentHashMap<>();

    public VideoScanService(
            VideoCompressProperties properties,
            DirectoryService directoryService,
            FfprobeService ffprobeService
    ) {
        this.properties = properties;
        this.directoryService = directoryService;
        this.ffprobeService = ffprobeService;
    }

    public List<VideoFileInfo> scan(Long minSizeMb) throws IOException {
        directoryService.ensureDataDirs();
        long minBytes = Math.max(0, minSizeMb == null ? properties.getDefaultMinSizeMb() : minSizeMb) * 1024L * 1024L;
        Path inputDir = properties.inputDir();
        videoRegistry.clear();
        try (Stream<Path> stream = Files.walk(inputDir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(this::isAllowedVideo)
                    .filter(path -> safeSize(path) >= minBytes)
                    .map(this::toVideoFileInfo)
                    .sorted(Comparator.comparing(VideoFileInfo::getSizeBytes, Comparator.nullsLast(Long::compareTo)).reversed())
                    .peek(info -> videoRegistry.put(info.getId(), info))
                    .toList();
        }
    }

    public Optional<VideoFileInfo> findById(String id) {
        VideoFileInfo info = videoRegistry.get(id);
        if (info == null) {
            return Optional.empty();
        }
        Path source = PathSafeUtils.ensureInside(properties.inputDir(), Path.of(info.getAbsolutePath()));
        if (!Files.isRegularFile(source)) {
            return Optional.empty();
        }
        return Optional.of(info);
    }

    private VideoFileInfo toVideoFileInfo(Path path) {
        Path safePath = PathSafeUtils.ensureInside(properties.inputDir(), path);
        String relativePath = properties.inputDir().relativize(safePath).toString().replace('\\', '/');
        long size = safeSize(safePath);
        long lastModified = safeLastModified(safePath);
        VideoMetadata metadata = new VideoMetadata();
        try {
            metadata = ffprobeService.probe(safePath);
        } catch (Exception ignored) {
            metadata.setCodec("未知");
        }

        VideoFileInfo info = new VideoFileInfo();
        info.setId(VideoNameUtils.videoId(relativePath, size, lastModified));
        info.setFileName(safePath.getFileName().toString());
        info.setAbsolutePath(safePath.toString());
        info.setRelativePath(relativePath);
        info.setSizeBytes(size);
        info.setSizeText(FileSizeUtils.format(size));
        info.setLastModified(lastModified);
        info.setDurationSeconds(metadata.getDurationSeconds());
        info.setDurationText(DateTimeUtils.formatDuration(metadata.getDurationSeconds()));
        info.setWidth(metadata.getWidth());
        info.setHeight(metadata.getHeight());
        info.setResolutionText(metadata.getWidth() == null || metadata.getHeight() == null
                ? "-"
                : metadata.getWidth() + "x" + metadata.getHeight());
        info.setCodec(metadata.getCodec() == null ? "未知" : metadata.getCodec());
        info.setFormat(extension(safePath.getFileName().toString()));
        info.setCompressedExists(hasCompressedOutput(safePath.getFileName().toString()));
        return info;
    }

    private boolean hasCompressedOutput(String fileName) {
        String baseName = VideoNameUtils.baseName(fileName);
        String extension = VideoNameUtils.extension(fileName);
        try (Stream<Path> files = Files.list(properties.outputDir())) {
            return files.anyMatch(path -> path.getFileName().toString().startsWith(baseName + "_compressed")
                    && path.getFileName().toString().endsWith(extension));
        } catch (IOException ex) {
            return false;
        }
    }

    private boolean isAllowedVideo(Path path) {
        String extension = extension(path.getFileName().toString());
        return properties.getAllowedExtensions().stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(extension::equals);
    }

    private String extension(String fileName) {
        String extension = VideoNameUtils.extension(fileName);
        if (extension.startsWith(".")) {
            extension = extension.substring(1);
        }
        return extension.toLowerCase(Locale.ROOT);
    }

    private long safeSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException ex) {
            return 0L;
        }
    }

    private long safeLastModified(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ex) {
            return 0L;
        }
    }
}
