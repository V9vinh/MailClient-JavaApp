package com.mailclient.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mục nhật ký hoạt động mạng hoặc giao diện.
 */
public class LogEntry {
    public enum Level {
        INFO, SUCCESS, WARN, ERROR
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final LocalDateTime timestamp;
    private final Level level;
    private final String stage;
    private final String message;

    public LogEntry(Level level, String stage, String message) {
        this.timestamp = LocalDateTime.now();
        this.level = level;
        this.stage = stage != null ? stage : "";
        this.message = message != null ? message : "";
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTime() {
        return timestamp.format(FORMATTER);
    }

    public Level getLevel() {
        return level;
    }

    public String getStage() {
        return stage;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("[%s] [%s] %s: %s", getFormattedTime(), level, stage, message);
    }
}
