package com.mailclient.ui.swing;

import com.mailclient.model.LogEntry;
import com.mailclient.service.LogService;
import com.mailclient.ui.theme.CloudBlueTheme;
import com.mailclient.ui.util.DialogUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;

/**
 * Màn hình Nhật ký hoạt động và giao thức mạng bằng Java Swing + FlatLaf Light.
 */
public class LogPanel extends JPanel {
    private final LogService logService = LogService.getInstance();
    private DefaultTableModel tableModel;
    private JTable logTable;
    private JTextArea detailArea;

    public LogPanel() {
        setLayout(new BorderLayout());
        setBackground(CloudBlueTheme.BG_PRIMARY);

        initUI();
        loadExistingLogs();

        // Đăng ký nhận log mới tự động
        logService.addListener(this::onNewLogEntry);
    }

    private void initUI() {
        JPanel container = new JPanel(new MigLayout("fill, insets 20 24 20 24, gap 12", "[grow]", "[][grow][120!]"));
        container.setBackground(CloudBlueTheme.BG_PRIMARY);

        // 1. Header & Actions
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CloudBlueTheme.BG_PRIMARY);

        JLabel titleLabel = new JLabel("Nhật ký hoạt động");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 19));
        titleLabel.setForeground(CloudBlueTheme.TEXT_PRIMARY);
        header.add(titleLabel, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setBackground(CloudBlueTheme.BG_PRIMARY);

        JButton copyBtn = new JButton("Sao chép");
        copyBtn.addActionListener(e -> handleCopyLogs());
        actions.add(copyBtn);

        JButton clearBtn = new JButton("Xóa nhật ký");
        clearBtn.addActionListener(e -> handleClearLogs());
        actions.add(clearBtn);

        header.add(actions, BorderLayout.EAST);
        container.add(header, "growx, wrap 10");

        // 2. Table Logs
        String[] columnNames = {"Thời gian", "Mức độ", "Giai đoạn", "Nội dung"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        logTable = new JTable(tableModel);
        logTable.setRowHeight(28);
        logTable.setShowGrid(true);
        logTable.setFillsViewportHeight(true);
        logTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        logTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        logTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        logTable.getColumnModel().getColumn(3).setPreferredWidth(500);

        // Custom Renderer cho cột Mức độ (Level)
        logTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected && value != null) {
                    String str = value.toString();
                    if ("SUCCESS".equals(str)) {
                        setForeground(CloudBlueTheme.SUCCESS);
                    } else if ("ERROR".equals(str)) {
                        setForeground(CloudBlueTheme.ERROR);
                    } else if ("WARN".equals(str)) {
                        setForeground(new Color(0xD9, 0x77, 0x06));
                    } else {
                        setForeground(CloudBlueTheme.ACCENT);
                    }
                    setFont(CloudBlueTheme.FONT_BOLD);
                }
                return c;
            }
        });

        logTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = logTable.getSelectedRow();
            if (selectedRow >= 0 && selectedRow < logTable.getRowCount()) {
                String time = String.valueOf(tableModel.getValueAt(selectedRow, 0));
                String level = String.valueOf(tableModel.getValueAt(selectedRow, 1));
                String stage = String.valueOf(tableModel.getValueAt(selectedRow, 2));
                String msg = String.valueOf(tableModel.getValueAt(selectedRow, 3));
                detailArea.setText(String.format("Thời gian: %s\nMức độ: %s\nGiai đoạn: %s\n\nNội dung chi tiết:\n%s",
                        time, level, stage, msg));
                detailArea.setCaretPosition(0);
            }
        });

        JScrollPane tableScroll = new JScrollPane(logTable);
        tableScroll.setBorder(new LineBorder(CloudBlueTheme.BORDER, 1, true));
        container.add(tableScroll, "grow, wrap 10");

        // 3. Detail Preview Box
        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        detailArea.setBackground(CloudBlueTheme.BG_SECONDARY);

        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailScroll.setBorder(new CompoundBorder(
                new LineBorder(CloudBlueTheme.BORDER, 1, true),
                new EmptyBorder(4, 4, 4, 4)
        ));
        container.add(detailScroll, "grow");

        add(container, BorderLayout.CENTER);
    }

    private void loadExistingLogs() {
        tableModel.setRowCount(0);
        for (LogEntry entry : logService.getLogs()) {
            addEntryToTable(entry);
        }
    }

    private void onNewLogEntry(LogEntry entry) {
        SwingUtilities.invokeLater(() -> {
            if (entry == null) {
                // Xóa log
                tableModel.setRowCount(0);
                detailArea.setText("");
            } else {
                addEntryToTable(entry);
            }
        });
    }

    private void addEntryToTable(LogEntry entry) {
        tableModel.addRow(new Object[]{
                entry.getFormattedTime(),
                entry.getLevel().name(),
                entry.getStage(),
                entry.getMessage()
        });
        // Tự động cuộn xuống dòng mới
        if (logTable.getRowCount() > 0) {
            logTable.scrollRectToVisible(logTable.getCellRect(logTable.getRowCount() - 1, 0, true));
        }
    }

    private void handleClearLogs() {
        boolean confirm = DialogUtil.showConfirmation(this, "Xác nhận xóa", "Bạn có chắc chắn muốn xóa toàn bộ lịch sử nhật ký không?");
        if (confirm) {
            logService.clear();
        }
    }

    private void handleCopyLogs() {
        StringBuilder sb = new StringBuilder();
        List<LogEntry> logs = logService.getLogs();
        for (LogEntry entry : logs) {
            sb.append(entry.toString()).append("\n");
        }
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(new StringSelection(sb.toString()), null);

        DialogUtil.showInfo(this, "Sao chép thành công", "Đã sao chép toàn bộ nhật ký vào Clipboard.");
    }
}
