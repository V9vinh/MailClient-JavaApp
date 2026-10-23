package com.mailclient.ui.theme;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

/**
 * Cấu hình hệ thống thiết kế Cloud Blue trên nền FlatLaf Light.
 */
public class CloudBlueTheme {
    // Cloud Blue Color Tokens
    public static final Color BG_PRIMARY = new Color(0xFFFFFF);        // #FFFFFF
    public static final Color BG_SECONDARY = new Color(0xF6, 0xF8, 0xFC); // #F6F8FC
    public static final Color ACCENT = new Color(0x25, 0x63, 0xEB);       // #2563EB
    public static final Color ACCENT_HOVER = new Color(0x1D, 0x4E, 0xD8); // #1D4ED8
    public static final Color SELECTED = new Color(0xDB, 0xEA, 0xFE);     // #DBEAFE
    public static final Color BORDER = new Color(0xE2, 0xE8, 0xF0);       // #E2E8F0
    public static final Color TEXT_PRIMARY = new Color(0x0F, 0x17, 0x2A); // #0F172A
    public static final Color TEXT_SECONDARY = new Color(0x64, 0x74, 0x8B); // #64748B
    public static final Color SUCCESS = new Color(0x16, 0xA3, 0x4A);      // #16A34A
    public static final Color ERROR = new Color(0xDC, 0x26, 0x26);        // #DC2626

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 17);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_MUTED = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    public static void setup() {
        // Thiết lập FlatLaf Light
        FlatLightLaf.setup();

        // Ghi đè UI defaults theo Cloud Blue Design System
        UIManager.put("Component.accentColor", ACCENT);
        UIManager.put("Component.focusColor", ACCENT);
        UIManager.put("Component.borderColor", BORDER);
        UIManager.put("Component.focusedBorderColor", ACCENT);
        UIManager.put("Component.arc", 6);
        UIManager.put("Button.arc", 6);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("ProgressBar.arc", 6);

        UIManager.put("Panel.background", BG_PRIMARY);
        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("Button.font", FONT_BOLD);
        UIManager.put("TextField.font", FONT_BODY);
        UIManager.put("TextArea.font", FONT_BODY);
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("TableHeader.font", FONT_BOLD);

        UIManager.put("Table.selectionBackground", SELECTED);
        UIManager.put("Table.selectionForeground", ACCENT);
        UIManager.put("Table.gridColor", BORDER);

        UIManager.put("ScrollBar.thumbArc", 6);
        UIManager.put("ScrollBar.width", 10);
    }
}
