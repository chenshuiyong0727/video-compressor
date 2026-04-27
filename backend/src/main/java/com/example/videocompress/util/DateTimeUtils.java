package com.example.videocompress.util;

public final class DateTimeUtils {

    private DateTimeUtils() {
    }

    public static String formatDuration(Double seconds) {
        if (seconds == null || seconds.isNaN() || seconds < 0) {
            return "-";
        }
        long total = Math.round(seconds);
        long hours = total / 3600;
        long minutes = (total % 3600) / 60;
        long secs = total % 60;
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        }
        return String.format("%02d:%02d", minutes, secs);
    }
}
