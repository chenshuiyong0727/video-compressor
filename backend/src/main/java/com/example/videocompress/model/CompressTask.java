package com.example.videocompress.model;

import java.time.LocalDateTime;

public class CompressTask {

    private String taskId;
    private String batchId;
    private String videoId;
    private String sourcePath;
    private String outputPath;
    private String fileName;
    private Long sourceSizeBytes;
    private Long outputSizeBytes;
    private String sourceSizeText;
    private String outputSizeText;
    private Integer progress = 0;
    private TaskStatus status = TaskStatus.WAITING;
    private String message;
    private CompressProfile profile;
    private LocalDateTime createTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double savePercent;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getSourceSizeBytes() {
        return sourceSizeBytes;
    }

    public void setSourceSizeBytes(Long sourceSizeBytes) {
        this.sourceSizeBytes = sourceSizeBytes;
    }

    public Long getOutputSizeBytes() {
        return outputSizeBytes;
    }

    public void setOutputSizeBytes(Long outputSizeBytes) {
        this.outputSizeBytes = outputSizeBytes;
    }

    public String getSourceSizeText() {
        return sourceSizeText;
    }

    public void setSourceSizeText(String sourceSizeText) {
        this.sourceSizeText = sourceSizeText;
    }

    public String getOutputSizeText() {
        return outputSizeText;
    }

    public void setOutputSizeText(String outputSizeText) {
        this.outputSizeText = outputSizeText;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CompressProfile getProfile() {
        return profile;
    }

    public void setProfile(CompressProfile profile) {
        this.profile = profile;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Double getSavePercent() {
        return savePercent;
    }

    public void setSavePercent(Double savePercent) {
        this.savePercent = savePercent;
    }
}
