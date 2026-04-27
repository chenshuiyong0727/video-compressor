@echo off
chcp 65001 > nul
title 本地视频批量压缩工具

echo 正在启动本地视频批量压缩工具...

if not exist "local-video-compressor.jar" (
    echo 未找到 local-video-compressor.jar
    echo 请先运行 build.bat 构建项目，或将 jar 放到当前目录。
    pause
    exit /b 1
)

if not exist "ffmpeg\ffmpeg.exe" (
    echo 未找到 ffmpeg\ffmpeg.exe
    echo 请确认 ffmpeg.exe 已放到 ffmpeg 目录下。
    pause
    exit /b 1
)

if not exist "ffmpeg\ffprobe.exe" (
    echo 未找到 ffmpeg\ffprobe.exe
    echo 请确认 ffprobe.exe 已放到 ffmpeg 目录下。
    pause
    exit /b 1
)

if not exist "data" mkdir data
if not exist "data\input" mkdir data\input
if not exist "data\output" mkdir data\output
if not exist "data\backup" mkdir data\backup
if not exist "data\temp" mkdir data\temp
if not exist "data\logs" mkdir data\logs

start "local-video-compressor" java -jar local-video-compressor.jar

timeout /t 3 > nul
start "" http://localhost:8787

echo.
echo 系统已启动。
echo 如果浏览器没有自动打开，请手动访问：
echo http://localhost:8787
echo.
pause
