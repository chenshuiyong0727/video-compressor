package com.example.videocompress.model;

import jakarta.validation.constraints.NotBlank;

public class SystemDirs {

    @NotBlank(message = "input 目录不能为空")
    private String inputDir;

    @NotBlank(message = "output 目录不能为空")
    private String outputDir;

    @NotBlank(message = "backup 目录不能为空")
    private String backupDir;

    @NotBlank(message = "temp 目录不能为空")
    private String tempDir;

    @NotBlank(message = "logs 目录不能为空")
    private String logDir;

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
}
