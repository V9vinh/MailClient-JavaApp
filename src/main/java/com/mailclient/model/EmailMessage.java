package com.mailclient.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Đại diện cho dữ liệu của một email cần gửi.
 */
public class EmailMessage {
    private String from;
    private List<String> to = new ArrayList<>();
    private List<String> cc = new ArrayList<>();
    private List<String> bcc = new ArrayList<>();
    private String subject = "";
    private String bodyText = "";
    private List<AttachmentItem> attachments = new ArrayList<>();

    public EmailMessage() {
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public List<String> getTo() {
        return to;
    }

    public void setTo(List<String> to) {
        this.to = to != null ? new ArrayList<>(to) : new ArrayList<>();
    }

    public List<String> getCc() {
        return cc;
    }

    public void setCc(List<String> cc) {
        this.cc = cc != null ? new ArrayList<>(cc) : new ArrayList<>();
    }

    public List<String> getBcc() {
        return bcc;
    }

    public void setBcc(List<String> bcc) {
        this.bcc = bcc != null ? new ArrayList<>(bcc) : new ArrayList<>();
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject != null ? subject : "";
    }

    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText != null ? bodyText : "";
    }

    public List<AttachmentItem> getAttachments() {
        return Collections.unmodifiableList(attachments);
    }

    public void addAttachment(AttachmentItem item) {
        if (item != null && !attachments.contains(item)) {
            attachments.add(item);
        }
    }

    public void removeAttachment(AttachmentItem item) {
        attachments.remove(item);
    }

    public void clearAttachments() {
        attachments.clear();
    }

    public long getTotalAttachmentSize() {
        return attachments.stream().mapToLong(AttachmentItem::getSizeBytes).sum();
    }
}
