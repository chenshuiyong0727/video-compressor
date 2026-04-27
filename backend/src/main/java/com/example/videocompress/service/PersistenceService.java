package com.example.videocompress.service;

import com.example.videocompress.model.CompressProfile;
import com.example.videocompress.model.CompressTask;
import com.example.videocompress.model.OperationLog;
import com.example.videocompress.model.TaskStatus;
import com.example.videocompress.model.VideoCompressSummary;
import com.example.videocompress.model.VideoFileInfo;
import jakarta.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@DependsOn("databaseInitializer")
public class PersistenceService {

    private final JdbcTemplate jdbcTemplate;

    public PersistenceService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void markInterruptedTasks() {
        String now = encodeTime(LocalDateTime.now());
        jdbcTemplate.update("""
                UPDATE compress_task
                SET status = 'FAILED',
                    message = '服务重启，未完成任务已中断',
                    end_time = ?
                WHERE status IN ('WAITING', 'RUNNING')
                """, now);
    }

    public void upsertVideo(VideoFileInfo info) {
        jdbcTemplate.update("""
                INSERT INTO video_file (
                    id, file_name, absolute_path, relative_path, size_bytes, size_text,
                    last_modified, duration_seconds, duration_text, width, height,
                    resolution_text, codec, format, scanned_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    file_name = excluded.file_name,
                    absolute_path = excluded.absolute_path,
                    relative_path = excluded.relative_path,
                    size_bytes = excluded.size_bytes,
                    size_text = excluded.size_text,
                    last_modified = excluded.last_modified,
                    duration_seconds = excluded.duration_seconds,
                    duration_text = excluded.duration_text,
                    width = excluded.width,
                    height = excluded.height,
                    resolution_text = excluded.resolution_text,
                    codec = excluded.codec,
                    format = excluded.format,
                    scanned_at = excluded.scanned_at
                """,
                info.getId(),
                info.getFileName(),
                info.getAbsolutePath(),
                info.getRelativePath(),
                info.getSizeBytes(),
                info.getSizeText(),
                info.getLastModified(),
                info.getDurationSeconds(),
                info.getDurationText(),
                info.getWidth(),
                info.getHeight(),
                info.getResolutionText(),
                info.getCodec(),
                info.getFormat(),
                encodeTime(LocalDateTime.now())
        );
    }

    public VideoCompressSummary summaryForVideo(String videoId) {
        VideoCompressSummary summary = new VideoCompressSummary();
        jdbcTemplate.query("""
                SELECT status, COUNT(*) AS count
                FROM compress_task
                WHERE video_id = ?
                GROUP BY status
                """, rs -> {
            String status = rs.getString("status");
            int count = rs.getInt("count");
            if (TaskStatus.SUCCESS.name().equals(status)) {
                summary.setSuccessCount(count);
            } else if (TaskStatus.FAILED.name().equals(status)) {
                summary.setFailedCount(count);
            } else if (TaskStatus.RUNNING.name().equals(status)) {
                summary.setRunningCount(count);
            } else if (TaskStatus.WAITING.name().equals(status)) {
                summary.setWaitingCount(count);
            }
        }, videoId);

        List<CompressTask> latest = jdbcTemplate.query("""
                SELECT *
                FROM compress_task
                WHERE video_id = ?
                ORDER BY COALESCE(end_time, start_time, create_time) DESC
                LIMIT 1
                """, this::mapTask, videoId);
        if (!latest.isEmpty()) {
            CompressTask task = latest.get(0);
            summary.setLastTaskStatus(task.getStatus() == null ? null : task.getStatus().name());
            summary.setLastTaskMessage(task.getMessage());
            summary.setLastOutputPath(task.getOutputPath());
            summary.setLastTaskTime(toMillis(task.getEndTime() != null ? task.getEndTime() : task.getCreateTime()));
        }
        return summary;
    }

    public void insertTask(CompressTask task) {
        jdbcTemplate.update("""
                INSERT INTO compress_task (
                    task_id, video_id, source_path, output_path, file_name,
                    source_size_bytes, output_size_bytes, source_size_text, output_size_text,
                    progress, status, message, profile, create_time, start_time, end_time, save_percent
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                task.getTaskId(),
                task.getVideoId(),
                task.getSourcePath(),
                task.getOutputPath(),
                task.getFileName(),
                task.getSourceSizeBytes(),
                task.getOutputSizeBytes(),
                task.getSourceSizeText(),
                task.getOutputSizeText(),
                task.getProgress(),
                task.getStatus().name(),
                task.getMessage(),
                task.getProfile() == null ? null : task.getProfile().name(),
                encodeTime(task.getCreateTime()),
                encodeTime(task.getStartTime()),
                encodeTime(task.getEndTime()),
                task.getSavePercent()
        );
    }

    public void updateTask(CompressTask task) {
        jdbcTemplate.update("""
                UPDATE compress_task
                SET output_size_bytes = ?,
                    output_size_text = ?,
                    progress = ?,
                    status = ?,
                    message = ?,
                    profile = ?,
                    start_time = ?,
                    end_time = ?,
                    save_percent = ?
                WHERE task_id = ?
                """,
                task.getOutputSizeBytes(),
                task.getOutputSizeText(),
                task.getProgress(),
                task.getStatus().name(),
                task.getMessage(),
                task.getProfile() == null ? null : task.getProfile().name(),
                encodeTime(task.getStartTime()),
                encodeTime(task.getEndTime()),
                task.getSavePercent(),
                task.getTaskId()
        );
    }

    public Optional<CompressTask> findTask(String taskId) {
        List<CompressTask> tasks = jdbcTemplate.query(
                "SELECT * FROM compress_task WHERE task_id = ?",
                this::mapTask,
                taskId
        );
        return tasks.stream().findFirst();
    }

    public List<CompressTask> listTasks(int limit) {
        return jdbcTemplate.query("""
                SELECT *
                FROM compress_task
                ORDER BY COALESCE(end_time, start_time, create_time) DESC
                LIMIT ?
                """, this::mapTask, limit);
    }

    public void recordOperation(String type, String videoId, String taskId, String fileName, String message) {
        jdbcTemplate.update("""
                INSERT INTO operation_log (operation_type, video_id, task_id, file_name, message, create_time)
                VALUES (?, ?, ?, ?, ?, ?)
                """, type, videoId, taskId, fileName, message, encodeTime(LocalDateTime.now()));
    }

    public List<OperationLog> listOperationLogs(int limit) {
        return jdbcTemplate.query("""
                SELECT *
                FROM operation_log
                ORDER BY id DESC
                LIMIT ?
                """, this::mapOperationLog, limit);
    }

    private OperationLog mapOperationLog(ResultSet rs, int rowNum) throws SQLException {
        OperationLog log = new OperationLog();
        log.setId(rs.getLong("id"));
        log.setOperationType(rs.getString("operation_type"));
        log.setVideoId(rs.getString("video_id"));
        log.setTaskId(rs.getString("task_id"));
        log.setFileName(rs.getString("file_name"));
        log.setMessage(rs.getString("message"));
        log.setCreateTime(rs.getString("create_time"));
        return log;
    }

    private CompressTask mapTask(ResultSet rs, int rowNum) throws SQLException {
        CompressTask task = new CompressTask();
        task.setTaskId(rs.getString("task_id"));
        task.setVideoId(rs.getString("video_id"));
        task.setSourcePath(rs.getString("source_path"));
        task.setOutputPath(rs.getString("output_path"));
        task.setFileName(rs.getString("file_name"));
        task.setSourceSizeBytes(readLong(rs, "source_size_bytes"));
        task.setOutputSizeBytes(readLong(rs, "output_size_bytes"));
        task.setSourceSizeText(rs.getString("source_size_text"));
        task.setOutputSizeText(rs.getString("output_size_text"));
        task.setProgress(rs.getInt("progress"));
        task.setStatus(TaskStatus.valueOf(rs.getString("status")));
        task.setMessage(rs.getString("message"));
        String profile = rs.getString("profile");
        if (profile != null) {
            task.setProfile(CompressProfile.valueOf(profile));
        }
        task.setCreateTime(decodeTime(rs.getString("create_time")));
        task.setStartTime(decodeTime(rs.getString("start_time")));
        task.setEndTime(decodeTime(rs.getString("end_time")));
        task.setSavePercent(readDouble(rs, "save_percent"));
        return task;
    }

    private Long readLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    private Double readDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    private String encodeTime(LocalDateTime time) {
        return time == null ? null : time.toString();
    }

    private LocalDateTime decodeTime(String value) {
        return value == null || value.isBlank() ? null : LocalDateTime.parse(value);
    }

    private Long toMillis(LocalDateTime time) {
        return time == null ? null : time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
