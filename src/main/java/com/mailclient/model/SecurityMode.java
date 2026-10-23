package com.mailclient.model;

/**
 * Các chế độ bảo mật đường truyền SMTP được hỗ trợ.
 */
public enum SecurityMode {
    STARTTLS("STARTTLS (Cổng 587)", 587),
    SSL_TLS("SSL/TLS (Cổng 465)", 465),
    NONE("Không mã hóa (Cổng 25)", 25);

    private final String displayName;
    private final int defaultPort;

    SecurityMode(String displayName, int defaultPort) {
        this.displayName = displayName;
        this.defaultPort = defaultPort;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
