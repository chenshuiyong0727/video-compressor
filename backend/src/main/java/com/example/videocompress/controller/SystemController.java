package com.example.videocompress.controller;

import com.example.videocompress.model.ApiResult;
import com.example.videocompress.model.OperationLog;
import com.example.videocompress.model.SystemDirs;
import com.example.videocompress.model.SystemCheckResult;
import com.example.videocompress.service.SystemService;
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
@RequestMapping("/api/system")
public class SystemController {

    private final SystemService systemService;

    public SystemController(SystemService systemService) {
        this.systemService = systemService;
    }

    @GetMapping("/check")
    public ApiResult<SystemCheckResult> check() {
        return ApiResult.success(systemService.check());
    }

    @GetMapping("/dirs")
    public ApiResult<SystemDirs> dirs() {
        return ApiResult.success(systemService.dirs());
    }

    @PostMapping("/dirs")
    public ApiResult<SystemDirs> updateDirs(@Valid @RequestBody SystemDirs dirs) throws IOException {
        return ApiResult.success(systemService.updateDirs(dirs));
    }

    @GetMapping("/operations")
    public ApiResult<List<OperationLog>> operations(@RequestParam(required = false) Integer limit) {
        return ApiResult.success(systemService.operationLogs(limit));
    }
}
