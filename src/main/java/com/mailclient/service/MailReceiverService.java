package com.mailclient.service;

import com.mailclient.config.AccountConfig;
import com.mailclient.model.ReceivedEmail;
import com.mailclient.model.SendResult;

import java.util.List;
import java.util.function.Consumer;

/**
 * Giao diện dịch vụ nhận email (hỗ trợ cả POP3 Socket thuần và Jakarta Mail).
 */
public interface MailReceiverService {
    /**
     * Tải danh sách email mới nhất từ hộp thư đến.
     *
     * @param account          Cấu hình tài khoản
     * @param maxCount         Số lượng thư tối đa cần tải
     * @param progressCallback Hàm cập nhật tiến trình
     * @return Danh sách ReceivedEmail
     * @throws Exception khi gặp lỗi mạng hoặc giao thức
     */
    List<ReceivedEmail> fetchEmails(AccountConfig account, int maxCount, Consumer<String> progressCallback) throws Exception;

    /**
     * Kiểm tra kết nối tới máy chủ nhận thư (POP3/IMAP) mà không tải nội dung.
     */
    SendResult testReceiveConnection(AccountConfig account, Consumer<String> progressCallback) throws Exception;
}
