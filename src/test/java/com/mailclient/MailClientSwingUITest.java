package com.mailclient;

import com.mailclient.ui.swing.MainFrame;
import com.mailclient.ui.theme.CloudBlueTheme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

public class MailClientSwingUITest {

    @Test
    @DisplayName("Khởi tạo và kiểm tra các view Swing và Theme Cloud Blue")
    void testSwingViewsInitialization() {
        // Thiết lập theme
        CloudBlueTheme.setup();

        assertNotNull(CloudBlueTheme.BG_PRIMARY);
        assertNotNull(CloudBlueTheme.ACCENT);
        assertEquals(new Color(0x25, 0x63, 0xEB), CloudBlueTheme.ACCENT);

        // Kiểm tra khởi tạo MainFrame nếu không ở môi trường headless ngặt nghèo
        if (!GraphicsEnvironment.isHeadless()) {
            MainFrame mainFrame = new MainFrame();
            assertNotNull(mainFrame);
            assertTrue(mainFrame.getTitle().contains("Java Mail Client"));

            mainFrame.showInbox();
            mainFrame.showCompose();
            mainFrame.showSettings();
            mainFrame.showLogs();
            mainFrame.setStatus("Test Status OK", false);

            mainFrame.dispose();
        }
    }
}
