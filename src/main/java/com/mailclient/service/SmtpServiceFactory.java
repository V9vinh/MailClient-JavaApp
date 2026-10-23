package com.mailclient.service;

import com.mailclient.config.AccountConfig;
import com.mailclient.model.SmtpEngineType;

/**
 * Factory cung cấp thể hiện SmtpService tương ứng với cấu hình của người dùng.
 */
public class SmtpServiceFactory {
    private static final SocketSmtpClient SOCKET_CLIENT = new SocketSmtpClient();
    private static final JakartaMailSmtpSender JAKARTA_SENDER = new JakartaMailSmtpSender();

    public static SmtpService getService(AccountConfig account) {
        if (account != null && account.getEngineType() == SmtpEngineType.JAKARTA) {
            return JAKARTA_SENDER;
        }
        return SOCKET_CLIENT;
    }
}
