package com.example.videocompress.service;

import com.example.videocompress.config.VideoCompressProperties;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DirectoryService {

    private final VideoCompressProperties properties;

    public DirectoryService(VideoCompressProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() throws IOException {
        ensureDataDirs();
    }

    public void ensureDataDirs() throws IOException {
        Files.createDirectories(properties.inputDir());
        Files.createDirectories(properties.outputDir());
        Files.createDirectories(properties.backupDir());
        Files.createDirectories(properties.tempDir());
        Files.createDirectories(properties.logDir());
    }

    public Map<String, String> dirs() {
        return Map.of(
                "inputDir", properties.getInputDir(),
                "outputDir", properties.getOutputDir(),
                "backupDir", properties.getBackupDir(),
                "tempDir", properties.getTempDir(),
                "logDir", properties.getLogDir()
        );
    }

    public Path inputDir() {
        return properties.inputDir();
    }

    public Path outputDir() {
        return properties.outputDir();
    }

    public Path backupDir() {
        return properties.backupDir();
    }
}
