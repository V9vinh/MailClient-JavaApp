package com.mailclient.ui.swing;

import com.mailclient.config.AccountConfig;
import com.mailclient.model.ReceivedAttachment;
import com.mailclient.model.ReceivedEmail;
import com.mailclient.service.LogService;
import com.mailclient.service.MailReceiverService;
import com.mailclient.service.MailReceiverServiceFactory;
import com.mailclient.ui.theme.CloudBlueTheme;
import com.mailclient.ui.util.DialogUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn hình Hộp thư đến (Inbox) theo bố cục Three-Pane Layout (Master - Detail).
 */
public class InboxPanel extends JPanel {
    private final MainFrame mainFrame;
    private final LogService logger = LogService.getInstance();
    private final List<ReceivedEmail> currentEmails = new ArrayList<>();

    private JTextField searchField;
    private JButton syncBtn;
    private JLabel emailCountLabel;
    private DefaultTableModel tableModel;
    private JTable emailTable;
    private TableRowSorter<DefaultTableModel> sorter;

    // Chi tiết thư (Right pane)
    private JPanel detailCard;
    private JLabel subjectLabel;
    private JLabel fromLabel;
    private JLabel toLabel;
    private JLabel dateLabel;
    private JPanel attachmentsContainer;
    private JPanel attachmentsListPanel;
    private JTextArea bodyArea;
    private JPanel emptyStatePanel;

    public InboxPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(CloudBlueTheme.BG_PRIMARY);

        initUI();
    }

    private void initUI() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(420);
        splitPane.setDividerSize(6);
        splitPane.setBorder(null);

        // 1. Bên trái: Danh sách thư
        JPanel listPanel = createListPanel();
        splitPane.setLeftComponent(listPanel);

        // 2. Bên phải: Chi tiết thư được chọn
        JPanel detailPanel = createDetailPanel();
        splitPane.setRightComponent(detailPanel);

        add(splitPane, BorderLayout.CENTER);
        showEmptyDetailState(true);
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(CloudBlueTheme.BG_PRIMARY);
        panel.setBorder(new EmptyBorder(16, 16, 16, 8));

        // Thanh công cụ trên danh sách (Search + Sync)
        JPanel toolBar = new JPanel(new MigLayout("fillx, insets 0, gap 6", "[grow][]", "[]"));
        toolBar.setBackground(CloudBlueTheme.BG_PRIMARY);

        searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Tìm kiếm người gửi, tiêu đề...");
        searchField.setPreferredSize(new Dimension(100, 34));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterList(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterList(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterList(); }
        });
        toolBar.add(searchField, "growx");

        syncBtn = new JButton("Đồng bộ");
        syncBtn.setBackground(CloudBlueTheme.ACCENT);
        syncBtn.setForeground(Color.WHITE);
        syncBtn.setFont(CloudBlueTheme.FONT_BOLD);
        syncBtn.setPreferredSize(new Dimension(90, 34));
        syncBtn.addActionListener(e -> handleFetchEmails());
        toolBar.add(syncBtn, "wrap");

        emailCountLabel = new JLabel("Hộp thư đến: 0 thư");
        emailCountLabel.setFont(CloudBlueTheme.FONT_MUTED);
        emailCountLabel.setForeground(CloudBlueTheme.TEXT_SECONDARY);
        toolBar.add(emailCountLabel, "span 2");

        panel.add(toolBar, BorderLayout.NORTH);

        // Bảng danh sách thư
        String[] cols = {"Người gửi", "Tiêu đề", "Thời gian", "Đính kèm"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        emailTable = new JTable(tableModel);
        emailTable.setRowHeight(32);
        emailTable.setShowGrid(true);
        emailTable.setFillsViewportHeight(true);
        emailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        emailTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        emailTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        emailTable.getColumnModel().getColumn(2).setPreferredWidth(85);
        emailTable.getColumnModel().getColumn(3).setPreferredWidth(65);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        emailTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        sorter = new TableRowSorter<>(tableModel);
        emailTable.setRowSorter(sorter);

        emailTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int viewRow = emailTable.getSelectedRow();
                if (viewRow >= 0) {
                    int modelRow = emailTable.convertRowIndexToModel(viewRow);
                    if (modelRow >= 0 && modelRow < currentEmails.size()) {
                        showEmailDetail(currentEmails.get(modelRow));
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(emailTable);
        scroll.setBorder(new LineBorder(CloudBlueTheme.BORDER, 1, true));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDetailPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(CloudBlueTheme.BG_PRIMARY);
        wrapper.setBorder(new EmptyBorder(16, 8, 16, 16));

        // Card chi tiết
        detailCard = new JPanel(new BorderLayout(0, 12));
        detailCard.setBackground(CloudBlueTheme.BG_PRIMARY);
        detailCard.setBorder(new CompoundBorder(
                new LineBorder(CloudBlueTheme.BORDER, 1, true),
                new EmptyBorder(16, 20, 16, 20)
        ));

        // Header thư
        JPanel metaBox = new JPanel(new MigLayout("fillx, insets 0, gap 4", "[100!][grow]", "[]"));
        metaBox.setBackground(CloudBlueTheme.BG_PRIMARY);

        subjectLabel = new JLabel("(Tiêu đề thư)");
        subjectLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        subjectLabel.setForeground(CloudBlueTheme.TEXT_PRIMARY);
        metaBox.add(subjectLabel, "span 2, wrap 10");

        metaBox.add(createMetaLabel("Người gửi:"), "right");
        fromLabel = new JLabel();
        fromLabel.setFont(CloudBlueTheme.FONT_BODY);
        metaBox.add(fromLabel, "growx, wrap");

        metaBox.add(createMetaLabel("Người nhận:"), "right");
        toLabel = new JLabel();
        toLabel.setFont(CloudBlueTheme.FONT_BODY);
        metaBox.add(toLabel, "growx, wrap");

        metaBox.add(createMetaLabel("Thời gian:"), "right");
        dateLabel = new JLabel();
        dateLabel.setFont(CloudBlueTheme.FONT_MUTED);
        dateLabel.setForeground(CloudBlueTheme.TEXT_SECONDARY);
        metaBox.add(dateLabel, "growx, wrap 10");

        detailCard.add(metaBox, BorderLayout.NORTH);

        // Body thư
        bodyArea = new JTextArea();
        bodyArea.setEditable(false);
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        bodyArea.setFont(CloudBlueTheme.FONT_BODY);
        JScrollPane bodyScroll = new JScrollPane(bodyArea);
        bodyScroll.setBorder(new LineBorder(CloudBlueTheme.BORDER, 1, true));
        detailCard.add(bodyScroll, BorderLayout.CENTER);

        // Vùng Attachments
        attachmentsContainer = new JPanel(new BorderLayout(8, 6));
        attachmentsContainer.setBackground(CloudBlueTheme.BG_SECONDARY);
        attachmentsContainer.setBorder(new CompoundBorder(
                new LineBorder(CloudBlueTheme.BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel attTitle = new JLabel("Tệp đính kèm:");
        attTitle.setFont(CloudBlueTheme.FONT_BOLD);
        attachmentsContainer.add(attTitle, BorderLayout.NORTH);

        attachmentsListPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        attachmentsListPanel.setBackground(CloudBlueTheme.BG_SECONDARY);
        attachmentsContainer.add(attachmentsListPanel, BorderLayout.CENTER);

        detailCard.add(attachmentsContainer, BorderLayout.SOUTH);

        // Trạng thái trống khi chưa chọn thư
        emptyStatePanel = new JPanel(new GridBagLayout());
        emptyStatePanel.setBackground(CloudBlueTheme.BG_PRIMARY);
        JLabel emptyLbl = new JLabel("Chọn một thư từ danh sách bên trái để xem nội dung.");
        emptyLbl.setFont(CloudBlueTheme.FONT_HEADER);
        emptyLbl.setForeground(CloudBlueTheme.TEXT_SECONDARY);
        emptyStatePanel.add(emptyLbl);

        wrapper.add(detailCard, BorderLayout.CENTER);
        wrapper.add(emptyStatePanel, BorderLayout.NORTH);

        return wrapper;
    }

    private JLabel createMetaLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(CloudBlueTheme.FONT_BOLD);
        l.setForeground(CloudBlueTheme.TEXT_SECONDARY);
        return l;
    }

    private void showEmptyDetailState(boolean empty) {
        detailCard.setVisible(!empty);
        emptyStatePanel.setVisible(empty);
    }

    private void showEmailDetail(ReceivedEmail email) {
        if (email == null) {
            showEmptyDetailState(true);
            return;
        }

        showEmptyDetailState(false);
        subjectLabel.setText(email.getSubject());
        fromLabel.setText(email.getFrom());
        toLabel.setText(email.getTo());
        dateLabel.setText(email.getDate());
        bodyArea.setText(email.getBodyText());
        bodyArea.setCaretPosition(0);

        // Hiển thị tệp đính kèm
        attachmentsListPanel.removeAll();
        if (email.hasAttachments()) {
            attachmentsContainer.setVisible(true);
            for (ReceivedAttachment att : email.getAttachments()) {
                JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
                chip.setBackground(CloudBlueTheme.BG_PRIMARY);
                chip.setBorder(new CompoundBorder(
                        new LineBorder(CloudBlueTheme.BORDER, 1, true),
                        new EmptyBorder(2, 8, 2, 8)
                ));

                JLabel name = new JLabel(att.getFileName());
                name.setFont(CloudBlueTheme.FONT_BOLD);

                JLabel size = new JLabel("(" + att.getFormattedSize() + ")");
                size.setFont(CloudBlueTheme.FONT_MUTED);
                size.setForeground(CloudBlueTheme.TEXT_SECONDARY);

                JButton saveBtn = new JButton("Lưu tệp");
                saveBtn.setFont(CloudBlueTheme.FONT_MUTED);
                saveBtn.addActionListener(e -> handleSaveAttachment(att));

                chip.add(name);
                chip.add(size);
                chip.add(saveBtn);
                attachmentsListPanel.add(chip);
            }
        } else {
            attachmentsContainer.setVisible(false);
        }

        attachmentsListPanel.revalidate();
        attachmentsListPanel.repaint();
    }

    private void handleSaveAttachment(ReceivedAttachment attachment) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu tệp đính kèm");
        fileChooser.setSelectedFile(new File(attachment.getFileName()));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File targetFile = fileChooser.getSelectedFile();

            // Kiểm tra ghi đè theo yêu cầu spec
            if (targetFile.exists()) {
                boolean confirm = DialogUtil.showConfirmation(this, "Xác nhận ghi đè",
                        "Tệp \"" + targetFile.getName() + "\" đã tồn tại. Bạn có muốn ghi đè không?");
                if (!confirm) return;
            }

            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                fos.write(attachment.getData());
                fos.flush();
                logger.success("Tải đính kèm", "Đã lưu tệp: " + targetFile.getAbsolutePath());
                DialogUtil.showInfo(this, "Lưu tệp thành công", "Đã lưu tệp đính kèm vào:\n" + targetFile.getAbsolutePath());
            } catch (Exception ex) {
                logger.error("Tải đính kèm", "Lỗi lưu file: " + ex.getMessage());
                DialogUtil.showError(this, "Lỗi lưu tệp", "Không thể lưu tệp đính kèm.", ex.getMessage());
            }
        }
    }

    public void handleFetchEmails() {
        AccountConfig account = mainFrame.getConfigManager().getCurrentAccount();
        if (account == null || !account.isReceiveValid()) {
            DialogUtil.showWarning(this, "Thiếu cấu hình nhận thư",
                    "Vui lòng vào Cài đặt để thiết lập máy chủ nhận thư (POP3/IMAP) và mật khẩu phiên.");
            mainFrame.showSettings();
            return;
        }

        syncBtn.setEnabled(false);
        mainFrame.setStatus("Đang kết nối nhận thư mới...", true);

        MailReceiverService service = MailReceiverServiceFactory.getService(account);

        SwingWorker<List<ReceivedEmail>, String> worker = new SwingWorker<>() {
            @Override
            protected List<ReceivedEmail> doInBackground() throws Exception {
                return service.fetchEmails(account, 50, this::publish);
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty()) {
                    String status = chunks.get(chunks.size() - 1);
                    mainFrame.setStatus(status, true);
                }
            }

            @Override
            protected void done() {
                syncBtn.setEnabled(true);
                try {
                    List<ReceivedEmail> emails = get();
                    currentEmails.clear();
                    currentEmails.addAll(emails);

                    renderTable();
                    emailCountLabel.setText("Hộp thư đến: " + emails.size() + " thư");
                    mainFrame.setStatus("Đồng bộ hoàn tất. Tải được " + emails.size() + " thư.", false);

                    if (!emails.isEmpty()) {
                        emailTable.setRowSelectionInterval(0, 0);
                    } else {
                        showEmptyDetailState(true);
                    }
                } catch (Exception ex) {
                    String errorMsg = DialogUtil.formatException(ex);
                    mainFrame.setStatus("Lỗi nhận thư: " + errorMsg, false);
                    logger.error("Nhận thư", "Thất bại: " + errorMsg);
                    DialogUtil.showError(InboxPanel.this, "Lỗi nhận thư", "Không thể đồng bộ hộp thư từ máy chủ.", errorMsg);
                }
            }
        };

        worker.execute();
    }

    private void renderTable() {
        tableModel.setRowCount(0);
        for (ReceivedEmail e : currentEmails) {
            tableModel.addRow(new Object[]{
                    e.getFrom(),
                    e.getSubject(),
                    e.getDate(),
                    e.hasAttachments() ? "Có" : ""
            });
        }
    }

    private void filterList() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query, 0, 1));
        }
    }
}
