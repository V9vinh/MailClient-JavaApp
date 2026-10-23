package com.mailclient.service;

import com.mailclient.config.AccountConfig;
import com.mailclient.model.ReceiveProtocol;
import com.mailclient.model.ReceivedAttachment;
import com.mailclient.model.ReceivedEmail;
import com.mailclient.model.SecurityMode;
import com.mailclient.model.SendResult;
import jakarta.mail.*;
import jakarta.mail.internet.MimeUtility;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

/**
 * Triển khai dịch vụ nhận thư qua Jakarta Mail hỗ trợ cả POP3 và IMAP chuẩn công nghiệp.
 */
public class JakartaMailReceiver implements MailReceiverService {
    private final LogService logger = LogService.getInstance();

    @Override
    public List<ReceivedEmail> fetchEmails(AccountConfig account, int maxCount, Consumer<String> progressCallback) throws Exception {
        List<ReceivedEmail> resultList = new ArrayList<>();
        String protocol = getProtocolString(account);

        updateProgress(progressCallback, "1/4: Đang kết nối tới máy chủ " + account.getReceiveHost() + " qua " + protocol.toUpperCase() + "...");
        logger.info("Jakarta Mail Receiver", "Mở kết nối Store tới " + account.getReceiveHost() + ":" + account.getReceivePort());

        Properties props = new Properties();
        props.put("mail." + protocol + ".connectiontimeout", "15000");
        props.put("mail." + protocol + ".timeout", "15000");

        if (account.getReceiveSecurityMode() == SecurityMode.SSL_TLS) {
            props.put("mail." + protocol + ".ssl.enable", "true");
        }

        Session session = Session.getInstance(props);

        try (Store store = session.getStore(protocol)) {
            store.connect(
                    account.getReceiveHost(),
                    account.getReceivePort(),
                    account.getUsername(),
                    account.getSessionPassword()
            );

            updateProgress(progressCallback, "2/4: Đang mở Hộp thư đến (INBOX)...");
            try (Folder inbox = store.getFolder("INBOX")) {
                inbox.open(Folder.READ_ONLY);

                int totalMessages = inbox.getMessageCount();
                logger.info("Jakarta Mail Receiver", "Tổng số thư trong INBOX: " + totalMessages);

                if (totalMessages > 0) {
                    int start = Math.max(1, totalMessages - maxCount + 1);
                    updateProgress(progressCallback, "3/4: Đang nạp " + (totalMessages - start + 1) + " thư...");

                    Message[] messages = inbox.getMessages(start, totalMessages);

                    // Đảo ngược để thư mới nhất lên đầu
                    for (int i = messages.length - 1; i >= 0; i--) {
                        Message msg = messages[i];
                        updateProgress(progressCallback, "Đang đọc thư: " + msg.getSubject() + "...");

                        String from = msg.getFrom() != null && msg.getFrom().length > 0 ?
                                MimeUtility.decodeText(msg.getFrom()[0].toString()) : "";
                        String to = msg.getAllRecipients() != null && msg.getAllRecipients().length > 0 ?
                                MimeUtility.decodeText(msg.getAllRecipients()[0].toString()) : "";
                        String subject = msg.getSubject() != null ? MimeUtility.decodeText(msg.getSubject()) : "(Không có tiêu đề)";
                        String date = msg.getSentDate() != null ? msg.getSentDate().toString() : "";

                        StringBuilder bodyText = new StringBuilder();
                        List<ReceivedAttachment> attachments = new ArrayList<>();

                        extractContent(msg, bodyText, attachments);

                        ReceivedEmail email = new ReceivedEmail(
                                msg.getMessageNumber(),
                                from,
                                to,
                                subject,
                                date,
                                bodyText.toString().trim(),
                                attachments
                        );
                        resultList.add(email);
                    }
                }
            }

            updateProgress(progressCallback, "4/4: Đã tải xong " + resultList.size() + " thư.");
            logger.success("Jakarta Mail Receiver", "Đồng bộ INBOX thành công qua " + protocol.toUpperCase());
            return resultList;
        }
    }

    @Override
    public SendResult testReceiveConnection(AccountConfig account, Consumer<String> progressCallback) throws Exception {
        String protocol = getProtocolString(account);
        updateProgress(progressCallback, "Đang thử kết nối Store " + protocol.toUpperCase() + "...");

        Properties props = new Properties();
        props.put("mail." + protocol + ".connectiontimeout", "15000");
        props.put("mail." + protocol + ".timeout", "15000");

        if (account.getReceiveSecurityMode() == SecurityMode.SSL_TLS) {
            props.put("mail." + protocol + ".ssl.enable", "true");
        }

        Session session = Session.getInstance(props);

        try (Store store = session.getStore(protocol)) {
            store.connect(
                    account.getReceiveHost(),
                    account.getReceivePort(),
                    account.getUsername(),
                    account.getSessionPassword()
            );

            try (Folder inbox = store.getFolder("INBOX")) {
                inbox.open(Folder.READ_ONLY);
                int count = inbox.getMessageCount();
                updateProgress(progressCallback, "Kết nối thành công! Hộp thư có " + count + " thư.");
                logger.success("Kiểm tra nhận thư", "Kết nối và mở INBOX thành công qua " + protocol.toUpperCase());
                return SendResult.ok("Kết nối thành công qua " + protocol.toUpperCase() + "!", "INBOX message count: " + count);
            }
        }
    }

    private void extractContent(Part part, StringBuilder bodyText, List<ReceivedAttachment> attachments) throws Exception {
        if (part.isMimeType("text/plain")) {
            bodyText.append(part.getContent().toString()).append("\n");
        } else if (part.isMimeType("text/html")) {
            if (bodyText.length() == 0) {
                // Chuyển HTML sang plain text đơn giản, an toàn
                String html = part.getContent().toString();
                String plain = html.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", " ")
                        .replaceAll("&nbsp;", " ")
                        .replaceAll("&amp;", "&")
                        .replaceAll("&lt;", "<")
                        .replaceAll("&gt;", ">");
                bodyText.append(plain.trim()).append("\n");
            }
        } else if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; i++) {
                extractContent(mp.getBodyPart(i), bodyText, attachments);
            }
        } else if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) || part.getFileName() != null) {
            String fileName = part.getFileName() != null ? MimeUtility.decodeText(part.getFileName()) : "attachment.dat";
            byte[] bytes = toByteArray(part.getInputStream());
            attachments.add(new ReceivedAttachment(fileName, bytes, part.getContentType()));
        }
    }

    private byte[] toByteArray(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        return out.toByteArray();
    }

    private String getProtocolString(AccountConfig account) {
        boolean isSsl = account.getReceiveSecurityMode() == SecurityMode.SSL_TLS;
        if (account.getReceiveProtocol() == ReceiveProtocol.IMAP_JAKARTA) {
            return isSsl ? "imaps" : "imap";
        } else {
            return isSsl ? "pop3s" : "pop3";
        }
    }

    private void updateProgress(Consumer<String> progress, String msg) {
        if (progress != null) {
            progress.accept(msg);
        }
    }
}
