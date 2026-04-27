package com.example.videocompress.controller;

import com.example.videocompress.model.ApiResult;
import com.example.videocompress.model.SystemCheckResult;
import com.example.videocompress.service.SystemService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ApiResult<Map<String, String>> dirs() {
        return ApiResult.success(systemService.dirs());
    }
}
