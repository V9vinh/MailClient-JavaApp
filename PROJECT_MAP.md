# Sơ đồ dự án — Java Mail Client (Java Swing + FlatLaf Desktop)

## 1. Trạng thái dự án hiện tại

- **Trạng thái:** Đã hoàn thành toàn diện cả **Gửi thư (SMTP)** và **Nhận thư (POP3 / IMAP)** theo đúng mục tiêu slide môn học và `PROJECT_SPEC.md`.
- **Kiến trúc giao diện:** Three-Pane Layout (Master - Detail), Java Swing, FlatLaf Light 3.5.2, MigLayout 11.4.2.
- **Xử lý nền:** `SwingWorker`, tuân thủ 100% Event Dispatch Thread (EDT).
- **Giao thức Email kép:**
  - **Gửi (SMTP):** Socket thuần (RFC 5321, STARTTLS, AUTH LOGIN, DATA dot-stuffing, MIME) và Jakarta Mail.
  - **Nhận (POP3 / IMAP):** Socket POP3 thuần (RFC 1939, USER/PASS, STAT, RETR, MIME parser) và Jakarta Mail (Store IMAP / POP3).
- **Tài liệu nguồn:**
  - [`GEMINI.md`](file:///d:/Email/GEMINI.md): Quy tắc làm việc của Antigravity (Swing + FlatLaf).
  - [`.agents/rules/project_rules.md`](file:///d:/Email/.agents/rules/project_rules.md): Cấu hình Project Rules cho Antigravity.
  - [`PROJECT_SPEC.md`](file:///d:/Email/PROJECT_SPEC.md): Đặc tả yêu cầu kỹ thuật và tiêu chí nghiệm thu.

---

## 2. Cấu trúc thư mục hiện hành

```text
d:/Email/
├── .agents/
│   └── rules/
│       └── project_rules.md     # Quy tắc Antigravity Project Rules (trigger: always_on)
├── src/
│   ├── main/java/com/mailclient/
│   │   ├── application/         # Entry point Swing
│   │   │   └── MailClientSwingApp.java
│   │   ├── config/              # Quản lý cấu hình tài khoản & JSON
│   │   │   ├── AccountConfig.java
│   │   │   └── ConfigManager.java
│   │   ├── model/               # Data models
│   │   │   ├── AttachmentItem.java
│   │   │   ├── EmailMessage.java
│   │   │   ├── LogEntry.java
│   │   │   ├── ReceiveProtocol.java
│   │   │   ├── ReceivedAttachment.java
│   │   │   ├── ReceivedEmail.java
│   │   │   ├── SecurityMode.java
│   │   │   ├── SendResult.java
│   │   │   └── SmtpEngineType.java
│   │   ├── service/             # Protocol services
│   │   │   ├── ConnectionTestService.java
│   │   │   ├── JakartaMailReceiver.java
│   │   │   ├── JakartaMailSmtpSender.java
│   │   │   ├── LogService.java
│   │   │   ├── MailReceiverService.java
│   │   │   ├── MailReceiverServiceFactory.java
│   │   │   ├── SmtpService.java
│   │   │   ├── SmtpServiceFactory.java
│   │   │   └── SocketPop3Client.java
│   │   │   └── SocketSmtpClient.java
│   │   └── ui/
│   │       ├── swing/           # Swing Views & Panels
│   │       │   ├── ComposePanel.java
│   │       │   ├── InboxPanel.java
│   │       │   ├── LogPanel.java
│   │       │   ├── MainFrame.java
│   │       │   └── SettingsPanel.java
│   │       ├── theme/           # FlatLaf Light + Cloud Blue Color Tokens
│   │       │   └── CloudBlueTheme.java
│   │       └── util/            # Dialog & UI Utilities
│   │           └── DialogUtil.java
│   └── test/java/com/mailclient/
│       ├── MailClientBusinessLogicTest.java
│       ├── MailClientSwingUITest.java
│       └── MailReceiverTest.java
├── reference-images/            # Thư mục chứa hình ảnh/tài liệu tham khảo từ slide bài giảng
├── GEMINI.md                    # Bản quy tắc làm việc cấp thư mục gốc (Swing)
├── PROJECT_MAP.md               # Sơ đồ module, chức năng và điều hướng nhanh
├── PROJECT_SPEC.md              # Đặc tả dự án chi tiết (Source of Truth)
├── README.md                    # Hướng dẫn build và chạy ứng dụng
└── pom.xml                      # Cấu hình Maven (Swing, FlatLaf, Jakarta Mail)
```

---

## 3. Bảng phân hệ Module và Chức năng

| Phân hệ / Module | Đường dẫn | Lớp / Thành phần chính | Chức năng chính | Trạng thái |
|---|---|---|---|---|
| **Application** | `application/` | [`MailClientSwingApp`](file:///d:/Email/src/main/java/com/mailclient/application/MailClientSwingApp.java) | Khởi tạo FlatLaf Light, nạp cấu hình và mở cửa sổ `MainFrame` trên EDT | Đã hoàn thành |
| **Theme** | `ui/theme/` | [`CloudBlueTheme`](file:///d:/Email/src/main/java/com/mailclient/ui/theme/CloudBlueTheme.java) | Bảng màu Cloud Blue (#FFFFFF, #2563EB) và cấu hình FlatLaf UI defaults | Đã hoàn thành |
| **Model** | `model/` | [`EmailMessage`](file:///d:/Email/src/main/java/com/mailclient/model/EmailMessage.java)<br>[`ReceivedEmail`](file:///d:/Email/src/main/java/com/mailclient/model/ReceivedEmail.java)<br>[`ReceivedAttachment`](file:///d:/Email/src/main/java/com/mailclient/model/ReceivedAttachment.java)<br>[`AccountConfig`](file:///d:/Email/src/main/java/com/mailclient/config/AccountConfig.java)<br>[`AttachmentItem`](file:///d:/Email/src/main/java/com/mailclient/model/AttachmentItem.java)<br>[`SendResult`](file:///d:/Email/src/main/java/com/mailclient/model/SendResult.java) | Mô hình dữ liệu gửi và nhận thư, tệp đính kèm, chế độ bảo mật và cấu hình kết nối | Đã hoàn thành |
| **Gửi thư (SMTP)** | `service/` | [`SocketSmtpClient`](file:///d:/Email/src/main/java/com/mailclient/service/SocketSmtpClient.java)<br>[`JakartaMailSmtpSender`](file:///d:/Email/src/main/java/com/mailclient/service/JakartaMailSmtpSender.java)<br>[`SmtpServiceFactory`](file:///d:/Email/src/main/java/com/mailclient/service/SmtpServiceFactory.java) | Gửi thư SMTP Socket thuần (STARTTLS/AUTH LOGIN/MIME) và Jakarta Mail | Đã hoàn thành |
| **Nhận thư (POP3/IMAP)** | `service/` | [`SocketPop3Client`](file:///d:/Email/src/main/java/com/mailclient/service/SocketPop3Client.java)<br>[`JakartaMailReceiver`](file:///d:/Email/src/main/java/com/mailclient/service/JakartaMailReceiver.java)<br>[`MailReceiverServiceFactory`](file:///d:/Email/src/main/java/com/mailclient/service/MailReceiverServiceFactory.java) | Nhận thư POP3 Socket thuần (USER/PASS/STAT/RETR) và Jakarta Mail (POP3/IMAP) | Đã hoàn thành |
| **Logging** | `service/` | [`LogService`](file:///d:/Email/src/main/java/com/mailclient/service/LogService.java) | Quản lý nhật ký tập trung thread-safe, tự động che mật khẩu/base64, thông báo tới EDT | Đã hoàn thành |
| **Config** | `config/` | [`ConfigManager`](file:///d:/Email/src/main/java/com/mailclient/config/ConfigManager.java) | Đọc/ghi cấu hình `mail_config.json`, bảo vệ mật khẩu phiên làm việc | Đã hoàn thành |
| **Swing UI** | `ui/swing/` | [`MainFrame`](file:///d:/Email/src/main/java/com/mailclient/ui/swing/MainFrame.java)<br>[`InboxPanel`](file:///d:/Email/src/main/java/com/mailclient/ui/swing/InboxPanel.java)<br>[`ComposePanel`](file:///d:/Email/src/main/java/com/mailclient/ui/swing/ComposePanel.java)<br>[`SettingsPanel`](file:///d:/Email/src/main/java/com/mailclient/ui/swing/SettingsPanel.java)<br>[`LogPanel`](file:///d:/Email/src/main/java/com/mailclient/ui/swing/LogPanel.java) | Three-Pane Layout: Hộp thư đến (Tìm kiếm + Xem chi tiết + Lưu attachment), Soạn thư, Cài đặt kết nối, Nhật ký mạng | Đã hoàn thành |

---

## 4. Lệnh chạy và kiểm thử

- **Chạy kiểm thử:** `mvn test`
- **Biên dịch dự án:** `mvn clean compile`
- **Khởi chạy ứng dụng:** `mvn compile exec:java`
