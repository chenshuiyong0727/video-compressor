package com.example.videocompress.service;

import com.example.videocompress.config.VideoCompressProperties;
import com.example.videocompress.model.SystemDirs;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Service;

@Service
public class DirectoryService {

    private static final Path DIR_CONFIG_PATH = Path.of("./data/config/video-dirs.json")
            .toAbsolutePath()
            .normalize();

    private final VideoCompressProperties properties;
    private final ObjectMapper objectMapper;

    public DirectoryService(VideoCompressProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() throws IOException {
        loadSavedDirs();
        ensureDataDirs();
    }

    public void ensureDataDirs() throws IOException {
        Files.createDirectories(properties.inputDir());
        Files.createDirectories(properties.outputDir());
        Files.createDirectories(properties.backupDir());
        Files.createDirectories(properties.tempDir());
        Files.createDirectories(properties.logDir());
    }

    public synchronized SystemDirs dirs() {
        SystemDirs dirs = new SystemDirs();
        dirs.setInputDir(properties.getInputDir());
        dirs.setOutputDir(properties.getOutputDir());
        dirs.setBackupDir(properties.getBackupDir());
        dirs.setTempDir(properties.getTempDir());
        dirs.setLogDir(properties.getLogDir());
        return dirs;
    }

    public synchronized SystemDirs updateDirs(SystemDirs dirs) throws IOException {
        properties.setInputDir(normalizeDir(dirs.getInputDir(), "input 目录不能为空"));
        properties.setOutputDir(normalizeDir(dirs.getOutputDir(), "output 目录不能为空"));
        properties.setBackupDir(normalizeDir(dirs.getBackupDir(), "backup 目录不能为空"));
        properties.setTempDir(normalizeDir(dirs.getTempDir(), "temp 目录不能为空"));
        properties.setLogDir(normalizeDir(dirs.getLogDir(), "logs 目录不能为空"));
        ensureDataDirs();
        saveDirs();
        return dirs();
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

    private void loadSavedDirs() throws IOException {
        if (!Files.isRegularFile(DIR_CONFIG_PATH)) {
            return;
        }
        try {
            SystemDirs dirs = objectMapper.readValue(DIR_CONFIG_PATH.toFile(), SystemDirs.class);
            properties.setInputDir(normalizeOptional(dirs.getInputDir(), properties.getInputDir()));
            properties.setOutputDir(normalizeOptional(dirs.getOutputDir(), properties.getOutputDir()));
            properties.setBackupDir(normalizeOptional(dirs.getBackupDir(), properties.getBackupDir()));
            properties.setTempDir(normalizeOptional(dirs.getTempDir(), properties.getTempDir()));
            properties.setLogDir(normalizeOptional(dirs.getLogDir(), properties.getLogDir()));
        } catch (Exception ex) {
            throw new IOException("读取目录配置失败: " + DIR_CONFIG_PATH, ex);
        }
    }

    private void saveDirs() throws IOException {
        Files.createDirectories(DIR_CONFIG_PATH.getParent());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(DIR_CONFIG_PATH.toFile(), dirs());
    }

    private String normalizeDir(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeOptional(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
