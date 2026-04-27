package com.example.videocompress.controller;

import com.example.videocompress.model.ApiResult;
import com.example.videocompress.model.CompressRequest;
import com.example.videocompress.model.CompressTask;
import com.example.videocompress.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/compress")
    public ApiResult<List<CompressTask>> compress(@Valid @RequestBody CompressRequest request) {
        return ApiResult.success(taskService.submit(request));
    }

    @GetMapping
    public ApiResult<List<CompressTask>> list() {
        return ApiResult.success(taskService.list());
    }

    @GetMapping("/{taskId}")
    public ApiResult<CompressTask> get(@PathVariable String taskId) {
        return ApiResult.success(taskService.find(taskId)
                .orElseThrow(() -> new IllegalArgumentException("任务不存在")));
    }

    @PostMapping("/{taskId}/cancel")
    public ApiResult<CompressTask> cancel(@PathVariable String taskId) {
        return ApiResult.success(taskService.cancel(taskId));
    }
}
