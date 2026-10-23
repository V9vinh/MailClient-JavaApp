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
├── README.md                   # Hướng dẫn cài đặt và sử dụng
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

## 5. Hướng dẫn sử dụng và Dữ liệu mẫu kiểm thử

Để kiểm thử nhanh chóng và thuận tiện nhất, **bạn chỉ cần chuẩn bị 1 tài khoản email duy nhất** và thực hiện theo kịch bản **Tự gửi thư cho chính mình (Self-Sending)** dưới đây.

### Giả sử tài khoản thử nghiệm của bạn là: `demo.student@gmail.com`

---

### Bước 1: Chuẩn bị Mật khẩu ứng dụng (App Password)

Vì Google không cho phép ứng dụng đăng nhập bằng mật khẩu tài khoản thông thường, bạn cần tạo **Mật khẩu ứng dụng gồm 16 chữ cái**:

1. Đăng nhập tài khoản Gmail của bạn trên trình duyệt.
2. Truy cập trang bảo mật Google: [https://myaccount.google.com/security](https://myaccount.google.com/security)
3. Đảm bảo mục **Xác minh 2 bước (2-Step Verification)** đang ở trạng thái **BẬT**.
4. Truy cập liên kết tạo mật khẩu: [https://myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
5. Nhập tên ứng dụng là `JavaMailClient` và nhấn **Tạo (Create)**.
6. Sao chép chuỗi **16 chữ cái** được cấp (ví dụ: `abcd efgh ijkl mnop`, bỏ các dấu cách) để dùng cho ô Mật khẩu phiên.

---

### Bước 2: Thiết lập cấu hình tài khoản trên ứng dụng

Mở ứng dụng Java Mail Client, chuyển đến tab **Cài đặt tài khoản** và nhập chính xác từng trường:

| Trường trên giao diện | Giá trị mẫu cần điền | Ý nghĩa & Lưu ý quan trọng |
|---|---|---|
| **Tên hiển thị người gửi** | `Sinh Viên Thử Nghiệm` | Tên xuất hiện đại diện trong hòm thư |
| **Địa chỉ Email gửi \*** | `demo.student@gmail.com` | Thay bằng địa chỉ email thật của bạn |
| **Máy chủ SMTP (Host) \*** | `smtp.gmail.com` | **Máy chủ SMTP của Google (Không điền email vào đây)** |
| **Cổng SMTP (Port) \*** | `587` | Cổng STARTTLS chuẩn |
| **Bảo mật SMTP** | `STARTTLS (Cổng 587)` | Cơ chế mã hóa đường truyền |
| **Giao thức gửi SMTP** | `Socket TCP thuần` | Kiểm thử thuật toán Socket Stream theo bài giảng |
| **Giao thức nhận** | `IMAP (Jakarta Mail)` | Nhận thư nhanh, hỗ trợ đồng bộ chuẩn |
| **Máy chủ nhận (Host) \*** | `imap.gmail.com` | Máy chủ nhận thư (hoặc `pop.gmail.com` nếu dùng POP3) |
| **Cổng nhận (Port) \*** | `993` | Cổng nhận IMAP qua SSL (hoặc `995` với POP3) |
| **Bảo mật nhận** | `SSL/TLS` | Bảo mật đường truyền nhận thư |
| **Yêu cầu xác thực tài khoản** | Tích chọn `[X]` | Bắt buộc để xác thực với máy chủ |
| **Tên đăng nhập \*** | `demo.student@gmail.com` | Tài khoản email xác thực |
| **Mật khẩu phiên \*** | `abcdefghijklmnop` | **Dán 16 chữ cái Mật khẩu ứng dụng vừa tạo ở Bước 1** |

**Kiểm tra và lưu:**
1. Nhấn nút **Kiểm tra gửi (SMTP)**: Hệ thống sẽ báo `Đã kết nối và xác thực thành công tới máy chủ SMTP!`.
2. Nhấn nút **Kiểm tra nhận thư**: Hệ thống xác nhận kết nối máy chủ nhận thư thành công.
3. Nhấn nút **Lưu cấu hình**.

---

### Bước 3: Kịch bản kiểm thử nhanh nhất — Tự gửi thư cho chính mình

Kịch bản này cho phép bạn kiểm thử toàn diện **100% chức năng của ứng dụng (cả Gửi SMTP, Nhận IMAP/POP3, Đọc nội dung UTF-8, Tải tệp đính kèm và xem Nhật ký)** chỉ với 1 tài khoản:

1. **Soạn thư gửi cho chính mình:**
   - Tại thanh điều hướng bên trái, bấm vào mục **Soạn thư**.
   - **Người nhận (To) \*:** Điền chính địa chỉ email của bạn: `demo.student@gmail.com`.
   - **Tiêu đề \*:** `[Thử nghiệm] Tự gửi email kiểm tra Java Mail Client`
   - **Nội dung \*:**
     ```text
     Xin chào,
     Đây là email kiểm thử được gửi và nhận trên cùng một ứng dụng Java Mail Client.
     Giao thức gửi: Socket TCP thuần (RFC 5321, STARTTLS cổng 587).
     Giao thức nhận: IMAP SSL cổng 993.
     ```
   - Nhấn nút **Thêm tệp...** và chọn 1 tệp tin bất kỳ từ máy tính (ví dụ: ảnh `.png` hoặc tệp `.pdf`, `.txt`) để kiểm thử chức năng đính kèm MIME đa phần.
   - Nhấn nút **Gửi thư** (hoặc nhấn phím tắt **Ctrl + Enter**).
   - Hệ thống hiển thị hộp thoại thông báo: `Gửi thư thành công`.

2. **Nhận và đọc bức thư vừa gửi:**
   - Chuyển sang mục **Hộp thư đến**.
   - Nhấn nút **Đồng bộ** (hoặc nút **Đồng bộ thư** ở góc trên cùng).
   - Tác vụ nền `SwingWorker` sẽ kết nối tới máy chủ tải thư mới về mà không gây đơ giao diện.
   - Bức thư bạn vừa gửi cho chính mình sẽ xuất hiện ngay tại dòng đầu tiên của danh sách thư bên trái.
   - Nhấp chuột vào dòng thư đó: Khung chi tiết bên phải sẽ hiển thị đầy đủ tiêu đề, người gửi, người nhận, thời gian và nội dung tiếng Việt.
   - Tại khu vực đính kèm, nhấn nút **Lưu tệp** bên cạnh tệp tin để tải về máy tính và kiểm tra tính toàn vẹn của tệp tải về.

3. **Xem và sao chép nhật ký mạng:**
   - Chuyển sang mục **Nhật ký hoạt động**.
   - Bảng nhật ký sẽ hiển thị toàn bộ tiến trình mạng: Bắt tay kết nối TCP, nâng cấp TLS, các lệnh `EHLO`, `AUTH LOGIN`, `MAIL FROM`, `RCPT TO`, `DATA` và các mã phản hồi từ máy chủ (`220`, `250`, `334`, `235`, `354`, `221`).
   - Mật khẩu và chuỗi Base64 xác thực đã được tự động che giấu an toàn.
   - Nhấn nút **Sao chép** để copy toàn bộ dữ liệu log phục vụ báo cáo bài tập lớn.

---

### Bảng thông số mẫu cho các môi trường thử nghiệm khác

| Môi trường | Máy chủ SMTP (Cổng) | Bảo mật SMTP | Máy chủ Nhận (Cổng) | Bảo mật Nhận | Mật khẩu yêu cầu |
|---|---|---|---|---|---|
| **Google Gmail** | `smtp.gmail.com` (587) | STARTTLS | `imap.gmail.com` (993) hoặc `pop.gmail.com` (995) | SSL/TLS | Mật khẩu ứng dụng 16 ký tự |
| **Ethereal Mail (Sandbox miễn phí)** | `smtp.ethereal.email` (587) | STARTTLS | `imap.ethereal.email` (993) | SSL/TLS | Mật khẩu tài khoản cấp tại `ethereal.email/create` |
| **Máy chủ nội bộ (Localhost)** | `localhost` / `127.0.0.1` (25) | Không mã hóa | `localhost` (110) | Không mã hóa | Không yêu cầu mật khẩu |

---

## 6. Xử lý các sự cố thường gặp (Troubleshooting)

- **Lỗi `535-5.7.8 Username and Password not accepted`:**
  - *Nguyên nhân:* Nhập mật khẩu tài khoản Google thông thường thay vì Mật khẩu ứng dụng 16 ký tự.
  - *Khắc phục:* Bắt buộc bật Xác minh 2 bước trên Google Account và tạo Mật khẩu ứng dụng 16 ký tự theo hướng dẫn ở Bước 1.
- **Lỗi `Không tìm thấy địa chỉ máy chủ (Unknown Host)`:**
  - *Nguyên nhân:* Nhập nhầm địa chỉ email vào ô Máy chủ (Host).
  - *Khắc phục:* Ô Máy chủ SMTP phải là `smtp.gmail.com`, ô Máy chủ nhận phải là `imap.gmail.com` hoặc `pop.gmail.com` (không chứa ký tự `@`).
- **Lỗi `Connect timeout` hoặc `Connection refused`:**
  - *Nguyên nhân:* Cấu hình sai cổng kết nối hoặc mạng bị chặn cổng 587/993.
  - *Khắc phục:* Kiểm tra lại bảng ánh xạ cổng (SMTP: 587 với STARTTLS, SMTP: 465 với SSL, IMAP: 993 với SSL, POP3: 995 với SSL).
- **Lỗi `Chưa cấu hình tài khoản gửi thư`:**
  - *Khắc phục:* Vào tab Cài đặt tài khoản điền thông tin, kiểm tra kết nối thành công và nhấn Lưu cấu hình trước khi gửi thư.

---

## 7. Giấy phép (License)

Dự án được xây dựng phục vụ mục đích học tập và nghiên cứu trong môn học Lập trình Mạng.
