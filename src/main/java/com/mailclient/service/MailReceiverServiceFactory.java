package com.mailclient.service;

import com.mailclient.config.AccountConfig;
import com.mailclient.model.ReceiveProtocol;

/**
 * Factory cung cấp MailReceiverService tương ứng với cấu hình của người dùng.
 */
public class MailReceiverServiceFactory {
    private static final SocketPop3Client SOCKET_POP3 = new SocketPop3Client();
    private static final JakartaMailReceiver JAKARTA_RECEIVER = new JakartaMailReceiver();

    public static MailReceiverService getService(AccountConfig account) {
        if (account != null && account.getReceiveProtocol() == ReceiveProtocol.POP3_SOCKET) {
            return SOCKET_POP3;
        }
        return JAKARTA_RECEIVER;
    }
}
