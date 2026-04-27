package com.example.videocompress.config;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "video")
public class VideoCompressProperties {

    private String ffmpegPath = "./ffmpeg/ffmpeg.exe";
    private String ffprobePath = "./ffmpeg/ffprobe.exe";
    private String inputDir = "./data/input";
    private String outputDir = "./data/output";
    private String backupDir = "./data/backup";
    private String tempDir = "./data/temp";
    private String logDir = "./data/logs";
    private List<String> allowedExtensions = new ArrayList<>(List.of("mp4", "mov", "m4v", "mkv", "avi", "3gp"));
    private long defaultMinSizeMb = 100;
    private int maxParallelTasks = 1;

    public Path ffmpegPath() {
        return Path.of(ffmpegPath).toAbsolutePath().normalize();
    }

    public Path ffprobePath() {
        return Path.of(ffprobePath).toAbsolutePath().normalize();
    }

    public Path inputDir() {
        return Path.of(inputDir).toAbsolutePath().normalize();
    }

    public Path outputDir() {
        return Path.of(outputDir).toAbsolutePath().normalize();
    }

    public Path backupDir() {
        return Path.of(backupDir).toAbsolutePath().normalize();
    }

    public Path tempDir() {
        return Path.of(tempDir).toAbsolutePath().normalize();
    }

    public Path logDir() {
        return Path.of(logDir).toAbsolutePath().normalize();
    }

    public String getFfmpegPath() {
        return ffmpegPath;
    }

    public void setFfmpegPath(String ffmpegPath) {
        this.ffmpegPath = ffmpegPath;
    }

    public String getFfprobePath() {
        return ffprobePath;
    }

    public void setFfprobePath(String ffprobePath) {
        this.ffprobePath = ffprobePath;
    }

    public String getInputDir() {
        return inputDir;
    }

    public void setInputDir(String inputDir) {
        this.inputDir = inputDir;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getBackupDir() {
        return backupDir;
    }

    public void setBackupDir(String backupDir) {
        this.backupDir = backupDir;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public String getLogDir() {
        return logDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public long getDefaultMinSizeMb() {
        return defaultMinSizeMb;
    }

    public void setDefaultMinSizeMb(long defaultMinSizeMb) {
        this.defaultMinSizeMb = defaultMinSizeMb;
    }

    public int getMaxParallelTasks() {
        return maxParallelTasks;
    }

    public void setMaxParallelTasks(int maxParallelTasks) {
        this.maxParallelTasks = maxParallelTasks;
    }
}
