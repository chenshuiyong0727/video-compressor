package com.example.videocompress;

import com.example.videocompress.config.VideoCompressProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(VideoCompressProperties.class)
public class VideoCompressApplication {

    public static void main(String[] args) {
        SpringApplication.run(VideoCompressApplication.class, args);
    }
}
