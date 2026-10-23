package com.mailclient.ui.swing;

import com.mailclient.config.AccountConfig;
import com.mailclient.config.ConfigManager;
import com.mailclient.model.LogEntry;
import com.mailclient.service.LogService;
import com.mailclient.ui.theme.CloudBlueTheme;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Khung cửa sổ chính của ứng dụng Java Mail Client.
 */
public class MainFrame extends JFrame {
    private final ConfigManager configManager = new ConfigManager();
    private final LogService logService = LogService.getInstance();

    private JLabel accountBadge;
    private JButton navInboxBtn;
    private JButton navComposeBtn;
    private JButton navSettingsBtn;
    private JButton navLogBtn;

    private CardLayout cardLayout;
    private JPanel contentCards;

    private InboxPanel inboxPanel;
    private ComposePanel composePanel;
    private SettingsPanel settingsPanel;
    private LogPanel logPanel;

    private JLabel statusLabel;
    private JProgressBar progressBar;
    private JLabel engineBadge;
    private JLabel logCountLabel;

    public MainFrame() {
        super("Java Mail Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1160, 780);
        setMinimumSize(new Dimension(960, 640));
        setLocationRelativeTo(null);

        initUI();
        updateAccountBadge();

        logService.addListener(this::onLogUpdated);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CloudBlueTheme.BG_PRIMARY);

        // 1. Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(CloudBlueTheme.BG_PRIMARY);
        topBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, CloudBlueTheme.BORDER),
                new EmptyBorder(12, 20, 12, 20)
        ));

        JLabel title = new JLabel("Java Mail Client");
        title.setFont(CloudBlueTheme.FONT_TITLE);
        title.setForeground(CloudBlueTheme.TEXT_PRIMARY);
        topBar.add(title, BorderLayout.WEST);

        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topActions.setBackground(CloudBlueTheme.BG_PRIMARY);

        JButton topSyncBtn = new JButton("Đồng bộ thư");
        topSyncBtn.addActionListener(e -> {
            showInbox();
            inboxPanel.handleFetchEmails();
        });
        topActions.add(topSyncBtn);

        accountBadge = new JLabel("Chưa cấu hình tài khoản");
        accountBadge.setFont(CloudBlueTheme.FONT_BOLD);
        accountBadge.setOpaque(true);
        accountBadge.setBackground(CloudBlueTheme.BG_SECONDARY);
        accountBadge.setBorder(new CompoundBorder(
                new LineBorder(CloudBlueTheme.BORDER, 1, true),
                new EmptyBorder(6, 12, 6, 12)
        ));
        topActions.add(accountBadge);

        JButton topSettingsBtn = new JButton("Cài đặt");
        topSettingsBtn.addActionListener(e -> showSettings());
        topActions.add(topSettingsBtn);

        topBar.add(topActions, BorderLayout.EAST);
        root.add(topBar, BorderLayout.NORTH);

        // 2. Sidebar Navigation
        JPanel sidebar = new JPanel(new MigLayout("fillx, insets 16 12 16 12, gap 6", "[grow]", "[]"));
        sidebar.setBackground(CloudBlueTheme.BG_SECONDARY);
        sidebar.setPreferredSize(new Dimension(200, 100));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, CloudBlueTheme.BORDER));

        JLabel navHeader = new JLabel("HỘP THƯ");
        navHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        navHeader.setForeground(CloudBlueTheme.TEXT_SECONDARY);
        sidebar.add(navHeader, "wrap 8");

        navInboxBtn = createNavButton("Hộp thư đến", () -> showInbox());
        navComposeBtn = createNavButton("Soạn thư", () -> showCompose());
        navSettingsBtn = createNavButton("Cài đặt tài khoản", () -> showSettings());
        navLogBtn = createNavButton("Nhật ký hoạt động", () -> showLogs());

        sidebar.add(navInboxBtn, "growx, wrap");
        sidebar.add(navComposeBtn, "growx, wrap");
        sidebar.add(navSettingsBtn, "growx, wrap");
        sidebar.add(navLogBtn, "growx");

        root.add(sidebar, BorderLayout.WEST);

        // 3. Center Content (CardLayout)
        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);
        contentCards.setBackground(CloudBlueTheme.BG_PRIMARY);

        inboxPanel = new InboxPanel(this);
        composePanel = new ComposePanel(this);
        settingsPanel = new SettingsPanel(this);
        logPanel = new LogPanel();

        contentCards.add(inboxPanel, "INBOX");
        contentCards.add(composePanel, "COMPOSE");
        contentCards.add(settingsPanel, "SETTINGS");
        contentCards.add(logPanel, "LOGS");

        root.add(contentCards, BorderLayout.CENTER);

        // 4. Bottom Status Bar
        JPanel statusBar = new JPanel(new BorderLayout(14, 0));
        statusBar.setBackground(CloudBlueTheme.BG_SECONDARY);
        statusBar.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, CloudBlueTheme.BORDER),
                new EmptyBorder(8, 16, 8, 16)
        ));

        statusLabel = new JLabel("Sẵn sàng");
        statusLabel.setFont(CloudBlueTheme.FONT_MUTED);
        statusLabel.setForeground(CloudBlueTheme.TEXT_SECONDARY);
        statusBar.add(statusLabel, BorderLayout.CENTER);

        JPanel statusRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        statusRight.setBackground(CloudBlueTheme.BG_SECONDARY);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(160, 14));
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        statusRight.add(progressBar);

        engineBadge = new JLabel("Giao thức: SOCKET");
        engineBadge.setFont(CloudBlueTheme.FONT_MUTED);
        engineBadge.setForeground(CloudBlueTheme.TEXT_SECONDARY);
        statusRight.add(engineBadge);

        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 14));
        statusRight.add(sep);

        logCountLabel = new JLabel("Nhật ký: 0 mục");
        logCountLabel.setFont(CloudBlueTheme.FONT_MUTED);
        logCountLabel.setForeground(CloudBlueTheme.TEXT_SECONDARY);
        statusRight.add(logCountLabel);

        statusBar.add(statusRight, BorderLayout.EAST);
        root.add(statusBar, BorderLayout.SOUTH);

        setContentPane(root);
        showInbox();
    }

    private JButton createNavButton(String title, Runnable action) {
        JButton btn = new JButton(title);
        btn.setFont(CloudBlueTheme.FONT_BOLD);
        btn.setForeground(CloudBlueTheme.TEXT_SECONDARY);
        btn.setBackground(CloudBlueTheme.BG_SECONDARY);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 12, 10, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.addActionListener(e -> action.run());
        return btn;
    }

    private void setActiveNav(JButton activeBtn) {
        JButton[] buttons = {navInboxBtn, navComposeBtn, navSettingsBtn, navLogBtn};
        for (JButton b : buttons) {
            if (b == activeBtn) {
                b.setBackground(CloudBlueTheme.SELECTED);
                b.setForeground(CloudBlueTheme.ACCENT);
            } else {
                b.setBackground(CloudBlueTheme.BG_SECONDARY);
                b.setForeground(CloudBlueTheme.TEXT_SECONDARY);
            }
        }
    }

    public void showInbox() {
        setActiveNav(navInboxBtn);
        cardLayout.show(contentCards, "INBOX");
    }

    public void showCompose() {
        setActiveNav(navComposeBtn);
        cardLayout.show(contentCards, "COMPOSE");
        composePanel.refreshAccountState();
    }

    public void showSettings() {
        setActiveNav(navSettingsBtn);
        cardLayout.show(contentCards, "SETTINGS");
        settingsPanel.loadCurrentSettings();
    }

    public void showLogs() {
        setActiveNav(navLogBtn);
        cardLayout.show(contentCards, "LOGS");
    }

    public void updateAccountBadge() {
        AccountConfig acc = configManager.getCurrentAccount();
        if (acc != null && acc.isValid()) {
            accountBadge.setText(acc.getEmailAddress() + " (" + acc.getSecurityMode().name() + ")");
            accountBadge.setBackground(CloudBlueTheme.SELECTED);
            accountBadge.setForeground(CloudBlueTheme.ACCENT);
        } else {
            accountBadge.setText("Chưa cấu hình tài khoản");
            accountBadge.setBackground(CloudBlueTheme.BG_SECONDARY);
            accountBadge.setForeground(CloudBlueTheme.TEXT_SECONDARY);
        }

        if (acc != null) {
            String smtpEng = acc.getEngineType() != null ? acc.getEngineType().name() : "SOCKET";
            String recvProto = acc.getReceiveProtocol() != null ? acc.getReceiveProtocol().name() : "POP3_SOCKET";
            engineBadge.setText("SMTP: " + smtpEng + " | RECV: " + recvProto);
        }
    }

    public void setStatus(String message, boolean inProgress) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message != null ? message : "Sẵn sàng");
            progressBar.setVisible(inProgress);
        });
    }

    private void onLogUpdated(LogEntry entry) {
        SwingUtilities.invokeLater(() -> {
            logCountLabel.setText("Nhật ký: " + logService.getLogs().size() + " mục");
        });
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
