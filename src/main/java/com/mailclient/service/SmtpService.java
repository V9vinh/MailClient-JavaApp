package com.mailclient.service;

import com.mailclient.config.AccountConfig;
import com.mailclient.model.EmailMessage;
import com.mailclient.model.SendResult;

import java.util.function.Consumer;

/**
 * Giao diện dịch vụ SMTP thực hiện kiểm tra kết nối và gửi thư.
 */
public interface SmtpService {
    /**
     * Gửi email theo cấu hình tài khoản và nội dung thư.
     *
     * @param account          Cấu hình tài khoản SMTP
     * @param message          Nội dung email
     * @param progressCallback Hàm callback cập nhật tiến trình
     * @return SendResult kết quả gửi thư
     * @throws Exception khi gặp lỗi mạng hoặc giao thức
     */
    SendResult sendEmail(AccountConfig account, EmailMessage message, Consumer<String> progressCallback) throws Exception;

    /**
     * Kiểm tra kết nối tới máy chủ SMTP mà không gửi thư.
     *
     * @param account          Cấu hình tài khoản SMTP
     * @param progressCallback Hàm callback cập nhật tiến trình
     * @return SendResult kết quả kiểm tra
     * @throws Exception khi gặp lỗi
     */
    SendResult testConnection(AccountConfig account, Consumer<String> progressCallback) throws Exception;
}
