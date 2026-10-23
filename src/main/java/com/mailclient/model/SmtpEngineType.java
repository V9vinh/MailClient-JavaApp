package com.mailclient.model;

/**
 * Phương thức thực thi giao thức gửi thư SMTP.
 */
public enum SmtpEngineType {
    SOCKET("Socket TCP thuần"),
    JAKARTA("Thư viện Jakarta Mail");

    private final String title;

    SmtpEngineType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return title;
    }
}
