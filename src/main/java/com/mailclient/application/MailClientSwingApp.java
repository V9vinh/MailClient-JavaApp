package com.mailclient.application;

import com.mailclient.ui.swing.MainFrame;
import com.mailclient.ui.theme.CloudBlueTheme;

import javax.swing.SwingUtilities;

/**
 * Entry point chính của ứng dụng Java Mail Client bằng Java Swing + FlatLaf Light.
 */
public class MailClientSwingApp {
    public static void main(String[] args) {
        // Thiết lập FlatLaf Light và các Color Tokens Cloud Blue trước khi dựng giao diện
        CloudBlueTheme.setup();

        // Khởi tạo cửa sổ chính trên Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
