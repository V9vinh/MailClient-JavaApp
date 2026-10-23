package com.mailclient.ui.swing;

import com.mailclient.config.AccountConfig;
import com.mailclient.model.ReceiveProtocol;
import com.mailclient.model.SecurityMode;
import com.mailclient.model.SendResult;
import com.mailclient.model.SmtpEngineType;
import com.mailclient.service.LogService;
import com.mailclient.service.MailReceiverService;
import com.mailclient.service.MailReceiverServiceFactory;
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

/**
 * Màn hình Cài đặt tài khoản, cấu hình máy chủ gửi (SMTP) và nhận thư (POP3/IMAP).
 */
public class SettingsPanel extends JPanel {
    private final MainFrame mainFrame;
    private final LogService logger = LogService.getInstance();

    // Thông tin người dùng
    private JTextField displayNameField;
    private JTextField emailAddressField;

    // Cấu hình SMTP (Gửi thư)
    private JTextField smtpHostField;
    private JTextField smtpPortField;
    private JComboBox<SecurityMode> securityModeCombo;
    private JComboBox<SmtpEngineType> engineTypeCombo;

    // Cấu hình Nhận thư (POP3 / IMAP)
    private JComboBox<ReceiveProtocol> receiveProtocolCombo;
    private JTextField receiveHostField;
    private JTextField receivePortField;
    private JComboBox<SecurityMode> receiveSecurityCombo;

    // Xác thực
    private JCheckBox authRequiredCheck;
    private JPanel authCredentialsPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;

    // Nút hành động
    private JButton testSmtpBtn;
    private JButton testReceiveBtn;
    private JButton saveBtn;
    private JLabel testStatusLabel;

    public SettingsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(CloudBlueTheme.BG_PRIMARY);

        initUI();
        loadCurrentSettings();
    }

    private void initUI() {
        JPanel contentPanel = new JPanel(new MigLayout("fillx, insets 20 24 20 24, gap 12", "[grow]", "[]"));
        contentPanel.setBackground(CloudBlueTheme.BG_PRIMARY);

        // Tiêu đề
        JLabel titleLabel = new JLabel("Cài đặt tài khoản");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 19));
        titleLabel.setForeground(CloudBlueTheme.TEXT_PRIMARY);
        contentPanel.add(titleLabel, "wrap 16");

        // Card form chính
        JPanel card = new JPanel(new MigLayout("fillx, insets 20, gap 12", "[180!][grow]", "[]"));
        card.setBackground(CloudBlueTheme.BG_PRIMARY);
        card.setBorder(new CompoundBorder(
                new LineBorder(CloudBlueTheme.BORDER, 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));

        // --- PHẦN 1: THÔNG TIN NGƯỜI DÙNG ---
        card.add(createSectionHeader("Thông tin chung"), "span 2, wrap 8");

        card.add(createLabel("Tên hiển thị người gửi:"), "right");
        displayNameField = new JTextField();
        displayNameField.putClientProperty("JTextField.placeholderText", "Ví dụ: Nguyễn Văn A");
        displayNameField.setPreferredSize(new Dimension(100, 36));
        card.add(displayNameField, "growx, wrap");

        card.add(createLabel("Địa chỉ Email gửi *:"), "right");
        emailAddressField = new JTextField();
        emailAddressField.putClientProperty("JTextField.placeholderText", "nguyenvana@gmail.com");
        emailAddressField.setPreferredSize(new Dimension(100, 36));
        emailAddressField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                String email = emailAddressField.getText().trim();
                if (usernameField.getText().trim().isEmpty() && !email.isEmpty()) {
                    usernameField.setText(email);
                }
                if (email.endsWith("@gmail.com")) {
                    if (smtpHostField.getText().trim().isEmpty() || smtpHostField.getText().contains("@")) {
                        smtpHostField.setText("smtp.gmail.com");
                    }
                    if (receiveHostField.getText().trim().isEmpty() || receiveHostField.getText().contains("@")) {
                        receiveHostField.setText("pop.gmail.com");
                    }
                }
            }
        });
        card.add(emailAddressField, "growx, wrap 16");

        // --- PHẦN 2: CẤU HÌNH GỬI THƯ (SMTP) ---
        card.add(createSectionHeader("Máy chủ gửi thư (SMTP)"), "span 2, wrap 8");

        card.add(createLabel("Máy chủ SMTP (Host) *:"), "right");
        smtpHostField = new JTextField();
        smtpHostField.putClientProperty("JTextField.placeholderText", "smtp.gmail.com");
        smtpHostField.setPreferredSize(new Dimension(100, 36));
        card.add(smtpHostField, "growx, wrap");

        card.add(createLabel("Cổng SMTP (Port) *:"), "right");
        smtpPortField = new JTextField("587");
        smtpPortField.setPreferredSize(new Dimension(100, 36));
        card.add(smtpPortField, "growx, wrap");

        card.add(createLabel("Bảo mật SMTP:"), "right");
        securityModeCombo = new JComboBox<>(SecurityMode.values());
        securityModeCombo.setPreferredSize(new Dimension(100, 36));
        securityModeCombo.addActionListener(e -> {
            SecurityMode mode = (SecurityMode) securityModeCombo.getSelectedItem();
            if (mode != null) {
                int port = parsePort(smtpPortField.getText());
                if (port == 587 || port == 465 || port == 25 || port == 0) {
                    smtpPortField.setText(String.valueOf(mode.getDefaultPort()));
                }
            }
        });
        card.add(securityModeCombo, "growx, wrap");

        card.add(createLabel("Giao thức gửi SMTP:"), "right");
        engineTypeCombo = new JComboBox<>(SmtpEngineType.values());
        engineTypeCombo.setPreferredSize(new Dimension(100, 36));
        card.add(engineTypeCombo, "growx, wrap 16");

        // --- PHẦN 3: CẤU HÌNH NHẬN THƯ (POP3 / IMAP) ---
        card.add(createSectionHeader("Máy chủ nhận thư"), "span 2, wrap 8");

        card.add(createLabel("Giao thức nhận:"), "right");
        receiveProtocolCombo = new JComboBox<>(ReceiveProtocol.values());
        receiveProtocolCombo.setPreferredSize(new Dimension(100, 36));
        receiveProtocolCombo.addActionListener(e -> {
            ReceiveProtocol proto = (ReceiveProtocol) receiveProtocolCombo.getSelectedItem();
            if (proto != null) {
                receivePortField.setText(String.valueOf(proto.getDefaultPort()));
                if (proto == ReceiveProtocol.IMAP_JAKARTA && receiveHostField.getText().contains("pop")) {
                    receiveHostField.setText(receiveHostField.getText().replace("pop", "imap"));
                } else if (proto != ReceiveProtocol.IMAP_JAKARTA && receiveHostField.getText().contains("imap")) {
                    receiveHostField.setText(receiveHostField.getText().replace("imap", "pop"));
                }
            }
        });
        card.add(receiveProtocolCombo, "growx, wrap");

        card.add(createLabel("Máy chủ nhận (Host) *:"), "right");
        receiveHostField = new JTextField();
        receiveHostField.putClientProperty("JTextField.placeholderText", "pop.gmail.com hoặc imap.gmail.com");
        receiveHostField.setPreferredSize(new Dimension(100, 36));
        card.add(receiveHostField, "growx, wrap");

        card.add(createLabel("Cổng nhận (Port) *:"), "right");
        receivePortField = new JTextField("995");
        receivePortField.setPreferredSize(new Dimension(100, 36));
        card.add(receivePortField, "growx, wrap");

        card.add(createLabel("Bảo mật nhận:"), "right");
        receiveSecurityCombo = new JComboBox<>(SecurityMode.values());
        receiveSecurityCombo.setSelectedItem(SecurityMode.SSL_TLS);
        receiveSecurityCombo.setPreferredSize(new Dimension(100, 36));
        card.add(receiveSecurityCombo, "growx, wrap 16");

        // --- PHẦN 4: THÔNG TIN XÁC THỰC ---
        card.add(createSectionHeader("Thông tin xác thực"), "span 2, wrap 8");

        card.add(new JLabel(""), "right");
        authRequiredCheck = new JCheckBox("Yêu cầu xác thực tài khoản", true);
        authRequiredCheck.setFont(CloudBlueTheme.FONT_BOLD);
        authRequiredCheck.setForeground(CloudBlueTheme.TEXT_PRIMARY);
        authRequiredCheck.addActionListener(e -> authCredentialsPanel.setVisible(authRequiredCheck.isSelected()));
        card.add(authRequiredCheck, "wrap 8");

        card.add(new JLabel(""), "right");
        authCredentialsPanel = new JPanel(new MigLayout("fillx, insets 10 14 10 14, gap 8", "[120!][grow]", "[]"));
        authCredentialsPanel.setBackground(CloudBlueTheme.BG_SECONDARY);
        authCredentialsPanel.setBorder(new CompoundBorder(
                new LineBorder(CloudBlueTheme.BORDER, 1, true),
                new EmptyBorder(6, 6, 6, 6)
        ));

        authCredentialsPanel.add(createLabel("Tên đăng nhập *:"), "right");
        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(100, 34));
        authCredentialsPanel.add(usernameField, "growx, wrap");

        authCredentialsPanel.add(createLabel("Mật khẩu phiên *:"), "right");
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(100, 34));
        authCredentialsPanel.add(passwordField, "growx, wrap 4");

        JLabel securityNote = new JLabel("Lưu ý: Mật khẩu chỉ được lưu trong phiên làm việc hiện tại.");
        securityNote.setFont(CloudBlueTheme.FONT_MUTED);
        securityNote.setForeground(CloudBlueTheme.TEXT_SECONDARY);
        authCredentialsPanel.add(securityNote, "skip 1, wrap");

        card.add(authCredentialsPanel, "growx, wrap 20");

        // --- PHẦN 5: CHÂN TRANG NÚT BẤM ---
        card.add(new JLabel(""), "right");
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionsPanel.setBackground(CloudBlueTheme.BG_PRIMARY);

        testSmtpBtn = new JButton("Kiểm tra gửi (SMTP)");
        testSmtpBtn.addActionListener(e -> handleTestSmtpConnection());
        actionsPanel.add(testSmtpBtn);

        testReceiveBtn = new JButton("Kiểm tra nhận thư");
        testReceiveBtn.addActionListener(e -> handleTestReceiveConnection());
        actionsPanel.add(testReceiveBtn);

        saveBtn = new JButton("Lưu cấu hình");
        saveBtn.setBackground(CloudBlueTheme.ACCENT);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> handleSaveSettings());
        actionsPanel.add(saveBtn);

        testStatusLabel = new JLabel("");
        testStatusLabel.setFont(CloudBlueTheme.FONT_BOLD);
        actionsPanel.add(testStatusLabel);

        card.add(actionsPanel, "growx");

        contentPanel.add(card, "growx");

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JLabel createSectionHeader(String text) {
        JLabel header = new JLabel(text);
        header.setFont(CloudBlueTheme.FONT_HEADER);
        header.setForeground(CloudBlueTheme.ACCENT);
        return header;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(CloudBlueTheme.FONT_BOLD);
        label.setForeground(CloudBlueTheme.TEXT_SECONDARY);
        return label;
    }

    public void loadCurrentSettings() {
        AccountConfig acc = mainFrame.getConfigManager().getCurrentAccount();
        if (acc == null) return;

        displayNameField.setText(acc.getDisplayName());
        emailAddressField.setText(acc.getEmailAddress());

        smtpHostField.setText(acc.getSmtpHost());
        smtpPortField.setText(String.valueOf(acc.getSmtpPort()));
        securityModeCombo.setSelectedItem(acc.getSecurityMode());
        engineTypeCombo.setSelectedItem(acc.getEngineType());

        receiveProtocolCombo.setSelectedItem(acc.getReceiveProtocol());
        receiveHostField.setText(acc.getReceiveHost());
        receivePortField.setText(String.valueOf(acc.getReceivePort()));
        receiveSecurityCombo.setSelectedItem(acc.getReceiveSecurityMode());

        authRequiredCheck.setSelected(acc.isAuthRequired());
        authCredentialsPanel.setVisible(acc.isAuthRequired());
        usernameField.setText(acc.getUsername());
        passwordField.setText(acc.getSessionPassword());
        testStatusLabel.setText("");
    }

    private void handleTestSmtpConnection() {
        AccountConfig testAccount = buildAccountFromForm();
        if (!validateSmtp(testAccount)) return;

        testSmtpBtn.setEnabled(false);
        testStatusLabel.setText("Đang kiểm tra SMTP...");
        testStatusLabel.setForeground(CloudBlueTheme.TEXT_SECONDARY);
        mainFrame.setStatus("Đang kiểm tra kết nối tới máy chủ SMTP...", true);

        SmtpService service = SmtpServiceFactory.getService(testAccount);

        SwingWorker<SendResult, String> worker = new SwingWorker<>() {
            @Override
            protected SendResult doInBackground() throws Exception {
                return service.testConnection(testAccount, this::publish);
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                if (!chunks.isEmpty()) {
                    String latest = chunks.get(chunks.size() - 1);
                    testStatusLabel.setText(latest);
                    mainFrame.setStatus(latest, true);
                }
            }

            @Override
            protected void done() {
                testSmtpBtn.setEnabled(true);
                try {
                    get();
                    mainFrame.setStatus("Kiểm tra gửi SMTP thành công!", false);
                    testStatusLabel.setText("Kết nối thành công");
                    testStatusLabel.setForeground(CloudBlueTheme.SUCCESS);
                    DialogUtil.showInfo(SettingsPanel.this, "Kiểm tra thành công", "Đã kết nối và xác thực thành công tới máy chủ SMTP!");
                } catch (Exception ex) {
                    String errorMsg = DialogUtil.formatException(ex);
                    mainFrame.setStatus("Lỗi SMTP: " + errorMsg, false);
                    testStatusLabel.setText("Kết nối thất bại");
                    testStatusLabel.setForeground(CloudBlueTheme.ERROR);
                    DialogUtil.showError(SettingsPanel.this, "Kiểm tra SMTP thất bại", "Không thể hoàn tất kết nối SMTP.", errorMsg);
                }
            }
        };

        worker.execute();
    }

    private void handleTestReceiveConnection() {
        AccountConfig testAccount = buildAccountFromForm();
        if (!validateReceive(testAccount)) return;

        testReceiveBtn.setEnabled(false);
        testStatusLabel.setText("Đang kiểm tra nhận thư...");
        testStatusLabel.setForeground(CloudBlueTheme.TEXT_SECONDARY);
        mainFrame.setStatus("Đang kiểm tra kết nối tới máy chủ nhận thư...", true);

        MailReceiverService service = MailReceiverServiceFactory.getService(testAccount);

        SwingWorker<SendResult, String> worker = new SwingWorker<>() {
            @Override
            protected SendResult doInBackground() throws Exception {
                return service.testReceiveConnection(testAccount, this::publish);
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                if (!chunks.isEmpty()) {
                    String latest = chunks.get(chunks.size() - 1);
                    testStatusLabel.setText(latest);
                    mainFrame.setStatus(latest, true);
                }
            }

            @Override
            protected void done() {
                testReceiveBtn.setEnabled(true);
                try {
                    SendResult result = get();
                    mainFrame.setStatus("Kiểm tra nhận thư thành công!", false);
                    testStatusLabel.setText("Kết nối thành công");
                    testStatusLabel.setForeground(CloudBlueTheme.SUCCESS);
                    DialogUtil.showInfo(SettingsPanel.this, "Kiểm tra thành công", "Kết nối máy chủ nhận thư thành công:\n" + result.getMessage());
                } catch (Exception ex) {
                    String errorMsg = DialogUtil.formatException(ex);
                    mainFrame.setStatus("Lỗi nhận thư: " + errorMsg, false);
                    testStatusLabel.setText("Kết nối thất bại");
                    testStatusLabel.setForeground(CloudBlueTheme.ERROR);
                    DialogUtil.showError(SettingsPanel.this, "Kiểm tra nhận thư thất bại", "Không thể kết nối máy chủ nhận thư.", errorMsg);
                }
            }
        };

        worker.execute();
    }

    private void handleSaveSettings() {
        AccountConfig updated = buildAccountFromForm();
        if (!validateSmtp(updated)) return;

        mainFrame.getConfigManager().updateAccount(updated);
        mainFrame.updateAccountBadge();
        mainFrame.setStatus("Đã lưu cấu hình tài khoản thành công.", false);
        logger.info("Cài đặt", "Đã cập nhật cấu hình tài khoản: " + updated.getEmailAddress());

        DialogUtil.showInfo(this, "Lưu thành công", "Cấu hình tài khoản SMTP và nhận thư đã được cập nhật thành công.");
    }

    private AccountConfig buildAccountFromForm() {
        AccountConfig acc = new AccountConfig();
        acc.setDisplayName(displayNameField.getText().trim());
        acc.setEmailAddress(emailAddressField.getText().trim());

        acc.setSmtpHost(smtpHostField.getText().trim());
        acc.setSmtpPort(parsePort(smtpPortField.getText()));
        acc.setSecurityMode((SecurityMode) securityModeCombo.getSelectedItem());
        acc.setEngineType((SmtpEngineType) engineTypeCombo.getSelectedItem());

        acc.setReceiveProtocol((ReceiveProtocol) receiveProtocolCombo.getSelectedItem());
        acc.setReceiveHost(receiveHostField.getText().trim());
        acc.setReceivePort(parsePort(receivePortField.getText()));
        acc.setReceiveSecurityMode((SecurityMode) receiveSecurityCombo.getSelectedItem());

        acc.setAuthRequired(authRequiredCheck.isSelected());
        acc.setUsername(usernameField.getText().trim());
        acc.setSessionPassword(new String(passwordField.getPassword()));
        return acc;
    }

    private boolean validateSmtp(AccountConfig acc) {
        if (acc.getEmailAddress().isEmpty() || !acc.getEmailAddress().contains("@")) {
            DialogUtil.showWarning(this, "Địa chỉ email không hợp lệ", "Vui lòng nhập địa chỉ email hợp lệ.");
            emailAddressField.requestFocus();
            return false;
        }
        if (acc.getSmtpHost().isEmpty()) {
            DialogUtil.showWarning(this, "Thiếu máy chủ SMTP", "Vui lòng nhập địa chỉ máy chủ SMTP (Host). Ví dụ: smtp.gmail.com");
            smtpHostField.requestFocus();
            return false;
        }
        if (acc.getSmtpHost().contains("@")) {
            DialogUtil.showWarning(this, "Máy chủ SMTP không hợp lệ",
                    "Địa chỉ Máy chủ SMTP (Host) không được chứa ký tự '@'.\n" +
                    "Bạn đang nhập nhầm địa chỉ email vào ô Máy chủ.\n" +
                    "Ví dụ máy chủ SMTP đúng: smtp.gmail.com");
            smtpHostField.requestFocus();
            return false;
        }
        if (acc.getSmtpPort() <= 0 || acc.getSmtpPort() > 65535) {
            DialogUtil.showWarning(this, "Cổng SMTP không hợp lệ", "Cổng kết nối phải nằm trong khoảng từ 1 đến 65535.");
            smtpPortField.requestFocus();
            return false;
        }
        if (acc.isAuthRequired()) {
            if (acc.getUsername().isEmpty()) {
                DialogUtil.showWarning(this, "Thiếu tên đăng nhập", "Vui lòng nhập tên đăng nhập (Username).");
                usernameField.requestFocus();
                return false;
            }
            if (acc.getSessionPassword().isEmpty()) {
                DialogUtil.showWarning(this, "Thiếu mật khẩu phiên", "Vui lòng nhập mật khẩu tài khoản cho phiên làm việc.");
                passwordField.requestFocus();
                return false;
            }
        }
        return true;
    }

    private boolean validateReceive(AccountConfig acc) {
        if (acc.getReceiveHost().isEmpty()) {
            DialogUtil.showWarning(this, "Thiếu máy chủ nhận thư", "Vui lòng nhập địa chỉ máy chủ nhận thư (POP3 / IMAP Host). Ví dụ: pop.gmail.com");
            receiveHostField.requestFocus();
            return false;
        }
        if (acc.getReceiveHost().contains("@")) {
            DialogUtil.showWarning(this, "Máy chủ nhận không hợp lệ",
                    "Địa chỉ Máy chủ nhận (Host) không được chứa ký tự '@'.\n" +
                    "Bạn đang nhập nhầm địa chỉ email vào ô Máy chủ.\n" +
                    "Ví dụ máy chủ nhận đúng: pop.gmail.com hoặc imap.gmail.com");
            receiveHostField.requestFocus();
            return false;
        }
        if (acc.getReceivePort() <= 0 || acc.getReceivePort() > 65535) {
            DialogUtil.showWarning(this, "Cổng nhận không hợp lệ", "Cổng kết nối phải nằm trong khoảng từ 1 đến 65535.");
            receivePortField.requestFocus();
            return false;
        }
        if (acc.isAuthRequired()) {
            if (acc.getUsername().isEmpty()) {
                DialogUtil.showWarning(this, "Thiếu tên đăng nhập", "Vui lòng nhập tên đăng nhập (Username).");
                usernameField.requestFocus();
                return false;
            }
            if (acc.getSessionPassword().isEmpty()) {
                DialogUtil.showWarning(this, "Thiếu mật khẩu phiên", "Vui lòng nhập mật khẩu tài khoản cho phiên làm việc.");
                passwordField.requestFocus();
                return false;
            }
        }
        return true;
    }

    private int parsePort(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return 587;
        }
    }
}
