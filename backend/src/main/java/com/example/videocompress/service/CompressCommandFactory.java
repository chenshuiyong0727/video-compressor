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
                    "-map", "0:v:0",
                    "-map", "0:a?",
                    "-c:v", "libx264",
                    "-preset", "slow",
                    "-crf", "22",
                    "-pix_fmt", "yuv420p",
                    "-tag:v", "avc1",
                    "-c:a", "aac",
                    "-b:a", "128k",
                    "-ac", "2",
                    "-movflags", "+faststart",
                    "-progress", "pipe:1",
                    "-f", "mp4",
                    outputPath
            );
            case SMALL_SIZE -> List.of(
                    ffmpegPath, "-y",
                    "-i", inputPath,
                    "-map", "0:v:0",
                    "-map", "0:a?",
                    "-c:v", "libx264",
                    "-preset", "medium",
                    "-crf", "27",
                    "-pix_fmt", "yuv420p",
                    "-tag:v", "avc1",
                    "-c:a", "aac",
                    "-b:a", "96k",
                    "-ac", "2",
                    "-movflags", "+faststart",
                    "-progress", "pipe:1",
                    "-f", "mp4",
                    outputPath
            );
            case BALANCED -> List.of(
                    ffmpegPath, "-y",
                    "-i", inputPath,
                    "-map", "0:v:0",
                    "-map", "0:a?",
                    "-c:v", "libx264",
                    "-preset", "medium",
                    "-crf", "24",
                    "-pix_fmt", "yuv420p",
                    "-tag:v", "avc1",
                    "-c:a", "aac",
                    "-b:a", "128k",
                    "-ac", "2",
                    "-movflags", "+faststart",
                    "-progress", "pipe:1",
                    "-f", "mp4",
                    outputPath
            );
        };
    }
}
