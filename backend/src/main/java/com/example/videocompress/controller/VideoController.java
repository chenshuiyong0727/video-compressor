package com.example.videocompress.controller;

import com.example.videocompress.model.ApiResult;
import com.example.videocompress.model.BackupRequest;
import com.example.videocompress.model.VideoFileInfo;
import com.example.videocompress.service.FileManageService;
import com.example.videocompress.service.VideoScanService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoScanService videoScanService;
    private final FileManageService fileManageService;

    public VideoController(VideoScanService videoScanService, FileManageService fileManageService) {
        this.videoScanService = videoScanService;
        this.fileManageService = fileManageService;
    }

    @GetMapping("/scan")
    public ApiResult<List<VideoFileInfo>> scan(
            @RequestParam(required = false) Long minSizeMb,
            @RequestParam(required = false) Long maxSizeMb,
            @RequestParam(required = false) String compressStatus
    ) throws IOException {
        return ApiResult.success(videoScanService.scan(minSizeMb, maxSizeMb, compressStatus));
    }

    @PostMapping("/backup")
    public ApiResult<List<String>> backup(@Valid @RequestBody BackupRequest request) throws IOException {
        return ApiResult.success(fileManageService.moveToBackup(request.getVideoIds()));
    }
}
