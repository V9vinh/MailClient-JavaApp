package com.mailclient.service;

import com.mailclient.model.LogEntry;

import javax.swing.SwingUtilities;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Dịch vụ ghi nhật ký tập trung cho ứng dụng Swing.
 * Tự động che dấu các dữ liệu nhạy cảm như mật khẩu hay chuỗi xác thực.
 * Đảm bảo thread-safe và gửi thông báo trên Swing Event Dispatch Thread (EDT).
 */
public class LogService {
    private static final LogService INSTANCE = new LogService();
    private final List<LogEntry> logs = new CopyOnWriteArrayList<>();
    private final List<Consumer<LogEntry>> listeners = new CopyOnWriteArrayList<>();

    private LogService() {
    }

    public static LogService getInstance() {
        return INSTANCE;
    }

    public List<LogEntry> getLogs() {
        return Collections.unmodifiableList(logs);
    }

    public void addListener(Consumer<LogEntry> listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(Consumer<LogEntry> listener) {
        listeners.remove(listener);
    }

    public void info(String stage, String message) {
        log(LogEntry.Level.INFO, stage, message);
    }

    public void success(String stage, String message) {
        log(LogEntry.Level.SUCCESS, stage, message);
    }

    public void warn(String stage, String message) {
        log(LogEntry.Level.WARN, stage, message);
    }

    public void error(String stage, String message) {
        log(LogEntry.Level.ERROR, stage, message);
    }

    public void log(LogEntry.Level level, String stage, String rawMessage) {
        String sanitizedMessage = maskSensitive(rawMessage);
        LogEntry entry = new LogEntry(level, stage, sanitizedMessage);
        logs.add(entry);

        if (SwingUtilities.isEventDispatchThread()) {
            notifyListeners(entry);
        } else {
            SwingUtilities.invokeLater(() -> notifyListeners(entry));
        }
    }

    public void clear() {
        logs.clear();
        if (SwingUtilities.isEventDispatchThread()) {
            notifyListeners(null);
        } else {
            SwingUtilities.invokeLater(() -> notifyListeners(null));
        }
    }

    private void notifyListeners(LogEntry entry) {
        for (Consumer<LogEntry> listener : listeners) {
            try {
                listener.accept(entry);
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Che các chuỗi nhạy cảm (mật khẩu, base64 auth).
     */
    public static String maskSensitive(String input) {
        if (input == null) return "";
        String sanitized = input.replaceAll("(?i)(password[:=]\\s*)([^\\s,]+)", "$1******");
        sanitized = sanitized.replaceAll("(?i)(AUTH LOGIN\\s+)([^\\s]+)", "$1******");
        return sanitized;
    }
}
