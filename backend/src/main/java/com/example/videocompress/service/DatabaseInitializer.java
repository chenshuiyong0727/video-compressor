package com.example.videocompress.service;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseInitializer {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS video_file (
                    id TEXT PRIMARY KEY,
                    file_name TEXT NOT NULL,
                    absolute_path TEXT NOT NULL,
                    relative_path TEXT NOT NULL,
                    size_bytes INTEGER NOT NULL,
                    size_text TEXT,
                    last_modified INTEGER NOT NULL,
                    duration_seconds REAL,
                    duration_text TEXT,
                    width INTEGER,
                    height INTEGER,
                    resolution_text TEXT,
                    codec TEXT,
                    format TEXT,
                    scanned_at TEXT NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS compress_task (
                    task_id TEXT PRIMARY KEY,
                    batch_id TEXT,
                    video_id TEXT NOT NULL,
                    source_path TEXT NOT NULL,
                    output_path TEXT NOT NULL,
                    file_name TEXT NOT NULL,
                    source_size_bytes INTEGER,
                    output_size_bytes INTEGER,
                    source_size_text TEXT,
                    output_size_text TEXT,
                    progress INTEGER,
                    status TEXT NOT NULL,
                    message TEXT,
                    profile TEXT,
                    create_time TEXT,
                    start_time TEXT,
                    end_time TEXT,
                    save_percent REAL
                )
                """);
        addColumnIfMissing("compress_task", "batch_id", "TEXT");
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS operation_log (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    operation_type TEXT NOT NULL,
                    video_id TEXT,
                    task_id TEXT,
                    file_name TEXT,
                    message TEXT,
                    create_time TEXT NOT NULL
                )
                """);
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_compress_task_video_id ON compress_task(video_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_compress_task_batch_id ON compress_task(batch_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_compress_task_status ON compress_task(status)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_operation_log_video_id ON operation_log(video_id)");
    }

    private void addColumnIfMissing(String tableName, String columnName, String definition) {
        Boolean exists = jdbcTemplate.query(
                "PRAGMA table_info(" + tableName + ")",
                rs -> {
                    while (rs.next()) {
                        if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                            return true;
                        }
                    }
                    return false;
                }
        );
        if (!Boolean.TRUE.equals(exists)) {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        }
    }
}
