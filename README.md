# Java Mail Client Desktop Application

Ứng dụng Desktop Mail Client hoàn chỉnh được phát triển trên nền tảng **Java Swing**, giao diện hiện đại **FlatLaf Light** và hệ thống bố cục **MigLayout**. Dự án phục vụ môn học **Lập trình Mạng**, cung cấp giải pháp email hai chiều: **Gửi thư (SMTP)** và **Nhận thư (POP3 / IMAP)** với cơ chế kép: **Socket TCP thuần** và **Thư viện chuẩn Jakarta Mail**.

---

## 1. Tính năng nổi bật

### Gửi thư (SMTP)
- Hỗ trợ triển khai trực tiếp giao thức **Socket TCP thuần (RFC 5321)**: Bắt tay `EHLO`, nâng cấp bảo mật `STARTTLS`, xác thực tài khoản `AUTH LOGIN` (Base64), khai báo địa chỉ `MAIL FROM`, `RCPT TO`, truyền dữ liệu `DATA` với thuật toán Dot-Stuffing theo đúng đặc tả bài giảng mạng.
- Tích hợp động cơ tiêu chuẩn công nghiệp **Jakarta Mail (Angus Mail)** cho độ tin cậy cao.
- Soạn thảo thư hỗ trợ đầy đủ tiếng Việt UTF-8 (RFC 2047 mã hóa tiêu đề `=?UTF-8?B?...?=`).
- Gửi tới nhiều người nhận (`To`), người nhận bản sao (`Cc`) và người nhận bản sao ẩn danh (`Bcc`).
- Đính kèm nhiều tệp tin cùng lúc (MIME `multipart/mixed`, mã hóa Base64 cho attachment).
- Phím tắt tiện lợi `Ctrl + Enter` để gửi thư nhanh.

### Nhận và duyệt thư (POP3 / IMAP)
- Bố cục giao diện **Three-Pane Layout (Master - Detail)**: Thanh điều hướng, danh sách thư lọc nhanh thời gian thực và khung đọc chi tiết.
- Hỗ trợ nhận thư qua **Socket POP3 thuần (RFC 1939)**: `USER`, `PASS`, `STAT`, `LIST`, `RETR`, tự động phân tích cú pháp MIME headers, nội dung văn bản UTF-8 và tệp đính kèm.
- Hỗ trợ nhận thư chuẩn **IMAP / POP3 qua Jakarta Mail** kết nối SSL an toàn.
- Tải và lưu tệp đính kèm an toàn về máy tính (tích hợp cơ chế kiểm tra chống tấn công Path Traversal).

### Giám sát giao thức và Quản lý tài khoản
- **Nhật ký hoạt động (Protocol Logs):** Theo dõi chi tiết từng bước bắt tay mạng, nâng cấp TLS, mã lệnh và mã phản hồi SMTP / POP3 (`220`, `250`, `334`, `235`, `354`, `+OK`, `-ERR`). Tự động che giấu mật khẩu và thông tin xác thực nhạy cảm.
- Sao chép toàn bộ nhật ký giao thức vào Clipboard phục vụ báo cáo bài tập lớn.
- Quản lý cấu hình cục bộ (`mail_config.json`), bảo mật mật khẩu phiên làm việc trong bộ nhớ RAM, không lưu xuống đĩa.

---

## 2. Công nghệ và Kiến trúc hệ thống

| Phân tầng / Thành phần | Công nghệ sử dụng | Vai trò |
|---|---|---|
| **Ngôn ngữ & Nền tảng** | Java 21 LTS+ (Tương thích OpenJDK 21, 25) | Nền tảng thực thi chính |
| **Giao diện người dùng (UI)** | Java Swing + FlatLaf Light 3.5.2 + MigLayout | Giao diện máy trạm hiện đại, phẳng, tinh gọn |
| **Xử lý bất đồng bộ** | `SwingWorker` | Xử lý mạng trên luồng nền, bảo toàn Event Dispatch Thread (EDT) |
| **Giao thức mạng thuần** | `java.net.Socket`, `javax.net.ssl.SSLSocket` | Triển khai RFC 5321 (SMTP) và RFC 1939 (POP3) qua Socket/Stream |
| **Thư viện tiêu chuẩn** | Jakarta Mail 2.1.3 + Angus Mail 2.0.3 | Triển khai giao thức công nghiệp |
| **Cấu hình & Lưu trữ** | Jackson Databind 2.18.0 | Đọc/ghi cấu hình JSON |
| **Kiểm thử tự động** | JUnit 5 (Jupiter 5.11.3) | Kiểm thử đơn vị (Unit Test) cho logic và giao diện |

---

## 3. Cấu trúc thư mục dự án

```text
MailClient-JavaApp/
├── .agents/                    # Cấu hình quy tắc phát triển dự án
├── src/
│   ├── main/java/com/mailclient/
│   │   ├── application/        # Điểm khởi chạy ứng dụng (MailClientSwingApp.java)
│   │   ├── config/             # Quản lý cấu hình tài khoản (AccountConfig, ConfigManager)
│   │   ├── model/              # Đối tượng mô hình dữ liệu (EmailMessage, ReceivedEmail,...)
│   │   ├── service/            # Triển khai các dịch vụ giao thức mạng (Socket, Jakarta, Log)
│   │   └── ui/                 # Giao diện Swing (MainFrame, InboxPanel, ComposePanel,...)
│   │       ├── swing/          # Các màn hình chính theo layout Three-Pane
│   │       ├── theme/          # Hệ màu Cloud Blue và FlatLaf Look and Feel
│   │       └── util/           # Tiện ích giao diện và chuẩn hóa thông báo lỗi
│   └── test/java/com/mailclient/ # Bộ kiểm thử tự động JUnit 5
├── .gitignore                  # Cấu hình loại trừ file biên dịch và dữ liệu cá nhân
├── GEMINI.md                   # Bộ quy tắc phát triển phần mềm
├── PROJECT_MAP.md              # Bản đồ liên kết mã nguồn và phân hệ
├── PROJECT_SPEC.md             # Đặc tả yêu cầu kỹ thuật chi tiết
├── README.md                   # Hướng dẫn cài đặt và sử dụng tổng quát
└── pom.xml                     # Cấu hình quản lý thư viện Maven
```

---

## 4. Hướng dẫn cài đặt và khởi chạy

### Yêu cầu tiên quyết
- Cài đặt **JDK 21** trở lên (đã kiểm thử tương thích tốt trên JDK 21 và JDK 25).
- Cài đặt **Apache Maven 3.8+**.
- Cấu hình biến môi trường `JAVA_HOME` và `PATH`.

### Các bước thực thi

1. **Sao chép mã nguồn:**
   ```bash
   git clone https://github.com/V9vinh/MailClient-JavaApp.git
   cd MailClient-JavaApp
   ```

2. **Biên dịch dự án:**
   ```bash
   mvn clean compile
   ```

3. **Chạy bộ kiểm thử tự động (Unit Tests):**
   ```bash
   mvn test
   ```

4. **Khởi chạy ứng dụng Desktop:**
   ```bash
   mvn compile exec:java
   ```

---

## 5. Hướng dẫn sử dụng và Cấu hình

### Thiết lập tài khoản (Cài đặt)

Tại tab **Cài đặt tài khoản**, cấu hình thông số kết nối:
1. **Thông tin chung:** Nhập tên hiển thị và địa chỉ email gửi của bạn.
2. **Cấu hình máy chủ gửi (SMTP):**
   - Host: Địa chỉ máy chủ SMTP (ví dụ: `smtp.gmail.com`). Không nhập địa chỉ email vào ô này.
   - Cổng: `587` (với STARTTLS) hoặc `465` (với SSL/TLS).
   - Bảo mật: Chọn `STARTTLS (Cổng 587)` hoặc `SSL/TLS (Cổng 465)`.
   - Giao thức gửi: `Socket TCP thuần` (để kiểm thử thuật toán mạng Socket) hoặc `Thư viện Jakarta Mail`.
3. **Cấu hình máy chủ nhận (POP3 / IMAP):**
   - Host: `pop.gmail.com` (đối với POP3) hoặc `imap.gmail.com` (đối với IMAP).
   - Cổng: `995` (với POP3 SSL) hoặc `993` (với IMAP SSL).
   - Bảo mật: `SSL/TLS`.
   - Giao thức nhận: `POP3 (Socket TCP thuần)`, `POP3 (Jakarta Mail)` hoặc `IMAP (Jakarta Mail)`.
4. **Xác thực:** Tích chọn Yêu cầu xác thực tài khoản, nhập Tên đăng nhập và Mật khẩu phiên (Mật khẩu ứng dụng 16 ký tự đối với Gmail).
5. **Kiểm tra và lưu:** Nhấn **Kiểm tra gửi (SMTP)** và **Kiểm tra nhận thư** trước khi nhấn **Lưu cấu hình**.

---

### Bảng thông số mẫu cho các dịch vụ email phổ biến

| Dịch vụ | Máy chủ SMTP (Cổng) | Bảo mật SMTP | Máy chủ Nhận (Cổng) | Bảo mật Nhận | Mật khẩu yêu cầu |
|---|---|---|---|---|---|
| **Google Gmail** | `smtp.gmail.com` (587) | STARTTLS | `imap.gmail.com` (993) hoặc `pop.gmail.com` (995) | SSL/TLS | Mật khẩu ứng dụng 16 ký tự |
| **Ethereal Mail (Sandbox)** | `smtp.ethereal.email` (587) | STARTTLS | `imap.ethereal.email` (993) | SSL/TLS | Mật khẩu tài khoản test cấp tự động |
| **Máy chủ cục bộ (Localhost)** | `localhost` / `127.0.0.1` (25) | Không mã hóa | `localhost` (110) | Không mã hóa | Không yêu cầu xác thực |

> **Lưu ý đối với tài khoản Gmail:** Bắt buộc phải bật **Xác minh 2 bước** trên Google Account, sau đó tạo **Mật khẩu ứng dụng (App Passwords)** tại `https://myaccount.google.com/apppasswords` để lấy chuỗi 16 ký tự nhập vào ô Mật khẩu phiên.

---

### Kịch bản kiểm thử quy trình Gửi và Nhận thư

1. **Kiểm thử Gửi thư:**
   - Vào mục **Soạn thư**, nhập địa chỉ email người nhận (`To`), tiêu đề và nội dung.
   - Nhấn **Thêm Cc/Bcc** nếu cần gửi bản sao hoặc bản sao ẩn danh.
   - Nhấn **Thêm tệp...** để đính kèm tập tin từ máy tính.
   - Bấm **Gửi thư** (hoặc tổ hợp phím `Ctrl + Enter`).
2. **Kiểm thử Nhận thư:**
   - Vào mục **Hộp thư đến**, bấm nút **Đồng bộ**. Ứng dụng kết nối ngầm qua `SwingWorker` tải danh sách thư về bảng bên trái.
   - Ô tìm kiếm cho phép lọc nhanh thư theo người gửi hoặc tiêu đề theo thời gian thực.
   - Bấm vào thư bất kỳ để xem chi tiết nội dung UTF-8 bên phải và nhấn **Lưu tệp** để tải tệp đính kèm về thư mục máy tính.
3. **Theo dõi nhật ký giao thức:**
   - Vào mục **Nhật ký hoạt động** để kiểm tra các bước bắt tay TCP, TLS và mã phản hồi mạng (`220`, `250`, `334`, `235`, `354`, `221`).
   - Nhấn **Sao chép** để xuất dữ liệu giao thức phục vụ báo cáo.

---

## 6. Xử lý các sự cố thường gặp (Troubleshooting)

- **Lỗi `535-5.7.8 Username and Password not accepted`:**
  - *Nguyên nhân:* Nhập mật khẩu đăng nhập Gmail thông thường thay vì Mật khẩu ứng dụng 16 ký tự.
  - *Khắc phục:* Bật Xác minh 2 bước và tạo Mật khẩu ứng dụng 16 ký tự theo hướng dẫn ở trên.
- **Lỗi `Không tìm thấy địa chỉ máy chủ (Unknown Host)`:**
  - *Nguyên nhân:* Nhập nhầm địa chỉ email vào ô Máy chủ (Host).
  - *Khắc phục:* Ô Máy chủ phải là địa chỉ server (ví dụ: `smtp.gmail.com`, `imap.gmail.com`, `pop.gmail.com`).
- **Lỗi `Connect timeout` hoặc `Connection refused`:**
  - *Nguyên nhân:* Cấu hình sai cổng kết nối hoặc bị tường lửa chặn.
  - *Khắc phục:* Kiểm tra lại bảng ánh xạ cổng (SMTP: 587 với STARTTLS hoặc 465 với SSL, POP3: 995, IMAP: 993).
- **Lỗi `Chưa cấu hình tài khoản gửi thư`:**
  - *Khắc phục:* Vào tab Cài đặt tài khoản điền thông tin, kiểm tra kết nối thành công và nhấn Lưu cấu hình.

---

## 7. Giấy phép (License)

Dự án được xây dựng phục vụ mục đích học tập và nghiên cứu trong môn học Lập trình Mạng.
