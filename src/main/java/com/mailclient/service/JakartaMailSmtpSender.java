package com.mailclient.service;

import com.mailclient.config.AccountConfig;
import com.mailclient.model.AttachmentItem;
import com.mailclient.model.EmailMessage;
import com.mailclient.model.SecurityMode;
import com.mailclient.model.SendResult;
import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Triển khai dịch vụ gửi email chuẩn công nghiệp sử dụng thư viện Jakarta Mail.
 */
public class JakartaMailSmtpSender implements SmtpService {
    private final LogService logger = LogService.getInstance();

    @Override
    public SendResult sendEmail(AccountConfig account, EmailMessage message, Consumer<String> progressCallback) throws Exception {
        updateProgress(progressCallback, "1/5: Thiết lập phiên làm việc Jakarta Mail...");
        logger.info("Jakarta Mail", "Khởi tạo Session tới " + account.getSmtpHost() + ":" + account.getSmtpPort());

        Properties props = buildProperties(account);
        Session session = createSession(account, props);

        updateProgress(progressCallback, "2/5: Đóng gói cấu trúc MIME Message...");
        MimeMessage mimeMessage = new MimeMessage(session);

        // From
        if (account.getDisplayName() != null && !account.getDisplayName().trim().isEmpty()) {
            mimeMessage.setFrom(new InternetAddress(account.getEmailAddress(), account.getDisplayName(), StandardCharsets.UTF_8.name()));
        } else {
            mimeMessage.setFrom(new InternetAddress(account.getEmailAddress()));
        }

        // To
        for (String to : message.getTo()) {
            if (!to.trim().isEmpty()) {
                mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to.trim()));
            }
        }

        // Cc
        for (String cc : message.getCc()) {
            if (!cc.trim().isEmpty()) {
                mimeMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(cc.trim()));
            }
        }

        // Bcc
        for (String bcc : message.getBcc()) {
            if (!bcc.trim().isEmpty()) {
                mimeMessage.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc.trim()));
            }
        }

        mimeMessage.setSubject(message.getSubject(), StandardCharsets.UTF_8.name());
        mimeMessage.setSentDate(new Date());

        // Body & Attachments
        if (message.getAttachments().isEmpty()) {
            mimeMessage.setText(message.getBodyText(), StandardCharsets.UTF_8.name());
        } else {
            Multipart multipart = new MimeMultipart();

            // Text part
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(message.getBodyText(), StandardCharsets.UTF_8.name());
            multipart.addBodyPart(textPart);

            // Attachment parts
            for (AttachmentItem item : message.getAttachments()) {
                File file = item.getFile();
                if (!file.exists() || !file.canRead()) {
                    throw new IllegalArgumentException("Không thể đọc tệp đính kèm: " + file.getAbsolutePath());
                }
                MimeBodyPart attachPart = new MimeBodyPart();
                attachPart.attachFile(file);
                attachPart.setFileName(MimeUtility.encodeText(item.getFileName(), StandardCharsets.UTF_8.name(), null));
                multipart.addBodyPart(attachPart);
            }

            mimeMessage.setContent(multipart);
        }

        mimeMessage.saveChanges();

        updateProgress(progressCallback, "3/5: Kết nối và xác thực tới máy chủ SMTP...");
        logger.info("Jakarta Mail", "Mở kết nối Transport...");

        try (Transport transport = session.getTransport("smtp")) {
            transport.connect(
                    account.getSmtpHost(),
                    account.getSmtpPort(),
                    account.getUsername(),
                    account.getSessionPassword()
            );

            updateProgress(progressCallback, "4/5: Đang gửi dữ liệu email...");
            logger.info("Jakarta Mail", "Đang truyền tải Message qua Transport");
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());

            updateProgress(progressCallback, "5/5: Hoàn tất!");
            logger.success("Jakarta Mail", "Đã gửi thư thành công qua Jakarta Mail!");
            return SendResult.ok("Đã gửi email thành công qua Jakarta Mail!", "Gửi thành công qua Jakarta Mail Transport.");
        }
    }

    @Override
    public SendResult testConnection(AccountConfig account, Consumer<String> progressCallback) throws Exception {
        updateProgress(progressCallback, "Đang kiểm tra kết nối Jakarta Mail...");
        logger.info("Kiểm tra kết nối", "Thử kết nối Transport tới " + account.getSmtpHost() + ":" + account.getSmtpPort());

        Properties props = buildProperties(account);
        Session session = createSession(account, props);

        try (Transport transport = session.getTransport("smtp")) {
            transport.connect(
                    account.getSmtpHost(),
                    account.getSmtpPort(),
                    account.getUsername(),
                    account.getSessionPassword()
            );
            updateProgress(progressCallback, "Kết nối và xác thực thành công!");
            logger.success("Kiểm tra kết nối", "Kết nối và xác thực Jakarta Mail thành công!");
            return SendResult.ok("Kết nối và xác thực Jakarta Mail thành công!", "Transport connect OK");
        }
    }

    private Properties buildProperties(AccountConfig account) {
        Properties props = new Properties();
        props.put("mail.smtp.host", account.getSmtpHost());
        props.put("mail.smtp.port", String.valueOf(account.getSmtpPort()));
        props.put("mail.smtp.auth", String.valueOf(account.isAuthRequired()));
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "15000");
        props.put("mail.smtp.writetimeout", "15000");

        if (account.getSecurityMode() == SecurityMode.STARTTLS) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        } else if (account.getSecurityMode() == SecurityMode.SSL_TLS) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", String.valueOf(account.getSmtpPort()));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }

        return props;
    }

    private Session createSession(AccountConfig account, Properties props) {
        if (account.isAuthRequired()) {
            return Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(account.getUsername(), account.getSessionPassword());
                }
            });
        }
        return Session.getInstance(props);
    }

    private void updateProgress(Consumer<String> progress, String msg) {
        if (progress != null) {
            progress.accept(msg);
        }
    }
}
