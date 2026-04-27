package com.example.videocompress.model;

public class SystemCheckResult {

    private Boolean ffmpegExists;
    private Boolean ffprobeExists;
    private String ffmpegPath;
    private String ffprobePath;
    private String ffmpegVersion;
    private String ffprobeVersion;
    private Boolean inputDirExists;
    private Boolean outputDirExists;
    private Boolean backupDirExists;
    private String message;

    public Boolean getFfmpegExists() {
        return ffmpegExists;
    }

    public void setFfmpegExists(Boolean ffmpegExists) {
        this.ffmpegExists = ffmpegExists;
    }

    public Boolean getFfprobeExists() {
        return ffprobeExists;
    }

    public void setFfprobeExists(Boolean ffprobeExists) {
        this.ffprobeExists = ffprobeExists;
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

    public String getFfmpegVersion() {
        return ffmpegVersion;
    }

    public void setFfmpegVersion(String ffmpegVersion) {
        this.ffmpegVersion = ffmpegVersion;
    }

    public String getFfprobeVersion() {
        return ffprobeVersion;
    }

    public void setFfprobeVersion(String ffprobeVersion) {
        this.ffprobeVersion = ffprobeVersion;
    }

    public Boolean getInputDirExists() {
        return inputDirExists;
    }

    public void setInputDirExists(Boolean inputDirExists) {
        this.inputDirExists = inputDirExists;
    }

    public Boolean getOutputDirExists() {
        return outputDirExists;
    }

    public void setOutputDirExists(Boolean outputDirExists) {
        this.outputDirExists = outputDirExists;
    }

    public Boolean getBackupDirExists() {
        return backupDirExists;
    }

    public void setBackupDirExists(Boolean backupDirExists) {
        this.backupDirExists = backupDirExists;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
