package com.example.videocompress.util;

import java.nio.file.Path;

public final class PathSafeUtils {

    private PathSafeUtils() {
    }

    public static Path ensureInside(Path baseDir, Path target) {
        Path normalizedBase = baseDir.toAbsolutePath().normalize();
        Path normalizedTarget = target.toAbsolutePath().normalize();
        if (!normalizedTarget.startsWith(normalizedBase)) {
            throw new IllegalArgumentException("非法文件路径，禁止操作配置目录之外的文件");
        }
        return normalizedTarget;
    }

    public static Path resolveInside(Path baseDir, String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        Path relative = Path.of(relativePath);
        if (relative.isAbsolute()) {
            throw new IllegalArgumentException("不允许传入绝对路径");
        }
        return ensureInside(baseDir, baseDir.resolve(relative));
    }
}
