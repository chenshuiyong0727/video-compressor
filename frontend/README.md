# Frontend

Vue 3 + Vite + Element Plus 前端，提供视频扫描、压缩任务和系统设置页面。

## 开发启动

```bat
npm install
npm run dev
```

访问：

```text
http://localhost:5173
```

开发阶段 `/api` 会代理到：

```text
http://localhost:8787
```

## 正式发布

运行根目录 `build.bat`，会把 `frontend/dist` 复制到：

```text
backend/src/main/resources/static
```

最终只需要启动 Spring Boot jar。
