package com.mailclient.ui.util;

import javax.swing.*;
import java.awt.*;

/**
 * Tiện ích hiển thị hộp thoại thông báo bằng Java Swing.
 */
public class DialogUtil {

    public static void showInfo(Component parent, String title, String content) {
        JOptionPane.showMessageDialog(parent, content, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarning(Component parent, String title, String content) {
        JOptionPane.showMessageDialog(parent, content, title, JOptionPane.WARNING_MESSAGE);
    }

    public static void showError(Component parent, String title, String content, String details) {
        if (details == null || details.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parent, content, title, JOptionPane.ERROR_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.add(new JLabel("<html><b>" + escapeHtml(content) + "</b></html>"), BorderLayout.NORTH);

        JTextArea textArea = new JTextArea(details, 8, 45);
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setCaretPosition(0);
        JScrollPane scrollPane = new JScrollPane(textArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(parent, panel, title, JOptionPane.ERROR_MESSAGE);
    }

    public static boolean showConfirmation(Component parent, String title, String content) {
        int result = JOptionPane.showConfirmDialog(parent, content, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return result == JOptionPane.YES_OPTION;
    }

    public static String formatException(Throwable ex) {
        if (ex == null) return "Lỗi không xác định";
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        if (cause instanceof java.net.UnknownHostException) {
            return "Không tìm thấy địa chỉ máy chủ (Unknown Host): " + cause.getMessage()
                    + "\nVui lòng kiểm tra lại trường Máy chủ (Host). Ví dụ hợp lệ: smtp.gmail.com hoặc pop.gmail.com.";
        }
        if (cause instanceof java.net.ConnectException) {
            return "Không thể kết nối tới máy chủ (Connection Refused): " + cause.getMessage()
                    + "\nVui lòng kiểm tra cổng mạng (Port) và kết nối mạng của thiết bị.";
        }
        if (cause instanceof java.net.SocketTimeoutException) {
            return "Quá thời gian kết nối tới máy chủ (Timeout sau 20 giây)."
                    + "\nVui lòng kiểm tra lại đường truyền mạng hoặc cấu hình tường lửa.";
        }
        String msg = cause.getMessage();
        return (msg != null && !msg.trim().isEmpty()) ? msg : cause.toString();
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
