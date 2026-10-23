package com.mailclient.model;

/**
 * Tệp đính kèm được tải về từ email nhận.
 */
public class ReceivedAttachment {
    private final String fileName;
    private final byte[] data;
    private final String contentType;

    public ReceivedAttachment(String fileName, byte[] data, String contentType) {
        this.fileName = sanitizeFileName(fileName);
        this.data = data != null ? data : new byte[0];
        this.contentType = contentType != null ? contentType : "application/octet-stream";
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getData() {
        return data;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSizeBytes() {
        return data.length;
    }

    public String getFormattedSize() {
        long size = data.length;
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        }
    }

    /**
     * Loại bỏ các ký tự nguy hiểm (path traversal) trong tên file theo yêu cầu spec.
     */
    public static String sanitizeFileName(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "attachment_" + System.currentTimeMillis() + ".dat";
        }
        String clean = raw.replaceAll("[/\\\\:*?\"<>|]", "_").trim();
        if (clean.startsWith(".")) {
            clean = "_" + clean;
        }
        return clean;
    }
}
