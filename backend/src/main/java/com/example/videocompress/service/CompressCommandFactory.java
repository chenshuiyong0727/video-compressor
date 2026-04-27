package com.example.videocompress.service;

import com.example.videocompress.model.CompressProfile;
import java.util.List;

public final class CompressCommandFactory {

    private CompressCommandFactory() {
    }

    public static List<String> buildCommand(
            String ffmpegPath,
            String inputPath,
            String outputPath,
            CompressProfile profile
    ) {
        CompressProfile actualProfile = profile == null ? CompressProfile.BALANCED : profile;
        return switch (actualProfile) {
            case HIGH_QUALITY -> List.of(
                    ffmpegPath, "-y",
                    "-i", inputPath,
                    "-c:v", "libx264",
                    "-preset", "slow",
                    "-crf", "22",
                    "-c:a", "aac",
                    "-b:a", "128k",
                    "-movflags", "+faststart",
                    "-progress", "pipe:1",
                    outputPath
            );
            case SMALL_SIZE -> List.of(
                    ffmpegPath, "-y",
                    "-i", inputPath,
                    "-c:v", "libx264",
                    "-preset", "medium",
                    "-crf", "27",
                    "-c:a", "aac",
                    "-b:a", "96k",
                    "-movflags", "+faststart",
                    "-progress", "pipe:1",
                    outputPath
            );
            case BALANCED -> List.of(
                    ffmpegPath, "-y",
                    "-i", inputPath,
                    "-c:v", "libx264",
                    "-preset", "medium",
                    "-crf", "24",
                    "-c:a", "aac",
                    "-b:a", "128k",
                    "-movflags", "+faststart",
                    "-progress", "pipe:1",
                    outputPath
            );
        };
    }
}
