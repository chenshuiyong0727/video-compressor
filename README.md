# 本地视频批量压缩工具

`local-video-compressor` 是一个 Windows 本地使用的视频批量压缩工具。用户把手机视频手动复制到 `data/input` 后，在本地网页中扫描、选择、批量压缩，并把压缩结果输出到 `data/output`。

第一版只做本地文件处理，不做登录、数据库、云上传、手机端自动扫描或自动删除源文件。

## 功能说明

- 扫描 `data/input` 下的视频文件。
- 展示文件大小、时长、分辨率、编码格式、修改时间。
- 支持最小文件大小筛选，默认 100MB。
- 支持单个视频压缩和批量排队压缩。
- 后台使用单线程队列逐个调用 FFmpeg，避免同时压缩多个大视频。
- 展示任务状态、进度、原大小、新大小、节省比例和失败原因。
- 使用 SQLite 持久化扫描记录、压缩任务和操作记录。
- 可识别文件是否已有成功压缩记录，重复压缩前会提醒。
- 压缩结果输出到 `data/output`。
- 源文件不会自动删除，只能由用户确认后移动到 `data/backup`。
- 系统设置页可检测 `ffmpeg.exe`、`ffprobe.exe` 和本地目录状态。

## 技术栈

- 后端：JDK 21、Spring Boot 3、Maven
- 前端：Vue 3、Vite、Element Plus、Axios
- 视频处理：`ffmpeg.exe`、`ffprobe.exe`
- 持久化：SQLite，本地文件数据库，无需单独安装数据库服务

## 目录说明

```text
local-video-compressor
+-- backend
+-- frontend
+-- ffmpeg
|   +-- ffmpeg.exe
|   +-- ffprobe.exe
+-- data
|   +-- input
|   +-- output
|   +-- backup
|   +-- temp
|   +-- logs
+-- start.bat
+-- stop.bat
+-- build.bat
+-- README.md
```

`data/input` 放待压缩视频，`data/output` 放压缩后视频，`data/backup` 放用户确认后移动的源视频。

SQLite 数据库文件会自动创建在：

```text
data/db/video-compressor.db
```

不需要安装 MySQL、Redis 或 SQLite 服务。

## 准备 FFmpeg

FFmpeg 不需要全局安装，也不需要配置 Windows 环境变量。

只需要把下面两个文件放到项目根目录的 `ffmpeg` 文件夹下：

```text
ffmpeg/ffmpeg.exe
ffmpeg/ffprobe.exe
```

后端会通过相对路径调用：

```yaml
video:
  ffmpeg-path: ./ffmpeg/ffmpeg.exe
  ffprobe-path: ./ffmpeg/ffprobe.exe
```

## 构建方式

确保本机已有：

- JDK 21
- Maven
- Node.js 和 npm

然后双击或在命令行运行：

```bat
build.bat
```

构建流程会：

1. 安装前端依赖。
2. 构建 Vue 前端。
3. 把 `frontend/dist` 复制到 `backend/src/main/resources/static`。
4. 打包 Spring Boot。
5. 复制生成的 `backend/target/local-video-compressor.jar` 到项目根目录。

## 启动方式

构建完成并准备好 FFmpeg 后，双击：

```bat
start.bat
```

脚本会启动：

```bat
java -jar local-video-compressor.jar
```

并自动打开：

```text
http://localhost:8787
```

正式发布时只需要：

```text
local-video-compressor.jar
start.bat
stop.bat
ffmpeg/
data/
README.md
```

不需要单独启动 Vue 服务。

## 开发模式

后端：

```bat
cd backend
mvn spring-boot:run
```

前端：

```bat
cd frontend
npm install
npm run dev
```

开发访问：

```text
http://localhost:5173
```

Vite 会把 `/api` 代理到 `http://localhost:8787`。

## 使用流程

1. 将手机视频复制到 `data/input`。
2. 启动系统并打开网页。
3. 在“视频扫描”页设置最小大小并点击“扫描视频”。
4. 勾选需要压缩的视频。
5. 选择压缩模式。
6. 点击“批量压缩”。
7. 在“压缩任务”页查看进度和结果。
8. 确认输出文件可用后，可把源文件移动到 `data/backup`。
9. 手动把 `data/output` 中的压缩视频复制回手机。

## 修改视频目录

可以在“系统设置”页修改 `input`、`output`、`backup`、`temp`、`logs` 目录。

目录支持绝对路径，例如：

```text
input: E:/照片
output: E:/压缩后视频
backup: E:/视频备份
```

保存后后端会立即使用新目录，并把配置写入：

```text
data/config/video-dirs.json
```

下次启动会自动读取上次保存的目录配置。浏览器出于安全限制不能直接读取 Windows 任意文件夹路径，所以当前版本通过输入路径保存。

## 压缩模式

- 高清优先：`libx264`、`preset slow`、`crf 22`、`yuv420p`、`aac 128k`、输出 MP4
- 平衡模式：`libx264`、`preset medium`、`crf 24`、`yuv420p`、`aac 128k`、输出 MP4
- 小体积优先：`libx264`、`preset medium`、`crf 27`、`yuv420p`、`aac 96k`、输出 MP4

所有压缩模式都会输出 H.264 编码和 MP4 容器，不使用 HEVC/H.265。HEVC/H.265 虽然可能更小，但部分设备、相册、播放器或第三方软件解析不好，所以第一版完全不启用。

## 安全注意事项

- 系统不会直接删除源视频。
- 只允许将源视频从 `data/input` 移动到 `data/backup`。
- 后端接口只接受 `videoId`，不会根据前端传入的任意路径操作文件。
- 输出文件使用 `_compressed` 后缀，不会覆盖源文件。
- 压缩完成后会校验输出文件是否存在、大小是否大于 0、是否能被 ffprobe 识别。
- 如果压缩后文件比源文件更大，任务会标记为失败并提示不建议使用。

## 持久化记录

系统会自动记录：

- 扫描到的视频文件信息。
- 每一次压缩任务的状态、进度、结果和失败原因。
- 创建任务、开始压缩、压缩成功、压缩失败、取消任务、移动备份等操作记录。

重复压缩判断使用：

```text
relativePath + sizeBytes + lastModified
```

这样不会对几百 MB 或数 GB 视频额外计算 hash，扫描速度更适合本地批量工具。若文件已经有成功压缩记录或已经存在输出文件，页面会显示重复风险，并在再次压缩前提醒。

## 常见问题

### 页面提示 FFmpeg 缺失

请确认：

```text
ffmpeg/ffmpeg.exe
ffmpeg/ffprobe.exe
```

两个文件都存在。它们不需要放到系统 PATH，也不需要全局安装。

### 扫描不到视频

请确认视频已经复制到 `data/input`，并且扩展名属于：

```text
mp4, mov, m4v, mkv, avi, 3gp
```

如果视频小于默认的 100MB，可以把扫描页的最小大小调低。

### 压缩任务失败

常见原因：

- FFmpeg 文件缺失或不可执行。
- 源视频已被移动或删除。
- 视频文件损坏，ffprobe 无法识别。
- 压缩后文件比源文件更大。

### 如何停止服务

关闭 `start.bat` 启动时打开的 Java 命令行窗口即可。也可以运行 `stop.bat` 查看提示。
