package com.mailclient;

import com.mailclient.config.AccountConfig;
import com.mailclient.model.ReceiveProtocol;
import com.mailclient.model.ReceivedAttachment;
import com.mailclient.model.ReceivedEmail;
import com.mailclient.service.JakartaMailReceiver;
import com.mailclient.service.MailReceiverServiceFactory;
import com.mailclient.service.SocketPop3Client;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MailReceiverTest {

    @Test
    @DisplayName("Kiểm tra ReceivedAttachment khử khuẩn tên file tránh path traversal và tính size")
    void testReceivedAttachment() {
        byte[] content = "File binary or text content".getBytes(StandardCharsets.UTF_8);
        String dirtyName = "../../etc/passwd.txt";
        ReceivedAttachment att = new ReceivedAttachment(dirtyName, content, "text/plain");

        assertFalse(att.getFileName().contains("/"), "Tên file không được chứa ký tự phân cách thư mục");
        assertFalse(att.getFileName().contains("\\"));
        assertEquals(content.length, att.getSizeBytes());
        assertTrue(att.getFormattedSize().contains("B"));
    }

    @Test
    @DisplayName("Kiểm tra ReceivedEmail lưu trữ thông tin thư và danh sách đính kèm")
    void testReceivedEmail() {
        ReceivedAttachment att = new ReceivedAttachment("document.pdf", new byte[1024 * 50], "application/pdf");
        ReceivedEmail email = new ReceivedEmail(
                1,
                "sender@domain.com",
                "receiver@domain.com",
                "Báo cáo tiến độ",
                "Sat, 24 Oct 2026 04:00:00 +0700",
                "Nội dung email chi tiết...",
                List.of(att)
        );

        assertEquals(1, email.getMessageNumber());
        assertEquals("Báo cáo tiến độ", email.getSubject());
        assertTrue(email.hasAttachments());
        assertEquals(1, email.getAttachments().size());
        assertEquals("document.pdf", email.getAttachments().get(0).getFileName());
    }

    @Test
    @DisplayName("Kiểm tra MailReceiverServiceFactory tạo đúng dịch vụ nhận thư theo cấu hình")
    void testReceiverFactory() {
        AccountConfig pop3SocketConfig = new AccountConfig();
        pop3SocketConfig.setReceiveProtocol(ReceiveProtocol.POP3_SOCKET);
        assertInstanceOf(SocketPop3Client.class, MailReceiverServiceFactory.getService(pop3SocketConfig));

        AccountConfig imapJakartaConfig = new AccountConfig();
        imapJakartaConfig.setReceiveProtocol(ReceiveProtocol.IMAP_JAKARTA);
        assertInstanceOf(JakartaMailReceiver.class, MailReceiverServiceFactory.getService(imapJakartaConfig));

        AccountConfig pop3JakartaConfig = new AccountConfig();
        pop3JakartaConfig.setReceiveProtocol(ReceiveProtocol.POP3_JAKARTA);
        assertInstanceOf(JakartaMailReceiver.class, MailReceiverServiceFactory.getService(pop3JakartaConfig));
    }

    @Test
    @DisplayName("Kiểm tra AccountConfig kiểm tra hợp lệ cấu hình nhận thư")
    void testAccountReceiveValidation() {
        AccountConfig config = new AccountConfig();
        assertFalse(config.isReceiveValid(), "Ban đầu chưa có host phải không hợp lệ");

        config.setReceiveHost("pop.gmail.com");
        config.setReceivePort(995);
        config.setUsername("myuser@gmail.com");
        config.setSessionPassword("mypassword");

        assertTrue(config.isReceiveValid(), "Đầy đủ host, port, username, sessionPassword phải hợp lệ");
    }
}
