package com.mailclient.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Đại diện cho một email nhận được từ máy chủ POP3 / IMAP.
 */
public class ReceivedEmail {
    private final int messageNumber;
    private final String from;
    private final String to;
    private final String subject;
    private final String date;
    private final String bodyText;
    private final List<ReceivedAttachment> attachments;
    private boolean read = false;

    public ReceivedEmail(int messageNumber, String from, String to, String subject, String date, String bodyText, List<ReceivedAttachment> attachments) {
        this.messageNumber = messageNumber;
        this.from = from != null ? from : "";
        this.to = to != null ? to : "";
        this.subject = subject != null ? subject : "(Không có tiêu đề)";
        this.date = date != null ? date : "";
        this.bodyText = bodyText != null ? bodyText : "";
        this.attachments = attachments != null ? new ArrayList<>(attachments) : new ArrayList<>();
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getDate() {
        return date;
    }

    public String getBodyText() {
        return bodyText;
    }

    public List<ReceivedAttachment> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }

    public boolean hasAttachments() {
        return !attachments.isEmpty();
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
