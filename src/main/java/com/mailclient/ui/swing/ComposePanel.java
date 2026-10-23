package com.mailclient.ui.swing;

import com.mailclient.config.AccountConfig;
import com.mailclient.model.AttachmentItem;
import com.mailclient.model.EmailMessage;
import com.mailclient.model.SendResult;
import com.mailclient.service.LogService;
import com.mailclient.service.SmtpService;
import com.mailclient.service.SmtpServiceFactory;
import com.mailclient.ui.theme.CloudBlueTheme;
import com.mailclient.ui.util.DialogUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Soạn và Gửi thư.
 */
public class ComposePanel extends JPanel {
    private final MainFrame mainFrame;
    private final List<AttachmentItem> attachments = new ArrayList<>();
    private final LogService logger = LogService.getInstance();

    private JPanel warningPanel;
    private JTextField toField;
    private JTextField ccField;
    private JTextField bccField;
    private JPanel ccBccPanel;
    private JButton toggleCcBccBtn;
    private JTextField subjectField;
    private JTextArea bodyArea;
    private JPanel attachmentListPanel;
    private JLabel attachmentSummaryLabel;
    private JButton sendBtn;

    private boolean ccBccVisible = false;

    public ComposePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(CloudBlueTheme.BG_PRIMARY);

        initUI();
        registerKeyShortcuts();
        refreshAccountState();
    }

    private void initUI() {
        JPanel contentPanel = new JPanel(new MigLayout("fillx, insets 20 24 20 24, gap 12", "[grow]", "[]"));
        contentPanel.setBackground(CloudBlueTheme.BG_PRIMARY);

        // 1. Cảnh báo chưa cấu hình tài khoản
        warningPanel = new JPanel(new BorderLayout(12, 0));
        warningPanel.setBackground(new Color(0xFE, 0xF3, 0xC7));
        warningPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xFC, 0xD3, 0x4D), 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));
        JLabel warnLabel = new JLabel("Chưa cấu hình tài khoản gửi thư. Vui lòng thiết lập cấu hình trước khi gửi.");
        warnLabel.setFont(CloudBlueTheme.FONT_BOLD);
        warnLabel.setForeground(new Color(0x92, 0x40, 0x0E));
        warningPanel.add(warnLabel, BorderLayout.CENTER);

        JButton configNowBtn = new JButton("Cấu hình");
        configNowBtn.setBackground(new Color(0xF5, 0x9E, 0x0B));
        configNowBtn.setForeground(Color.WHITE);
        configNowBtn.setFocusPainted(false);
        configNowBtn.addActionListener(e -> mainFrame.showSettings());
        warningPanel.add(configNowBtn, BorderLayout.EAST);
        contentPanel.add(warningPanel, "growx, wrap 16");

        // 2. Tiêu đề màn hình
        JLabel titleLabel = new JLabel("Soạn thư");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 19));
        titleLabel.setForeground(CloudBlueTheme.TEXT_PRIMARY);
        contentPanel.add(titleLabel, "wrap 16");

        // 3. Card form nhập liệu
        JPanel card = new JPanel(new MigLayout("fillx, insets 18, gap 10", "[grow]", "[]"));
        card.setBackground(CloudBlueTheme.BG_PRIMARY);
        card.setBorder(new CompoundBorder(
                new LineBorder(CloudBlueTheme.BORDER, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        // Hàng Người nhận (To) + nút bật Cc/Bcc
        JPanel toHeader = new JPanel(new BorderLayout());
        toHeader.setBackground(CloudBlueTheme.BG_PRIMARY);
        JLabel toLabel = new JLabel("Người nhận (To) *");
        toLabel.setFont(CloudBlueTheme.FONT_BOLD);
        toLabel.setForeground(CloudBlueTheme.TEXT_SECONDARY);
        toHeader.add(toLabel, BorderLayout.WEST);

        toggleCcBccBtn = new JButton("Thêm Cc/Bcc");
        toggleCcBccBtn.setBorder(null);
        toggleCcBccBtn.setContentAreaFilled(false);
        toggleCcBccBtn.setForeground(CloudBlueTheme.ACCENT);
        toggleCcBccBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleCcBccBtn.addActionListener(e -> toggleCcBcc());
        toHeader.add(toggleCcBccBtn, BorderLayout.EAST);
        card.add(toHeader, "growx, wrap 4");

        toField = new JTextField();
        toField.putClientProperty("JTextField.placeholderText", "Địa chỉ email người nhận (phân cách bằng dấu phẩy)");
        toField.setPreferredSize(new Dimension(100, 36));
        card.add(toField, "growx, wrap 10");

        // Khối Cc & Bcc (ẩn/hiện)
        ccBccPanel = new JPanel(new MigLayout("fillx, insets 0, gap 8", "[grow]", "[]"));
        ccBccPanel.setBackground(CloudBlueTheme.BG_PRIMARY);

        JLabel ccLabel = new JLabel("Sao chép (Cc)");
        ccLabel.setFont(CloudBlueTheme.FONT_BOLD);
        ccLabel.setForeground(CloudBlueTheme.TEXT_SECONDARY);
        ccBccPanel.add(ccLabel, "wrap 2");

        ccField = new JTextField();
        ccField.putClientProperty("JTextField.placeholderText", "Địa chỉ Cc");
        ccField.setPreferredSize(new Dimension(100, 34));
        ccBccPanel.add(ccField, "growx, wrap 8");

        JLabel bccLabel = new JLabel("Sao chép ẩn danh (Bcc)");
        bccLabel.setFont(CloudBlueTheme.FONT_BOLD);
        bccLabel.setForeground(CloudBlueTheme.TEXT_SECONDARY);
        ccBccPanel.add(bccLabel, "wrap 2");

        bccField = new JTextField();
        bccField.putClientProperty("JTextField.placeholderText", "Địa chỉ Bcc");
        bccField.setPreferredSize(new Dimension(100, 34));
        ccBccPanel.add(bccField, "growx");

        ccBccPanel.setVisible(false);
        card.add(ccBccPanel, "growx, wrap 10");

        // Tiêu đề thư (Subject)
        JLabel subjectLabel = new JLabel("Tiêu đề *");
        subjectLabel.setFont(CloudBlueTheme.FONT_BOLD);
        subjectLabel.setForeground(CloudBlueTheme.TEXT_SECONDARY);
        card.add(subjectLabel, "wrap 4");

        subjectField = new JTextField();
        subjectField.putClientProperty("JTextField.placeholderText", "Tiêu đề email...");
        subjectField.setPreferredSize(new Dimension(100, 36));
        card.add(subjectField, "growx, wrap 10");

        // Nội dung thư (Body)
        JLabel bodyLabel = new JLabel("Nội dung *");
        bodyLabel.setFont(CloudBlueTheme.FONT_BOLD);
        bodyLabel.setForeground(CloudBlueTheme.TEXT_SECONDARY);
        card.add(bodyLabel, "wrap 4");

        bodyArea = new JTextArea(10, 40);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setFont(CloudBlueTheme.FONT_BODY);
        JScrollPane bodyScroll = new JScrollPane(bodyArea);
        card.add(bodyScroll, "growx, height 200:220:400, wrap 12");

        // Vùng đính kèm tệp
        JPanel attachContainer = new JPanel(new BorderLayout(8, 8));
        attachContainer.setBackground(CloudBlueTheme.BG_SECONDARY);
        attachContainer.setBorder(new CompoundBorder(
                new LineBorder(CloudBlueTheme.BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JPanel attachHeader = new JPanel(new BorderLayout());
        attachHeader.setBackground(CloudBlueTheme.BG_SECONDARY);
        JLabel attachTitle = new JLabel("Tệp đính kèm: ");
        attachTitle.setFont(CloudBlueTheme.FONT_BOLD);
        attachmentSummaryLabel = new JLabel("Chưa có tệp");
        attachmentSummaryLabel.setForeground(CloudBlueTheme.TEXT_SECONDARY);

        JPanel attachTitleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        attachTitleBox.setBackground(CloudBlueTheme.BG_SECONDARY);
        attachTitleBox.add(attachTitle);
        attachTitleBox.add(attachmentSummaryLabel);
        attachHeader.add(attachTitleBox, BorderLayout.WEST);

        JButton addAttachBtn = new JButton("Thêm tệp...");
        addAttachBtn.addActionListener(e -> handleAddAttachments());
        attachHeader.add(addAttachBtn, BorderLayout.EAST);
        attachContainer.add(attachHeader, BorderLayout.NORTH);

        attachmentListPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        attachmentListPanel.setBackground(CloudBlueTheme.BG_SECONDARY);
        attachContainer.add(attachmentListPanel, BorderLayout.CENTER);

        card.add(attachContainer, "growx, wrap 16");

        // Hàng chân trang (Actions)
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(CloudBlueTheme.BG_PRIMARY);

        JButton clearBtn = new JButton("Làm mới");
        clearBtn.addActionListener(e -> handleClearForm());
        footer.add(clearBtn, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightActions.setBackground(CloudBlueTheme.BG_PRIMARY);
        JLabel shortcutLabel = new JLabel("Ctrl + Enter để gửi");
        shortcutLabel.setFont(CloudBlueTheme.FONT_MUTED);
        shortcutLabel.setForeground(CloudBlueTheme.TEXT_SECONDARY);
        rightActions.add(shortcutLabel);

        sendBtn = new JButton("Gửi thư");
        sendBtn.setBackground(CloudBlueTheme.ACCENT);
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setPreferredSize(new Dimension(110, 36));
        sendBtn.setFont(CloudBlueTheme.FONT_BOLD);
        sendBtn.addActionListener(e -> handleSendEmail());
        rightActions.add(sendBtn);

        footer.add(rightActions, BorderLayout.EAST);
        card.add(footer, "growx");

        contentPanel.add(card, "growx");

        JScrollPane mainScroll = new JScrollPane(contentPanel);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);
    }

    private void toggleCcBcc() {
        ccBccVisible = !ccBccVisible;
        ccBccPanel.setVisible(ccBccVisible);
        toggleCcBccBtn.setText(ccBccVisible ? "Ẩn Cc/Bcc" : "Thêm Cc/Bcc");
        revalidate();
        repaint();
    }

    public void refreshAccountState() {
        AccountConfig acc = mainFrame.getConfigManager().getCurrentAccount();
        boolean valid = acc != null && acc.isValid();
        warningPanel.setVisible(!valid);
        revalidate();
        repaint();
    }

    private void handleAddAttachments() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setDialogTitle("Chọn tệp đính kèm");

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                AttachmentItem item = new AttachmentItem(file);
                if (!attachments.contains(item)) {
                    attachments.add(item);
                }
            }
            updateAttachmentUI();
        }
    }

    private void updateAttachmentUI() {
        attachmentListPanel.removeAll();

        for (AttachmentItem item : attachments) {
            JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            chip.setBackground(CloudBlueTheme.BG_PRIMARY);
            chip.setBorder(new CompoundBorder(
                    new LineBorder(CloudBlueTheme.BORDER, 1, true),
                    new EmptyBorder(2, 8, 2, 8)
            ));

            JLabel name = new JLabel(item.getFileName());
            name.setFont(CloudBlueTheme.FONT_BOLD);

            JLabel size = new JLabel("(" + item.getFormattedSize() + ")");
            size.setFont(CloudBlueTheme.FONT_MUTED);
            size.setForeground(CloudBlueTheme.TEXT_SECONDARY);

            JButton removeBtn = new JButton("Xóa");
            removeBtn.setBorder(null);
            removeBtn.setContentAreaFilled(false);
            removeBtn.setForeground(CloudBlueTheme.ERROR);
            removeBtn.setFont(CloudBlueTheme.FONT_MUTED);
            removeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            removeBtn.addActionListener(e -> {
                attachments.remove(item);
                updateAttachmentUI();
            });

            chip.add(name);
            chip.add(size);
            chip.add(removeBtn);
            attachmentListPanel.add(chip);
        }

        if (attachments.isEmpty()) {
            attachmentSummaryLabel.setText("Chưa có tệp");
        } else {
            long totalBytes = attachments.stream().mapToLong(AttachmentItem::getSizeBytes).sum();
            String formattedTotal = totalBytes < 1024 * 1024 ?
                    String.format("%.1f KB", totalBytes / 1024.0) :
                    String.format("%.2f MB", totalBytes / (1024.0 * 1024.0));
            attachmentSummaryLabel.setText(attachments.size() + " tệp (" + formattedTotal + ")");
        }

        attachmentListPanel.revalidate();
        attachmentListPanel.repaint();
    }

    private void handleClearForm() {
        if (!toField.getText().trim().isEmpty() || !subjectField.getText().trim().isEmpty() || !bodyArea.getText().trim().isEmpty()) {
            boolean confirm = DialogUtil.showConfirmation(this, "Xác nhận làm mới", "Bạn có chắc muốn xóa toàn bộ nội dung đang soạn không?");
            if (!confirm) return;
        }
        toField.setText("");
        ccField.setText("");
        bccField.setText("");
        subjectField.setText("");
        bodyArea.setText("");
        attachments.clear();
        updateAttachmentUI();
        if (ccBccVisible) {
            toggleCcBcc();
        }
    }

    private void handleSendEmail() {
        AccountConfig account = mainFrame.getConfigManager().getCurrentAccount();
        if (account == null || !account.isValid()) {
            DialogUtil.showWarning(this, "Thiếu cấu hình tài khoản", "Vui lòng hoàn tất thiết lập tài khoản SMTP và mật khẩu phiên trước khi gửi.");
            mainFrame.showSettings();
            return;
        }

        String toText = toField.getText().trim();
        if (toText.isEmpty()) {
            DialogUtil.showWarning(this, "Thiếu người nhận", "Vui lòng nhập ít nhất một địa chỉ email người nhận.");
            toField.requestFocus();
            return;
        }

        List<String> toList = parseEmailList(toText);
        List<String> ccList = parseEmailList(ccField.getText().trim());
        List<String> bccList = parseEmailList(bccField.getText().trim());

        String subject = subjectField.getText().trim();
        String body = bodyArea.getText();

        EmailMessage message = new EmailMessage();
        message.setFrom(account.getEmailAddress());
        message.setTo(toList);
        message.setCc(ccList);
        message.setBcc(bccList);
        message.setSubject(subject);
        message.setBodyText(body);
        for (AttachmentItem att : attachments) {
            message.addAttachment(att);
        }

        sendBtn.setEnabled(false);
        mainFrame.setStatus("Đang chuẩn bị gửi email...", true);

        SmtpService service = SmtpServiceFactory.getService(account);

        SwingWorker<SendResult, String> worker = new SwingWorker<>() {
            @Override
            protected SendResult doInBackground() throws Exception {
                return service.sendEmail(account, message, this::publish);
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) {
                    String latestStatus = chunks.get(chunks.size() - 1);
                    mainFrame.setStatus(latestStatus, true);
                }
            }

            @Override
            protected void done() {
                sendBtn.setEnabled(true);
                try {
                    SendResult result = get();
                    mainFrame.setStatus("Gửi thư thành công.", false);
                    logger.success("Gửi thư", "Gửi email tới " + String.join(", ", toList) + " thành công.");
                    DialogUtil.showInfo(ComposePanel.this, "Gửi thư thành công", "Máy chủ SMTP đã chấp nhận thư.");
                } catch (Exception ex) {
                    String errorMsg = DialogUtil.formatException(ex);
                    mainFrame.setStatus("Lỗi gửi thư: " + errorMsg, false);
                    logger.error("Gửi thư", "Thất bại: " + errorMsg);
                    DialogUtil.showError(ComposePanel.this, "Gửi thư thất bại", "Không thể gửi email qua máy chủ SMTP.", errorMsg);
                }
            }
        };

        worker.execute();
    }

    private List<String> parseEmailList(String input) {
        List<String> result = new ArrayList<>();
        if (input == null || input.trim().isEmpty()) return result;
        String[] tokens = input.split("[,;\\s]+");
        for (String t : tokens) {
            if (!t.trim().isEmpty()) {
                result.add(t.trim());
            }
        }
        return result;
    }

    private void registerKeyShortcuts() {
        KeyStroke ctrlEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK);
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(ctrlEnter, "sendEmailShortcut");
        getActionMap().put("sendEmailShortcut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (sendBtn.isEnabled()) {
                    handleSendEmail();
                }
            }
        });
    }
}
