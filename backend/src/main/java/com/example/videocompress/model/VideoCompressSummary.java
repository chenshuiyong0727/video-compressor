package com.example.videocompress.model;

public class VideoCompressSummary {

    private int successCount;
    private int failedCount;
    private int runningCount;
    private int waitingCount;
    private String lastTaskStatus;
    private String lastTaskMessage;
    private String lastOutputPath;
    private Long lastTaskTime;

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public int getRunningCount() {
        return runningCount;
    }

    public void setRunningCount(int runningCount) {
        this.runningCount = runningCount;
    }

    public int getWaitingCount() {
        return waitingCount;
    }

    public void setWaitingCount(int waitingCount) {
        this.waitingCount = waitingCount;
    }

    public String getLastTaskStatus() {
        return lastTaskStatus;
    }

    public void setLastTaskStatus(String lastTaskStatus) {
        this.lastTaskStatus = lastTaskStatus;
    }

    public String getLastTaskMessage() {
        return lastTaskMessage;
    }

    public void setLastTaskMessage(String lastTaskMessage) {
        this.lastTaskMessage = lastTaskMessage;
    }

    public String getLastOutputPath() {
        return lastOutputPath;
    }

    public void setLastOutputPath(String lastOutputPath) {
        this.lastOutputPath = lastOutputPath;
    }

    public Long getLastTaskTime() {
        return lastTaskTime;
    }

    public void setLastTaskTime(Long lastTaskTime) {
        this.lastTaskTime = lastTaskTime;
    }
}
