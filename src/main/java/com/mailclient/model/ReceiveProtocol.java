package com.mailclient.model;

/**
 * Các giao thức nhận thư được hỗ trợ.
 */
public enum ReceiveProtocol {
    POP3_SOCKET("POP3 (Socket TCP thuần)", 995),
    POP3_JAKARTA("POP3 (Jakarta Mail)", 995),
    IMAP_JAKARTA("IMAP (Jakarta Mail)", 993);

    private final String title;
    private final int defaultPort;

    ReceiveProtocol(String title, int defaultPort) {
        this.title = title;
        this.defaultPort = defaultPort;
    }

    public String getTitle() {
        return title;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    @Override
    public String toString() {
        return title;
    }
}
