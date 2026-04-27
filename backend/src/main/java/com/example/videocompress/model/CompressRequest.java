package com.example.videocompress.model;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CompressRequest {

    @NotEmpty(message = "请选择要压缩的视频")
    private List<String> videoIds;
    private CompressProfile profile = CompressProfile.BALANCED;
    private Boolean overwriteOutput = false;

    public List<String> getVideoIds() {
        return videoIds;
    }

    public void setVideoIds(List<String> videoIds) {
        this.videoIds = videoIds;
    }

    public CompressProfile getProfile() {
        return profile;
    }

    public void setProfile(CompressProfile profile) {
        this.profile = profile;
    }

    public Boolean getOverwriteOutput() {
        return overwriteOutput;
    }

    public void setOverwriteOutput(Boolean overwriteOutput) {
        this.overwriteOutput = overwriteOutput;
    }
}
