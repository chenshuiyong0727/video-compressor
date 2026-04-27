package com.example.videocompress.service;

import com.example.videocompress.config.VideoCompressProperties;
import com.example.videocompress.model.OperationLog;
import com.example.videocompress.model.SystemDirs;
import com.example.videocompress.model.SystemCheckResult;
import com.example.videocompress.util.ProcessUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SystemService {

    private final VideoCompressProperties properties;
    private final DirectoryService directoryService;
    private final PersistenceService persistenceService;

    public SystemService(
            VideoCompressProperties properties,
            DirectoryService directoryService,
            PersistenceService persistenceService
    ) {
        this.properties = properties;
        this.directoryService = directoryService;
        this.persistenceService = persistenceService;
    }

    public SystemCheckResult check() {
        SystemCheckResult result = new SystemCheckResult();
        boolean ffmpegExists = Files.isRegularFile(properties.ffmpegPath());
        boolean ffprobeExists = Files.isRegularFile(properties.ffprobePath());
        result.setFfmpegExists(ffmpegExists);
        result.setFfprobeExists(ffprobeExists);
        result.setFfmpegPath(properties.getFfmpegPath());
        result.setFfprobePath(properties.getFfprobePath());
        result.setInputDirExists(Files.isDirectory(properties.inputDir()));
        result.setOutputDirExists(Files.isDirectory(properties.outputDir()));
        result.setBackupDirExists(Files.isDirectory(properties.backupDir()));
        result.setFfmpegVersion(ffmpegExists
                ? ProcessUtils.firstLine(List.of(properties.ffmpegPath().toString(), "-version"), 5)
                : "");
        result.setFfprobeVersion(ffprobeExists
                ? ProcessUtils.firstLine(List.of(properties.ffprobePath().toString(), "-version"), 5)
                : "");
        boolean ok = ffmpegExists
                && ffprobeExists
                && Boolean.TRUE.equals(result.getInputDirExists())
                && Boolean.TRUE.equals(result.getOutputDirExists())
                && Boolean.TRUE.equals(result.getBackupDirExists());
        result.setMessage(ok ? "系统环境正常" : "请检查 ffmpeg/ffprobe 文件和 data 目录");
        return result;
    }

    public SystemDirs dirs() {
        return directoryService.dirs();
    }

    public SystemDirs updateDirs(SystemDirs dirs) throws IOException {
        return directoryService.updateDirs(dirs);
    }

    public List<OperationLog> operationLogs(Integer limit) {
        int actualLimit = limit == null ? 200 : Math.min(Math.max(limit, 1), 1000);
        return persistenceService.listOperationLogs(actualLimit);
    }
}
