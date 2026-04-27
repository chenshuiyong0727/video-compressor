package com.example.videocompress.model;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class BackupRequest {

    @NotEmpty(message = "请选择要移动到备份目录的视频")
    private List<String> videoIds;

    public List<String> getVideoIds() {
        return videoIds;
    }

    public void setVideoIds(List<String> videoIds) {
        this.videoIds = videoIds;
    }
}
