package com.example.videocompress.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class ProcessUtils {

    private ProcessUtils() {
    }

    public static Thread readAsync(InputStream inputStream, Consumer<String> lineConsumer) {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lineConsumer.accept(line);
                }
            } catch (IOException ex) {
                lineConsumer.accept("读取进程输出失败: " + ex.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    public static String firstLine(List<String> command, long timeoutSeconds) {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
            StringBuilder output = new StringBuilder();
            Thread reader = readAsync(process.getInputStream(), line -> {
                if (output.isEmpty()) {
                    output.append(line);
                }
            });
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
            }
            reader.join(TimeUnit.SECONDS.toMillis(1));
            return output.isEmpty() ? "" : output.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}
