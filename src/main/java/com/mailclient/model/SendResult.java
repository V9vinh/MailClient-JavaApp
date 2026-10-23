package com.mailclient.model;

import java.time.LocalDateTime;

/**
 * Kết quả sau khi thực hiện tác vụ gửi email hoặc kiểm tra kết nối.
 */
public class SendResult {
    private final boolean success;
    private final String message;
    private final String detailedTranscript;
    private final LocalDateTime timestamp;

    public SendResult(boolean success, String message, String detailedTranscript) {
        this.success = success;
        this.message = message;
        this.detailedTranscript = detailedTranscript;
        this.timestamp = LocalDateTime.now();
    }

    public static SendResult ok(String message, String transcript) {
        return new SendResult(true, message, transcript);
    }

    public static SendResult fail(String message, String transcript) {
        return new SendResult(false, message, transcript);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getDetailedTranscript() {
        return detailedTranscript;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
