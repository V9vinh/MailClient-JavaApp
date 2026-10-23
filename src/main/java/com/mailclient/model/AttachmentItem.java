package com.mailclient.model;

import java.io.File;

/**
 * Đại diện cho một tệp đính kèm trong email.
 */
public class AttachmentItem {
    private final File file;
    private final String fileName;
    private final long sizeBytes;

    public AttachmentItem(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File không được để null");
        }
        this.file = file;
        this.fileName = file.getName();
        this.sizeBytes = file.exists() ? file.length() : 0;
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return fileName;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public boolean isValid() {
        return file.exists() && file.isFile() && file.canRead();
    }

    public String getFormattedSize() {
        if (sizeBytes < 1024) {
            return sizeBytes + " B";
        } else if (sizeBytes < 1024 * 1024) {
            return String.format("%.1f KB", sizeBytes / 1024.0);
        } else {
            return String.format("%.2f MB", sizeBytes / (1024.0 * 1024.0));
        }
    }

    @Override
    public String toString() {
        return fileName + " (" + getFormattedSize() + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttachmentItem that = (AttachmentItem) o;
        return file.getAbsolutePath().equals(that.file.getAbsolutePath());
    }

    @Override
    public int hashCode() {
        return file.getAbsolutePath().hashCode();
    }
}
