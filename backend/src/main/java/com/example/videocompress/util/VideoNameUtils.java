package com.example.videocompress.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class VideoNameUtils {

    private VideoNameUtils() {
    }

    public static String videoId(String relativePath, long sizeBytes, long lastModified) {
        String raw = relativePath + "|" + sizeBytes + "|" + lastModified;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8))).substring(0, 24);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("当前 JDK 不支持 SHA-256", ex);
        }
    }

    public static Path compressedOutputPath(Path outputDir, String sourceFileName, boolean overwrite) {
        String base = baseName(sourceFileName);
        String extension = ".mp4";
        Path candidate = outputDir.resolve(base + "_compressed" + extension);
        if (overwrite || !Files.exists(candidate)) {
            return candidate;
        }
        int index = 1;
        while (true) {
            Path next = outputDir.resolve(base + "_compressed_" + index + extension);
            if (!Files.exists(next)) {
                return next;
            }
            index++;
        }
    }

    public static Path uniqueSibling(Path candidate) {
        if (!Files.exists(candidate)) {
            return candidate;
        }
        String fileName = candidate.getFileName().toString();
        String base = baseName(fileName);
        String extension = extension(fileName);
        int index = 1;
        while (true) {
            Path next = candidate.resolveSibling(base + "_" + index + extension);
            if (!Files.exists(next)) {
                return next;
            }
            index++;
        }
    }

    public static String extension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0) {
            return "";
        }
        return fileName.substring(index);
    }

    public static String baseName(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0) {
            return fileName;
        }
        return fileName.substring(0, index);
    }
}
