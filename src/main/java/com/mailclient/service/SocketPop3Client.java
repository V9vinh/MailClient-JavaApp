package com.mailclient.service;

import com.mailclient.config.AccountConfig;
import com.mailclient.model.ReceivedAttachment;
import com.mailclient.model.ReceivedEmail;
import com.mailclient.model.SecurityMode;
import com.mailclient.model.SendResult;
import jakarta.mail.internet.MimeUtility;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

/**
 * Triển khai giao thức nhận thư POP3 thuần bằng Socket và Stream mạng.
 * Tuân thủ chuẩn RFC 1939 theo sát Slide bài giảng 17-18.
 */
public class SocketPop3Client implements MailReceiverService {
    private static final int TIMEOUT_MS = 20000;
    private final LogService logger = LogService.getInstance();

    @Override
    public List<ReceivedEmail> fetchEmails(AccountConfig account, int maxCount, Consumer<String> progressCallback) throws Exception {
        List<ReceivedEmail> resultList = new ArrayList<>();
        Socket socket = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;

        updateProgress(progressCallback, "1/4: Đang kết nối tới máy chủ POP3 " + account.getReceiveHost() + ":" + account.getReceivePort() + "...");
        logger.info("POP3 Socket", "Mở kết nối tới " + account.getReceiveHost() + ":" + account.getReceivePort());

        try {
            socket = connectSocket(account);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            // 1. Đọc Greeting (+OK)
            String greeting = readResponse(reader);
            if (!greeting.startsWith("+OK")) {
                throw new IOException("Máy chủ POP3 từ chối kết nối: " + greeting);
            }

            // 2. Xác thực USER & PASS
            updateProgress(progressCallback, "2/4: Xác thực tài khoản POP3...");
            sendCommand(writer, "USER " + account.getUsername());
            String userResp = readResponse(reader);
            if (!userResp.startsWith("+OK")) {
                throw new IOException("Tên đăng nhập POP3 không được chấp nhận: " + userResp);
            }

            sendSensitiveCommand(writer, "PASS " + account.getSessionPassword());
            String passResp = readResponse(reader);
            if (!passResp.startsWith("+OK")) {
                throw new IOException("Xác thực mật khẩu POP3 thất bại: " + passResp);
            }
            logger.success("POP3 Socket", "Xác thực tài khoản thành công!");

            // 3. Lấy thông tin thống kê hộp thư (STAT)
            updateProgress(progressCallback, "3/4: Đang đọc số lượng thư (STAT)...");
            sendCommand(writer, "STAT");
            String statResp = readResponse(reader);
            if (!statResp.startsWith("+OK")) {
                throw new IOException("Không thể lấy trạng thái STAT: " + statResp);
            }

            int totalCount = parseStatCount(statResp);
            logger.info("POP3 Socket", "Hộp thư có tổng cộng: " + totalCount + " thư.");

            if (totalCount > 0) {
                int start = Math.max(1, totalCount - maxCount + 1);
                updateProgress(progressCallback, "4/4: Đang tải " + (totalCount - start + 1) + " thư mới nhất...");

                // Đọc ngược từ thư mới nhất tới cũ hơn
                for (int msgNum = totalCount; msgNum >= start; msgNum--) {
                    updateProgress(progressCallback, "Đang tải thư #" + msgNum + "/" + totalCount + "...");
                    sendCommand(writer, "RETR " + msgNum);
                    String retrResp = readResponse(reader);
                    if (retrResp.startsWith("+OK")) {
                        String rawMessage = readMultiline(reader);
                        ReceivedEmail email = parseRfc822(msgNum, rawMessage);
                        resultList.add(email);
                    }
                }
            }

            // 4. Đóng phiên QUIT
            sendCommand(writer, "QUIT");
            readResponse(reader);
            updateProgress(progressCallback, "Đã đồng bộ xong " + resultList.size() + " thư.");
            logger.success("POP3 Socket", "Đồng bộ hộp thư hoàn tất thành công.");
            return resultList;

        } finally {
            closeQuietly(reader);
            closeQuietly(writer);
            closeQuietly(socket);
        }
    }

    @Override
    public SendResult testReceiveConnection(AccountConfig account, Consumer<String> progressCallback) throws Exception {
        Socket socket = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;

        updateProgress(progressCallback, "Đang kiểm tra kết nối POP3 tới " + account.getReceiveHost() + "...");
        logger.info("POP3 Test", "Thử kết nối tới " + account.getReceiveHost() + ":" + account.getReceivePort());

        try {
            socket = connectSocket(account);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            String greeting = readResponse(reader);
            if (!greeting.startsWith("+OK")) {
                throw new IOException("Máy chủ từ chối: " + greeting);
            }

            sendCommand(writer, "USER " + account.getUsername());
            String userResp = readResponse(reader);
            if (!userResp.startsWith("+OK")) {
                throw new IOException("Tên đăng nhập không đúng: " + userResp);
            }

            sendSensitiveCommand(writer, "PASS " + account.getSessionPassword());
            String passResp = readResponse(reader);
            if (!passResp.startsWith("+OK")) {
                throw new IOException("Mật khẩu không đúng: " + passResp);
            }

            sendCommand(writer, "STAT");
            String stat = readResponse(reader);

            sendCommand(writer, "QUIT");
            readResponse(reader);

            updateProgress(progressCallback, "Kết nối và xác thực POP3 thành công!");
            logger.success("POP3 Test", "Kiểm tra kết nối POP3 thành công: " + stat);
            return SendResult.ok("Kết nối và xác thực POP3 thành công!", stat);

        } finally {
            closeQuietly(reader);
            closeQuietly(writer);
            closeQuietly(socket);
        }
    }

    private Socket connectSocket(AccountConfig account) throws IOException {
        String host = account.getReceiveHost();
        int port = account.getReceivePort();
        boolean isSsl = (account.getReceiveSecurityMode() == SecurityMode.SSL_TLS) || (port == 995);

        if (isSsl) {
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslSocket = (SSLSocket) ssf.createSocket();
            sslSocket.connect(new InetSocketAddress(host, port), TIMEOUT_MS);
            sslSocket.setSoTimeout(TIMEOUT_MS);
            sslSocket.startHandshake();
            return sslSocket;
        } else {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), TIMEOUT_MS);
            socket.setSoTimeout(TIMEOUT_MS);
            return socket;
        }
    }

    private void sendCommand(BufferedWriter writer, String cmd) throws IOException {
        writer.write(cmd + "\r\n");
        writer.flush();
    }

    private void sendSensitiveCommand(BufferedWriter writer, String cmd) throws IOException {
        writer.write(cmd + "\r\n");
        writer.flush();
    }

    private String readResponse(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new IOException("Mất kết nối đột ngột từ máy chủ POP3.");
        }
        return line;
    }

    private String readMultiline(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (".".equals(line)) {
                break;
            }
            if (line.startsWith("..")) {
                line = line.substring(1);
            }
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    private int parseStatCount(String statResp) {
        try {
            String[] parts = statResp.trim().split("\\s+");
            if (parts.length >= 2) {
                return Integer.parseInt(parts[1]);
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    /**
     * Phân tích cú pháp RFC 822 (Headers, MIME Boundary, Plain text, Attachments).
     */
    private ReceivedEmail parseRfc822(int msgNum, String raw) {
        String from = "";
        String to = "";
        String subject = "(Không có tiêu đề)";
        String date = "";
        String contentType = "text/plain";
        List<ReceivedAttachment> attachments = new ArrayList<>();

        int splitIndex = raw.indexOf("\n\n");
        if (splitIndex < 0) {
            splitIndex = raw.indexOf("\r\n\r\n");
        }

        String headersPart = (splitIndex > 0) ? raw.substring(0, splitIndex) : raw;
        String bodyPart = (splitIndex > 0) ? raw.substring(splitIndex).trim() : "";

        BufferedReader hr = new BufferedReader(new StringReader(headersPart));
        String hLine;
        try {
            while ((hLine = hr.readLine()) != null) {
                String lower = hLine.toLowerCase();
                if (lower.startsWith("from:")) {
                    from = decodeHeader(hLine.substring(5).trim());
                } else if (lower.startsWith("to:")) {
                    to = decodeHeader(hLine.substring(3).trim());
                } else if (lower.startsWith("subject:")) {
                    subject = decodeHeader(hLine.substring(8).trim());
                } else if (lower.startsWith("date:")) {
                    date = hLine.substring(5).trim();
                } else if (lower.startsWith("content-type:")) {
                    contentType = hLine.substring(13).trim();
                }
            }
        } catch (IOException ignored) {
        }

        // Xử lý Multipart / Attachment đơn giản
        String cleanBody = bodyPart;
        if (contentType.toLowerCase().contains("multipart") && contentType.contains("boundary=")) {
            String boundary = extractBoundary(contentType);
            if (boundary != null) {
                String[] parts = bodyPart.split("--" + boundary);
                StringBuilder bodyBuilder = new StringBuilder();
                for (String part : parts) {
                    if (part.trim().isEmpty() || part.trim().equals("--")) continue;
                    if (part.toLowerCase().contains("content-disposition: attachment") || part.toLowerCase().contains("filename=")) {
                        ReceivedAttachment att = parseAttachmentPart(part);
                        if (att != null) {
                            attachments.add(att);
                        }
                    } else if (part.toLowerCase().contains("text/plain")) {
                        int bSplit = part.indexOf("\n\n");
                        if (bSplit > 0) {
                            bodyBuilder.append(part.substring(bSplit).trim()).append("\n");
                        }
                    }
                }
                if (bodyBuilder.length() > 0) {
                    cleanBody = bodyBuilder.toString().trim();
                }
            }
        }

        return new ReceivedEmail(msgNum, from, to, subject, date, cleanBody, attachments);
    }

    private ReceivedAttachment parseAttachmentPart(String part) {
        try {
            String fileName = "attachment.dat";
            int fIdx = part.indexOf("filename=");
            if (fIdx > 0) {
                int end = part.indexOf("\n", fIdx);
                if (end < 0) end = part.length();
                fileName = part.substring(fIdx + 9, end).replace("\"", "").replace(";", "").trim();
                fileName = decodeHeader(fileName);
            }

            int split = part.indexOf("\n\n");
            if (split > 0) {
                String base64Data = part.substring(split).replaceAll("\\s+", "");
                byte[] decoded = Base64.getDecoder().decode(base64Data);
                return new ReceivedAttachment(fileName, decoded, "application/octet-stream");
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String extractBoundary(String contentType) {
        int idx = contentType.indexOf("boundary=");
        if (idx >= 0) {
            String b = contentType.substring(idx + 9).trim();
            if (b.startsWith("\"")) {
                b = b.substring(1);
                int end = b.indexOf("\"");
                if (end >= 0) b = b.substring(0, end);
            } else {
                int end = b.indexOf(";");
                if (end >= 0) b = b.substring(0, end);
            }
            return b;
        }
        return null;
    }

    private String decodeHeader(String headerValue) {
        try {
            return MimeUtility.decodeText(headerValue);
        } catch (Exception e) {
            return headerValue;
        }
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
}
