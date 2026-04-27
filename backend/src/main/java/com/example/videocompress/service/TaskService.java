package com.example.videocompress.service;

import com.example.videocompress.config.VideoCompressProperties;
import com.example.videocompress.model.CompressRequest;
import com.example.videocompress.model.CompressTask;
import com.example.videocompress.model.TaskStatus;
import com.example.videocompress.model.VideoFileInfo;
import com.example.videocompress.util.FileSizeUtils;
import com.example.videocompress.util.PathSafeUtils;
import com.example.videocompress.util.VideoNameUtils;
import jakarta.annotation.PreDestroy;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private static final int MAX_TASK_HISTORY = 200;

    private final VideoCompressProperties properties;
    private final VideoScanService videoScanService;
    private final FfmpegService ffmpegService;
    private final PersistenceService persistenceService;
    private final ExecutorService executorService;
    private final ConcurrentHashMap<String, CompressTask> taskMap = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<String> taskOrder = new ConcurrentLinkedDeque<>();

    public TaskService(
            VideoCompressProperties properties,
            VideoScanService videoScanService,
            FfmpegService ffmpegService,
            PersistenceService persistenceService
    ) {
        this.properties = properties;
        this.videoScanService = videoScanService;
        this.ffmpegService = ffmpegService;
        this.persistenceService = persistenceService;
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, "video-compress-worker");
            thread.setDaemon(true);
            return thread;
        };
        this.executorService = Executors.newSingleThreadExecutor(threadFactory);
    }

    public List<CompressTask> submit(CompressRequest request) {
        List<CompressTask> created = new ArrayList<>();
        String batchId = UUID.randomUUID().toString().replace("-", "");
        for (String videoId : request.getVideoIds()) {
            VideoFileInfo video = videoScanService.findById(videoId)
                    .orElseThrow(() -> new IllegalArgumentException("未找到视频，请先重新扫描: " + videoId));
            CompressTask task = createTask(video, request, batchId);
            taskMap.put(task.getTaskId(), task);
            taskOrder.addFirst(task.getTaskId());
            created.add(task);
            persistenceService.insertTask(task);
            persistenceService.recordOperation(
                    "CREATE_TASK",
                    task.getVideoId(),
                    task.getTaskId(),
                    task.getFileName(),
                    duplicateMessage(video)
            );
            executorService.submit(() -> runTask(task.getTaskId()));
        }
        cleanupOldFinishedTasks();
        return created;
    }

    public List<CompressTask> list() {
        return persistenceService.listTasks(MAX_TASK_HISTORY);
    }

    public Optional<CompressTask> find(String taskId) {
        CompressTask task = taskMap.get(taskId);
        return task == null ? persistenceService.findTask(taskId) : Optional.of(task);
    }

    public CompressTask cancel(String taskId) {
        CompressTask task = taskMap.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在");
        }
        if (task.getStatus() == TaskStatus.WAITING) {
            task.setStatus(TaskStatus.CANCELLED);
            task.setProgress(0);
            task.setMessage("已取消等待中的任务");
            task.setEndTime(LocalDateTime.now());
            persistenceService.updateTask(task);
            persistenceService.recordOperation("CANCEL_TASK", task.getVideoId(), task.getTaskId(), task.getFileName(), task.getMessage());
            return task;
        }
        if (task.getStatus() == TaskStatus.RUNNING) {
            throw new IllegalStateException("当前版本暂不支持取消正在运行的 FFmpeg 进程");
        }
        return task;
    }

    private String duplicateMessage(VideoFileInfo video) {
        if (Boolean.TRUE.equals(video.getDuplicateCompressionRisk())) {
            return "创建压缩任务：该文件已有成功压缩记录，属于重复压缩";
        }
        return "创建压缩任务";
    }

    private CompressTask createTask(VideoFileInfo video, CompressRequest request, String batchId) {
        Path outputPath = VideoNameUtils.compressedOutputPath(
                properties.outputDir(),
                video.getFileName(),
                Boolean.TRUE.equals(request.getOverwriteOutput())
        );
        Path safeOutput = PathSafeUtils.ensureInside(properties.outputDir(), outputPath);

        CompressTask task = new CompressTask();
        task.setTaskId(UUID.randomUUID().toString().replace("-", ""));
        task.setBatchId(batchId);
        task.setVideoId(video.getId());
        task.setSourcePath(video.getAbsolutePath());
        task.setOutputPath(safeOutput.toString());
        task.setFileName(video.getFileName());
        task.setSourceSizeBytes(video.getSizeBytes());
        task.setSourceSizeText(FileSizeUtils.format(video.getSizeBytes()));
        task.setProgress(0);
        task.setStatus(TaskStatus.WAITING);
        task.setMessage("等待压缩");
        task.setProfile(request.getProfile() == null ? com.example.videocompress.model.CompressProfile.BALANCED : request.getProfile());
        task.setCreateTime(LocalDateTime.now());
        return task;
    }

    private void runTask(String taskId) {
        CompressTask task = taskMap.get(taskId);
        if (task == null || task.getStatus() == TaskStatus.CANCELLED) {
            return;
        }
        try {
            VideoFileInfo video = videoScanService.findById(task.getVideoId())
                    .orElseThrow(() -> new IllegalArgumentException("源文件不存在，任务无法执行"));
            task.setStatus(TaskStatus.RUNNING);
            task.setStartTime(LocalDateTime.now());
            task.setMessage("正在压缩");
            persistenceService.updateTask(task);
            persistenceService.recordOperation("TASK_RUNNING", task.getVideoId(), task.getTaskId(), task.getFileName(), "开始压缩");
            AtomicInteger lastPersistedProgress = new AtomicInteger(task.getProgress());
            ffmpegService.compress(task, video, progressTask -> {
                int progress = progressTask.getProgress();
                if (progress - lastPersistedProgress.get() >= 5 || progress >= 99) {
                    lastPersistedProgress.set(progress);
                    persistenceService.updateTask(progressTask);
                }
            });
            task.setStatus(TaskStatus.SUCCESS);
            task.setProgress(100);
            task.setMessage("压缩完成");
            task.setEndTime(LocalDateTime.now());
            persistenceService.updateTask(task);
            persistenceService.recordOperation("TASK_SUCCESS", task.getVideoId(), task.getTaskId(), task.getFileName(), "压缩完成");
        } catch (Exception ex) {
            task.setStatus(TaskStatus.FAILED);
            task.setMessage(ex.getMessage());
            task.setEndTime(LocalDateTime.now());
            persistenceService.updateTask(task);
            persistenceService.recordOperation("TASK_FAILED", task.getVideoId(), task.getTaskId(), task.getFileName(), ex.getMessage());
        } finally {
            cleanupOldFinishedTasks();
        }
    }

    private void cleanupOldFinishedTasks() {
        while (taskOrder.size() > MAX_TASK_HISTORY) {
            String taskId = taskOrder.pollLast();
            if (taskId == null) {
                return;
            }
            CompressTask task = taskMap.get(taskId);
            if (task != null && task.getStatus() != TaskStatus.WAITING && task.getStatus() != TaskStatus.RUNNING) {
                taskMap.remove(taskId);
            } else if (task != null) {
                taskOrder.addLast(taskId);
                return;
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdownNow();
    }
}
