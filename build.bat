@echo off
chcp 65001 > nul
title 构建本地视频批量压缩工具

echo 开始构建前端...
cd frontend
call npm install
if errorlevel 1 (
    echo 前端依赖安装失败。
    pause
    exit /b 1
)

call npm run build
if errorlevel 1 (
    echo 前端构建失败。
    pause
    exit /b 1
)

echo 复制前端 dist 到后端 static...
cd ..
if exist "backend\src\main\resources\static" rmdir /s /q "backend\src\main\resources\static"
mkdir "backend\src\main\resources\static"
xcopy "frontend\dist\*" "backend\src\main\resources\static\" /E /I /Y
if errorlevel 1 (
    echo 前端静态资源复制失败。
    pause
    exit /b 1
)

echo 开始构建后端...
cd backend
call mvn clean package -DskipTests
if errorlevel 1 (
    echo 后端构建失败。
    pause
    exit /b 1
)

cd ..
copy /Y "backend\target\local-video-compressor.jar" "local-video-compressor.jar"
if errorlevel 1 (
    echo jar 复制失败。
    pause
    exit /b 1
)

if not exist "data" mkdir data
if not exist "data\input" mkdir data\input
if not exist "data\output" mkdir data\output
if not exist "data\backup" mkdir data\backup
if not exist "data\temp" mkdir data\temp
if not exist "data\logs" mkdir data\logs
if not exist "ffmpeg" mkdir ffmpeg

echo.
echo 构建完成：local-video-compressor.jar
echo 请将 ffmpeg.exe 和 ffprobe.exe 放入 ffmpeg 目录后运行 start.bat。
echo.
pause
