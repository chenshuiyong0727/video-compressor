# Backend

Spring Boot 3 后端服务，负责系统检测、视频扫描、FFmpeg 压缩任务、任务状态查询和源文件移动备份。

## 启动

```bat
mvn spring-boot:run
```

默认端口：

```text
http://localhost:8787
```

## 主要接口

- `GET /api/system/check`
- `GET /api/system/dirs`
- `GET /api/videos/scan?minSizeMb=100`
- `POST /api/videos/backup`
- `POST /api/tasks/compress`
- `GET /api/tasks`
- `GET /api/tasks/{taskId}`
- `POST /api/tasks/{taskId}/cancel`
