package com.example.videocompress.service;

import com.example.videocompress.config.VideoCompressProperties;
import com.example.videocompress.model.VideoMetadata;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Service
public class FfprobeService {

    private final VideoCompressProperties properties;
    private final ObjectMapper objectMapper;

    public FfprobeService(VideoCompressProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public VideoMetadata probe(Path videoPath) throws IOException, InterruptedException {
        if (!Files.isRegularFile(properties.ffprobePath())) {
            throw new IOException("未找到 ffprobe.exe: " + properties.getFfprobePath());
        }
        List<String> command = List.of(
                properties.ffprobePath().toString(),
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=width,height,codec_name",
                "-show_entries", "format=duration",
                "-of", "json",
                videoPath.toString()
        );
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("ffprobe 读取超时");
        }
        if (process.exitValue() != 0) {
            throw new IOException("ffprobe 读取失败: " + output);
        }
        return parseMetadata(output);
    }

    private VideoMetadata parseMetadata(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json);
        VideoMetadata metadata = new VideoMetadata();
        JsonNode stream = root.path("streams").isArray() && !root.path("streams").isEmpty()
                ? root.path("streams").get(0)
                : objectMapper.createObjectNode();
        if (stream.has("width")) {
            metadata.setWidth(stream.path("width").asInt());
        }
        if (stream.has("height")) {
            metadata.setHeight(stream.path("height").asInt());
        }
        if (stream.has("codec_name")) {
            metadata.setCodec(stream.path("codec_name").asText());
        }
        JsonNode duration = root.path("format").path("duration");
        if (!duration.isMissingNode() && !duration.asText().isBlank()) {
            metadata.setDurationSeconds(duration.asDouble());
        }
        return metadata;
    }
}
