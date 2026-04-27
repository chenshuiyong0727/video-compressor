package com.example.videocompress.service;

import com.example.videocompress.config.VideoCompressProperties;
import com.example.videocompress.model.VideoFileInfo;
import com.example.videocompress.util.PathSafeUtils;
import com.example.videocompress.util.VideoNameUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FileManageService {

    private final VideoCompressProperties properties;
    private final VideoScanService videoScanService;

    public FileManageService(VideoCompressProperties properties, VideoScanService videoScanService) {
        this.properties = properties;
        this.videoScanService = videoScanService;
    }

    public List<String> moveToBackup(List<String> videoIds) throws IOException {
        List<String> movedFiles = new ArrayList<>();
        for (String videoId : videoIds) {
            VideoFileInfo video = videoScanService.findById(videoId)
                    .orElseThrow(() -> new IllegalArgumentException("未找到视频，请先重新扫描: " + videoId));
            Path source = PathSafeUtils.ensureInside(properties.inputDir(), Path.of(video.getAbsolutePath()));
            Path target = PathSafeUtils.resolveInside(properties.backupDir(), video.getRelativePath());
            Files.createDirectories(target.getParent());
            Path uniqueTarget = VideoNameUtils.uniqueSibling(target);
            Files.move(source, uniqueTarget, StandardCopyOption.ATOMIC_MOVE);
            movedFiles.add(uniqueTarget.toString());
        }
        return movedFiles;
    }
}
