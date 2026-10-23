package com.mailclient;

import com.mailclient.config.AccountConfig;
import com.mailclient.config.ConfigManager;
import com.mailclient.model.*;
import com.mailclient.service.JakartaMailSmtpSender;
import com.mailclient.service.LogService;
import com.mailclient.service.SocketSmtpClient;
import com.mailclient.service.SmtpServiceFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class MailClientBusinessLogicTest {

    @Test
    @DisplayName("Kiểm tra AttachmentItem tính toán dung lượng và kiểm tra file")
    void testAttachmentItem() throws IOException {
        File tempFile = File.createTempFile("test_mail_attachment", ".txt");
        tempFile.deleteOnExit();
        Files.writeString(tempFile.toPath(), "Hello Email Protocol!");

        AttachmentItem item = new AttachmentItem(tempFile);
        assertTrue(item.isValid());
        assertEquals(tempFile.getName(), item.getFileName());
        assertTrue(item.getSizeBytes() > 0);
        assertTrue(item.getFormattedSize().contains("B"));

        EmailMessage message = new EmailMessage();
        message.addAttachment(item);
        assertEquals(1, message.getAttachments().size());
        assertEquals(item.getSizeBytes(), message.getTotalAttachmentSize());

        message.removeAttachment(item);
        assertEquals(0, message.getAttachments().size());
    }

    @Test
    @DisplayName("Kiểm tra AccountConfig validation và mật khẩu phiên")
    void testAccountConfigValidation() {
        AccountConfig config = new AccountConfig();
        assertFalse(config.isValid(), "Config ban đầu chưa có host/email phải không hợp lệ");

        config.setSmtpHost("smtp.gmail.com");
        config.setSmtpPort(587);
        config.setEmailAddress("test@domain.com");
        config.setUsername("testuser");
        config.setSessionPassword("sessionSecret123");
        config.setAuthRequired(true);

        assertTrue(config.isValid(), "Config đầy đủ thông tin phải hợp lệ");

        AccountConfig copy = config.copy();
        assertEquals("test@domain.com", copy.getEmailAddress());
        assertEquals("sessionSecret123", copy.getSessionPassword());
    }

    @Test
    @DisplayName("Kiểm tra LogService tự động che dấu thông tin nhạy cảm")
    void testLogServiceMasking() {
        String sensitive1 = "User login with password: secretPass123! to server";
        String masked1 = LogService.maskSensitive(sensitive1);
        assertFalse(masked1.contains("secretPass123!"));
        assertTrue(masked1.contains("******"));

        String sensitive2 = "AUTH LOGIN dXNlcm5hbWU=";
        String masked2 = LogService.maskSensitive(sensitive2);
        assertFalse(masked2.contains("dXNlcm5hbWU="));
        assertTrue(masked2.contains("******"));
    }

    @Test
    @DisplayName("Kiểm tra SmtpServiceFactory lựa chọn đúng động cơ theo cấu hình")
    void testSmtpServiceFactory() {
        AccountConfig socketConfig = new AccountConfig();
        socketConfig.setEngineType(SmtpEngineType.SOCKET);
        assertInstanceOf(SocketSmtpClient.class, SmtpServiceFactory.getService(socketConfig));

        AccountConfig jakartaConfig = new AccountConfig();
        jakartaConfig.setEngineType(SmtpEngineType.JAKARTA);
        assertInstanceOf(JakartaMailSmtpSender.class, SmtpServiceFactory.getService(jakartaConfig));
    }

    @Test
    @DisplayName("Kiểm tra LogService thread-safety và listener")
    void testLogServiceListener() {
        LogService logService = LogService.getInstance();
        AtomicBoolean received = new AtomicBoolean(false);

        logService.addListener(entry -> {
            if (entry != null && entry.getMessage().contains("Test Message 999")) {
                received.set(true);
            }
        });

        logService.info("UNIT_TEST", "Test Message 999");
        assertTrue(logService.getLogs().stream().anyMatch(e -> e.getMessage().contains("Test Message 999")));
    }
}
