package com.mailclient.service;

import com.mailclient.config.AccountConfig;
import com.mailclient.model.AttachmentItem;
import com.mailclient.model.EmailMessage;
import com.mailclient.model.SecurityMode;
import com.mailclient.model.SendResult;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

/**
 * Triển khai giao thức SMTP thuần qua Socket và Stream mạng.
 * Tuân thủ chuẩn RFC 5321, RFC 2045 (MIME), RFC 2047, theo sát bài giảng môn học.
 */
public class SocketSmtpClient implements SmtpService {
    private static final int TIMEOUT_MS = 20000;
    private final LogService logger = LogService.getInstance();

    @Override
    public SendResult sendEmail(AccountConfig account, EmailMessage message, Consumer<String> progressCallback) throws Exception {
        return executeSmtp(account, message, progressCallback, false);
    }

    @Override
    public SendResult testConnection(AccountConfig account, Consumer<String> progressCallback) throws Exception {
        return executeSmtp(account, null, progressCallback, true);
    }

    private SendResult executeSmtp(AccountConfig account, EmailMessage message, Consumer<String> progress, boolean isTestOnly) throws Exception {
        StringBuilder transcript = new StringBuilder();
        Socket rawSocket = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;

        updateProgress(progress, "1/5: Đang kết nối tới máy chủ " + account.getSmtpHost() + ":" + account.getSmtpPort() + "...");
        logger.info("Kết nối", "Đang mở kết nối TCP tới " + account.getSmtpHost() + ":" + account.getSmtpPort());

        try {
            boolean isDirectSsl = (account.getSecurityMode() == SecurityMode.SSL_TLS);

            if (isDirectSsl) {
                SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket sslSocket = (SSLSocket) ssf.createSocket();
                sslSocket.connect(new InetSocketAddress(account.getSmtpHost(), account.getSmtpPort()), TIMEOUT_MS);
                sslSocket.setSoTimeout(TIMEOUT_MS);
                sslSocket.startHandshake();
                rawSocket = sslSocket;
                logger.info("Bảo mật", "Đã thiết lập kết nối SSL/TLS trực tiếp (SMTPS)");
            } else {
                rawSocket = new Socket();
                rawSocket.connect(new InetSocketAddress(account.getSmtpHost(), account.getSmtpPort()), TIMEOUT_MS);
                rawSocket.setSoTimeout(TIMEOUT_MS);
            }

            reader = new BufferedReader(new InputStreamReader(rawSocket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(rawSocket.getOutputStream(), StandardCharsets.UTF_8));

            // 1. Đọc lời chào từ máy chủ (Greeting 220)
            SmtpReply greeting = readReply(reader, transcript);
            if (greeting.code != 220) {
                throw new IOException("Máy chủ từ chối kết nối (Mã: " + greeting.code + "): " + greeting.message);
            }
            logger.info("Máy chủ", "Greeting: " + greeting.message.trim());

            // 2. Gửi EHLO ban đầu
            updateProgress(progress, "2/5: Gửi lệnh EHLO tới máy chủ...");
            sendCommand(writer, "EHLO client.local", transcript);
            SmtpReply ehloReply = readReply(reader, transcript);
            if (ehloReply.code != 250) {
                // Thử fallback HELO nếu máy chủ không hỗ trợ EHLO
                sendCommand(writer, "HELO client.local", transcript);
                ehloReply = readReply(reader, transcript);
                if (ehloReply.code != 250) {
                    throw new IOException("Lỗi chào hỏi EHLO/HELO: " + ehloReply.message);
                }
            }

            // 3. Xử lý STARTTLS nếu được chọn
            if (account.getSecurityMode() == SecurityMode.STARTTLS) {
                updateProgress(progress, "Bắt tay bảo mật STARTTLS...");
                logger.info("Bảo mật", "Gửi lệnh STARTTLS để nâng cấp mã hóa");
                sendCommand(writer, "STARTTLS", transcript);
                SmtpReply starttlsReply = readReply(reader, transcript);
                if (starttlsReply.code != 220) {
                    throw new IOException("Máy chủ không chấp nhận STARTTLS (Mã " + starttlsReply.code + "): " + starttlsReply.message);
                }

                // Nâng cấp Socket thường lên SSLSocket
                SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket sslSocket = (SSLSocket) ssf.createSocket(
                        rawSocket,
                        account.getSmtpHost(),
                        account.getSmtpPort(),
                        true
                );
                sslSocket.setSoTimeout(TIMEOUT_MS);
                sslSocket.startHandshake();

                rawSocket = sslSocket;
                reader = new BufferedReader(new InputStreamReader(rawSocket.getInputStream(), StandardCharsets.UTF_8));
                writer = new BufferedWriter(new OutputStreamWriter(rawSocket.getOutputStream(), StandardCharsets.UTF_8));
                logger.info("Bảo mật", "Nâng cấp kênh TLS thành công!");

                // QUY TẮC BẮT BUỘC RFC: Phải gửi lại EHLO sau khi nâng cấp TLS
                sendCommand(writer, "EHLO client.local", transcript);
                ehloReply = readReply(reader, transcript);
                if (ehloReply.code != 250) {
                    throw new IOException("Lỗi EHLO sau STARTTLS: " + ehloReply.message);
                }
            }

            // 4. Xác thực AUTH LOGIN nếu yêu cầu
            if (account.isAuthRequired()) {
                updateProgress(progress, "3/5: Xác thực tài khoản với máy chủ...");
                logger.info("Xác thực", "Bắt đầu xác thực AUTH LOGIN");
                sendCommand(writer, "AUTH LOGIN", transcript);
                SmtpReply authReply = readReply(reader, transcript);
                if (authReply.code != 334) {
                    throw new IOException("Máy chủ không hỗ trợ AUTH LOGIN (Mã " + authReply.code + "): " + authReply.message);
                }

                // Gửi Username (Base64)
                String userB64 = Base64.getEncoder().encodeToString(account.getUsername().getBytes(StandardCharsets.UTF_8));
                sendSensitiveCommand(writer, userB64, transcript, "[USERNAME_BASE64]");
                SmtpReply userReply = readReply(reader, transcript);
                if (userReply.code != 334) {
                    throw new IOException("Tên đăng nhập không được chấp nhận: " + userReply.message);
                }

                // Gửi Password (Base64)
                String passB64 = Base64.getEncoder().encodeToString(account.getSessionPassword().getBytes(StandardCharsets.UTF_8));
                sendSensitiveCommand(writer, passB64, transcript, "[PASSWORD_BASE64]");
                SmtpReply passReply = readReply(reader, transcript);
                if (passReply.code != 235) {
                    throw new IOException("Xác thực mật khẩu thất bại (Mã " + passReply.code + "): " + passReply.message);
                }
                logger.success("Xác thực", "Xác thực tài khoản thành công!");
            }

            // Nếu chỉ là tác vụ kiểm tra kết nối, dừng tại đây và đóng phiên
            if (isTestOnly) {
                sendCommand(writer, "QUIT", transcript);
                readReply(reader, transcript);
                updateProgress(progress, "Kiểm tra kết nối và xác thực thành công!");
                logger.success("Kết nối", "Kiểm tra kết nối SMTP hoàn tất thành công.");
                return SendResult.ok("Kết nối và xác thực SMTP thành công!", transcript.toString());
            }

            // 5. Giao dịch gửi thư (MAIL FROM -> RCPT TO -> DATA)
            updateProgress(progress, "4/5: Đang gửi dữ liệu email và tệp đính kèm...");
            String senderEmail = account.getEmailAddress();
            sendCommand(writer, "MAIL FROM:<" + senderEmail + ">", transcript);
            SmtpReply mailFromReply = readReply(reader, transcript);
            if (mailFromReply.code != 250) {
                throw new IOException("Máy chủ từ chối người gửi (" + senderEmail + "): " + mailFromReply.message);
            }

            // Thu thập tất cả người nhận (To, Cc, Bcc)
            List<String> allRecipients = new ArrayList<>();
            allRecipients.addAll(message.getTo());
            allRecipients.addAll(message.getCc());
            allRecipients.addAll(message.getBcc());

            if (allRecipients.isEmpty()) {
                throw new IllegalArgumentException("Không có người nhận nào trong thư!");
            }

            for (String recipient : allRecipients) {
                String cleanRcpt = recipient.trim();
                if (cleanRcpt.isEmpty()) continue;
                sendCommand(writer, "RCPT TO:<" + cleanRcpt + ">", transcript);
                SmtpReply rcptReply = readReply(reader, transcript);
                if (rcptReply.code != 250 && rcptReply.code != 251) {
                    throw new IOException("Máy chủ từ chối người nhận (" + cleanRcpt + "): " + rcptReply.message);
                }
            }

            // 6. Gửi lệnh DATA
            sendCommand(writer, "DATA", transcript);
            SmtpReply dataReply = readReply(reader, transcript);
            if (dataReply.code != 354) {
                throw new IOException("Máy chủ từ chối nhận nội dung DATA (Mã " + dataReply.code + "): " + dataReply.message);
            }

            // 7. Gửi nội dung MIME thực tế với Dot-Stuffing
            logger.info("Gửi thư", "Bắt đầu truyền nội dung MIME và đính kèm");
            writeMimeStream(writer, account, message);

            // Kết thúc khối DATA bằng <CRLF>.<CRLF>
            writer.write("\r\n.\r\n");
            writer.flush();
            transcript.append("<CRLF>.<CRLF>\n");

            // Đọc phản hồi chấp nhận thư của máy chủ
            SmtpReply sendAcceptReply = readReply(reader, transcript);
            if (sendAcceptReply.code != 250) {
                throw new IOException("Máy chủ từ chối nhận thư sau DATA (Mã " + sendAcceptReply.code + "): " + sendAcceptReply.message);
            }

            // 8. Đóng phiên QUIT
            updateProgress(progress, "5/5: Hoàn tất!");
            sendCommand(writer, "QUIT", transcript);
            readReply(reader, transcript);

            logger.success("Gửi thư", "Máy chủ SMTP đã chấp nhận thư thành công: " + sendAcceptReply.message.trim());
            return SendResult.ok("Máy chủ đã chấp nhận thư thành công!", transcript.toString());

        } finally {
            closeQuietly(reader);
            closeQuietly(writer);
            closeQuietly(rawSocket);
        }
    }

    /**
     * Ghi toàn bộ nội dung MIME chuẩn (Headers, Body UTF-8, Attachments Base64) với Dot-Stuffing.
     */
    private void writeMimeStream(BufferedWriter writer, AccountConfig account, EmailMessage message) throws IOException {
        String boundary = "----=_Part_" + UUID.randomUUID().toString().replace("-", "");
        boolean hasAttachments = !message.getAttachments().isEmpty();

        // Headers
        writeHeader(writer, "Date", ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        String fromHeader = encodeAddress(account.getDisplayName(), account.getEmailAddress());
        writeHeader(writer, "From", fromHeader);
        writeHeader(writer, "To", String.join(", ", message.getTo()));
        if (!message.getCc().isEmpty()) {
            writeHeader(writer, "Cc", String.join(", ", message.getCc()));
        }
        writeHeader(writer, "Subject", encodeRfc2047(message.getSubject()));
        writeHeader(writer, "MIME-Version", "1.0");

        if (hasAttachments) {
            writeHeader(writer, "Content-Type", "multipart/mixed; boundary=\"" + boundary + "\"");
            writer.write("\r\n");

            // Phần 1: Nội dung văn bản Plain text
            writer.write("--" + boundary + "\r\n");
            writeHeader(writer, "Content-Type", "text/plain; charset=UTF-8; format=flowed");
            writeHeader(writer, "Content-Transfer-Encoding", "8bit");
            writer.write("\r\n");
            writeBodyWithDotStuffing(writer, message.getBodyText());
            writer.write("\r\n");

            // Phần 2+: Các tệp đính kèm Base64
            for (AttachmentItem item : message.getAttachments()) {
                File file = item.getFile();
                if (!file.exists() || !file.canRead()) {
                    throw new FileNotFoundException("Tệp đính kèm không tồn tại hoặc không thể đọc: " + file.getAbsolutePath());
                }

                writer.write("--" + boundary + "\r\n");
                String encodedFileName = encodeRfc2047(item.getFileName());
                writeHeader(writer, "Content-Type", "application/octet-stream; name=\"" + encodedFileName + "\"");
                writeHeader(writer, "Content-Transfer-Encoding", "base64");
                writeHeader(writer, "Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; size=" + item.getSizeBytes());
                writer.write("\r\n");

                byte[] fileBytes = Files.readAllBytes(file.toPath());
                String base64Content = Base64.getMimeEncoder(76, new byte[]{'\r', '\n'}).encodeToString(fileBytes);
                writer.write(base64Content);
                writer.write("\r\n");
            }

            // Đóng Multipart
            writer.write("--" + boundary + "--\r\n");
        } else {
            writeHeader(writer, "Content-Type", "text/plain; charset=UTF-8; format=flowed");
            writeHeader(writer, "Content-Transfer-Encoding", "8bit");
            writer.write("\r\n");
            writeBodyWithDotStuffing(writer, message.getBodyText());
        }
    }

    private void writeHeader(BufferedWriter writer, String name, String value) throws IOException {
        writer.write(name + ": " + value + "\r\n");
    }

    /**
     * Áp dụng quy tắc Dot-Stuffing (RFC 5321 mục 4.5.2):
     * Nếu một dòng văn bản bắt đầu bằng dấu chấm '.', cần thêm một dấu chấm nữa '..' để tránh hiểu nhầm là kết thúc DATA.
     */
    private void writeBodyWithDotStuffing(BufferedWriter writer, String body) throws IOException {
        if (body == null || body.isEmpty()) return;
        BufferedReader lineReader = new BufferedReader(new StringReader(body));
        String line;
        while ((line = lineReader.readLine()) != null) {
            if (line.startsWith(".")) {
                writer.write(".");
            }
            writer.write(line);
            writer.write("\r\n");
        }
    }

    private void sendCommand(BufferedWriter writer, String cmd, StringBuilder transcript) throws IOException {
        writer.write(cmd + "\r\n");
        writer.flush();
        transcript.append("C: ").append(cmd).append("\n");
    }

    private void sendSensitiveCommand(BufferedWriter writer, String secret, StringBuilder transcript, String maskLabel) throws IOException {
        writer.write(secret + "\r\n");
        writer.flush();
        transcript.append("C: ").append(maskLabel).append("\n");
    }

    /**
     * Đọc phản hồi nhiều dòng từ máy chủ SMTP theo chuẩn RFC (dấu '-' ở vị trí thứ 4 biểu thị dòng tiếp theo).
     */
    private SmtpReply readReply(BufferedReader reader, StringBuilder transcript) throws IOException {
        StringBuilder fullReply = new StringBuilder();
        int replyCode = 0;
        String line;

        while ((line = reader.readLine()) != null) {
            transcript.append("S: ").append(line).append("\n");
            fullReply.append(line).append("\n");

            if (line.length() >= 3) {
                try {
                    replyCode = Integer.parseInt(line.substring(0, 3));
                } catch (NumberFormatException ignored) {
                }
            }

            // Nếu dòng chỉ có 3 ký tự hoặc ký tự thứ 4 là khoảng trắng -> dòng cuối của phản hồi
            if (line.length() == 3 || line.charAt(3) == ' ') {
                break;
            }
        }

        if (line == null && replyCode == 0) {
            throw new IOException("Mất kết nối đột ngột từ máy chủ SMTP.");
        }

        return new SmtpReply(replyCode, fullReply.toString());
    }

    private String encodeAddress(String displayName, String email) {
        if (displayName != null && !displayName.trim().isEmpty()) {
            return encodeRfc2047(displayName.trim()) + " <" + email.trim() + ">";
        }
        return "<" + email.trim() + ">";
    }

    /**
     * Mã hóa tiêu đề hoặc chuỗi theo chuẩn RFC 2047 =?UTF-8?B?...?= nếu chứa ký tự non-ASCII.
     */
    private String encodeRfc2047(String text) {
        if (text == null) return "";
        boolean isAscii = true;
        for (char c : text.toCharArray()) {
            if (c > 127) {
                isAscii = false;
                break;
            }
        }
        if (isAscii) {
            return text;
        }
        String b64 = Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
        return "=?UTF-8?B?" + b64 + "?=";
    }

    private void updateProgress(Consumer<String> progress, String msg) {
        if (progress != null) {
            progress.accept(msg);
        }
    }

    private void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static class SmtpReply {
        final int code;
        final String message;

        SmtpReply(int code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
