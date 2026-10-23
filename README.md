# Hướng Dẫn Sử Dụng — Java Mail Client Desktop

Ứng dụng Mail Client Desktop phát triển bằng Java Swing, giao diện FlatLaf Light và MigLayout, hỗ trợ đầy đủ cả hai chiều **Gửi thư (SMTP)** và **Nhận thư (POP3 / IMAP)**. Ứng dụng cung cấp cơ chế kép: triển khai giao thức Socket TCP thuần và thư viện Jakarta Mail.

---

## 1. Yêu cầu hệ thống và cài đặt

### Yêu cầu môi trường
- **Java Development Kit (JDK):** Phiên bản 21 trở lên (đã kiểm thử tương thích tốt trên OpenJDK 21 và 25).
- **Apache Maven:** Phiên bản 3.8.0 trở lên.
- **Biến môi trường:** Đảm bảo `JAVA_HOME` và `MAVEN_HOME` đã được cấu hình trong `PATH`.

### Lệnh biên dịch và khởi chạy

1. **Biên dịch dự án:**
   ```bash
   mvn clean compile
   ```

2. **Chạy kiểm thử tự động (Unit Tests):**
   ```bash
   mvn test
   ```

3. **Khởi chạy ứng dụng:**
   ```bash
   mvn compile exec:java
   ```

---

## 2. Hướng dẫn sử dụng chi tiết

### Bước 1: Cấu hình tài khoản (Cài đặt)

Trước khi thực hiện gửi hoặc nhận thư, người dùng cần thiết lập thông số máy chủ tại tab **Cài đặt tài khoản**:

1. **Thông tin chung:**
   - **Tên hiển thị người gửi:** Tên hiển thị đại diện trong email gửi đi (ví dụ: `Nguyễn Văn A`).
   - **Địa chỉ Email gửi:** Địa chỉ hòm thư của bạn (ví dụ: `nguyenvana@gmail.com`).

2. **Máy chủ gửi thư (SMTP):**
   - **Máy chủ SMTP (Host):** Địa chỉ máy chủ gửi (ví dụ: `smtp.gmail.com`).
   - **Cổng SMTP (Port):** 
     - Cổng `587` khi sử dụng chế độ bảo mật `STARTTLS`.
     - Cổng `465` khi sử dụng chế độ bảo mật `SSL/TLS`.
     - Cổng `25` khi không mã hóa.
   - **Bảo mật SMTP:** Chọn `STARTTLS` (khuyên dùng) hoặc `SSL/TLS`.
   - **Giao thức gửi SMTP:** 
     - `Socket TCP thuần`: Sử dụng kết nối Socket và luồng Stream trực tiếp theo đặc tả RFC 5321.
     - `Thư viện Jakarta Mail`: Sử dụng triển khai tiêu chuẩn của Jakarta Mail.

3. **Máy chủ nhận thư:**
   - **Giao thức nhận:** 
     - `POP3 (Socket TCP thuần)`: Kết nối Socket trực tiếp tải thư theo RFC 1939.
     - `POP3 (Jakarta Mail)`: Nhận thư POP3 qua Jakarta Mail.
     - `IMAP (Jakarta Mail)`: Nhận thư qua giao thức IMAP tiêu chuẩn.
   - **Máy chủ nhận (Host):** `pop.gmail.com` (đối với POP3) hoặc `imap.gmail.com` (đối với IMAP).
   - **Cổng nhận (Port):** Cổng `995` cho POP3 SSL/TLS hoặc `993` cho IMAP SSL/TLS.
   - **Bảo mật nhận:** Chọn `SSL/TLS`.

4. **Thông tin xác thực:**
   - Tích chọn ô **Yêu cầu xác thực tài khoản**.
   - **Tên đăng nhập:** Địa chỉ email đầy đủ của bạn.
   - **Mật khẩu phiên:** Nhập **Mật khẩu ứng dụng (App Password)** gồm 16 ký tự do nhà cung cấp email cấp (ví dụ: Google App Password).
   - *Lưu ý về bảo mật:* Mật khẩu chỉ được lưu trữ trong bộ nhớ RAM tạm thời của phiên làm việc hiện tại, hoàn toàn không lưu xuống tệp đĩa `mail_config.json`.

5. **Kiểm tra kết nối và Lưu cấu hình:**
   - Nhấn nút **Kiểm tra gửi (SMTP)** để kiểm tra quy trình bắt tay và đăng nhập máy chủ gửi.
   - Nhấn nút **Kiểm tra nhận thư** để kiểm tra đăng nhập hòm thư đến.
   - Sau khi cả hai kiểm tra đều trả về trạng thái thành công, nhấn nút **Lưu cấu hình**.

---

### Bước 2: Nhận và đọc thư (Hộp thư đến)

Giao diện **Hộp thư đến** được thiết kế theo bố cục Three-Pane Layout (Master - Detail):

1. **Đồng bộ thư mới:**
   - Nhấn nút **Đồng bộ** trên thanh công cụ hoặc nút **Đồng bộ thư** ở góc trên bên phải thanh tiêu đề.
   - Ứng dụng sẽ thực thi tác vụ nền bằng `SwingWorker` để kết nối tới máy chủ POP3/IMAP, phân tích cú pháp MIME và tải thư về mà không gây đơ giao diện.
   - Trạng thái tiến độ hiển thị trực quan trên thanh tiến trình ở góc dưới cùng.

2. **Duyệt và lọc danh sách thư:**
   - Danh sách thư hiển thị ở khung bên trái với các cột: Người gửi, Tiêu đề, Thời gian, và Đính kèm.
   - Ô tìm kiếm ở trên đầu danh sách cho phép lọc nhanh thư theo người gửi hoặc tiêu đề theo thời gian thực.

3. **Xem chi tiết nội dung:**
   - Nhấp chuột vào một dòng thư trong danh sách bên trái.
   - Khung chi tiết bên phải sẽ hiển thị:
     - Tiêu đề thư (Subject).
     - Người gửi (From), Người nhận (To), Thời gian nhận (Date).
     - Danh sách tệp đính kèm (nếu thư có tệp đính kèm).
     - Nội dung văn bản của thư (hỗ trợ hiển thị đầy đủ tiếng Việt Unicode UTF-8).

4. **Tải và lưu tệp đính kèm:**
   - Nếu thư có tệp đính kèm, khu vực đính kèm sẽ liệt kê tên tệp và kích thước ước tính.
   - Nhấn nút **Lưu tệp** bên cạnh tệp tin cần tải.
   - Chọn thư mục trên máy tính trong hộp thoại `JFileChooser` để lưu tệp. Hệ thống tự động kiểm tra an toàn đường dẫn (chống tấn công Path Traversal) và hiển thị cảnh báo nếu trùng tên tệp.

---

### Bước 3: Soạn và gửi thư (Soạn thư)

Chuyển sang mục **Soạn thư** tại thanh điều hướng bên trái:

1. **Điền thông tin thư:**
   - **Người nhận (To) \*:** Nhập một hoặc nhiều địa chỉ email người nhận. Nếu gửi nhiều người, phân cách bằng dấu phẩy (`,`).
   - **Thêm Cc / Bcc:** Nhấn liên kết **Thêm Cc/Bcc** ở góc phải để mở rộng các trường gửi bản sao (Cc) hoặc gửi bản sao ẩn danh (Bcc).
   - **Tiêu đề \*:** Nhập chủ đề của bức thư.
   - **Nội dung \*:** Soạn thảo nội dung văn bản thư. Hỗ trợ đầy đủ bộ gõ tiếng Việt có dấu.

2. **Đính kèm tệp tin:**
   - Nhấn nút **Thêm tệp...** để chọn tệp từ máy tính.
   - Có thể chọn và đính kèm nhiều tệp cùng lúc.
   - Danh sách tệp đã chọn sẽ hiển thị tên tệp và dung lượng thực tế. Nếu muốn gỡ bỏ tệp nào, nhấn nút **Xóa** tương ứng.

3. **Gửi thư:**
   - Nhấn nút **Gửi thư** hoặc sử dụng tổ hợp phím tắt nhanh **Ctrl + Enter**.
   - Ứng dụng sẽ tự động đóng gói dữ liệu MIME (`multipart/mixed`, mã hóa Base64 cho tệp đính kèm, dot-stuffing cho nội dung văn bản) và gửi tuần tự qua kết nối bảo mật.
   - Khi hoàn tất, hệ thống hiển thị hộp thoại thông báo thành công.

---

### Bước 4: Theo dõi và trích xuất nhật ký (Nhật ký hoạt động)

Mục **Nhật ký hoạt động** ghi nhận toàn bộ luồng giao tiếp mạng và giao thức:

1. **Nội dung theo dõi:**
   - Ghi lại từng bước bắt tay TCP Socket.
   - Quá trình thương lượng nâng cấp bảo mật TLS (STARTTLS Handshake).
   - Các lệnh giao thức được gửi đi (`EHLO`, `AUTH LOGIN`, `MAIL FROM`, `RCPT TO`, `DATA`, `USER`, `PASS`, `STAT`, `RETR`,...).
   - Mã phản hồi từ máy chủ (`220`, `250`, `334`, `235`, `354`, `221`, `+OK`, `-ERR`).

2. **Cơ chế an toàn:**
   - Hệ thống tự động che giấu mật khẩu tài khoản và chuỗi Base64 credentials trong nhật ký để đảm bảo an toàn thông tin.

3. **Thao tác quản lý:**
   - Nhấp chọn từng dòng nhật ký để xem chi tiết thông điệp ở ô xem trước phía dưới.
   - Nhấn **Sao chép** để đưa toàn bộ nội dung nhật ký vào bộ nhớ tạm (Clipboard), thuận tiện cho việc lập báo cáo thực hành môn học.
   - Nhấn **Xóa nhật ký** để làm sạch danh sách log khi cần kiểm thử phiên mới.

---

---

## 3. Dữ liệu mẫu kiểm thử và Giải đáp về Email

### Câu hỏi: "Có cần điền Email thật không?"

- **Nếu kết nối tới máy chủ công cộng (Gmail, Outlook, Yahoo):** **BẮT BUỘC phải dùng email thật và Mật khẩu ứng dụng (App Password 16 ký tự)**. Máy chủ của Google/Microsoft kiểm tra tính hợp lệ của tài khoản khi gửi lệnh `AUTH LOGIN`. Nếu nhập email giả hoặc mật khẩu thường, máy chủ sẽ từ chối với mã lỗi `535-5.7.8 Username and Password not accepted`.
- **Nếu KHÔNG muốn dùng email cá nhân:** Bạn có thể sử dụng **Dịch vụ Email Sandbox miễn phí (Ethereal Email)** hoặc **Máy chủ cục bộ (Localhost)** theo các mẫu bên dưới:

---

### Mẫu 1: Dịch vụ Email ảo miễn phí (Ethereal Mail — Khuyên dùng để test)

Ethereal Mail là dịch vụ sandbox miễn phí chuyên phục vụ lập trình viên test gửi và nhận email:
1. Truy cập: [https://ethereal.email/create](https://ethereal.email/create)
2. Hệ thống sẽ tự động tạo ngay 1 tài khoản test tạm thời (gồm email, password, host và webmail xem thư trực tiếp).
3. Điền vào ứng dụng theo mẫu sau:

| Thông số | Giá trị thiết lập mẫu |
|---|---|
| **Tên hiển thị** | `Người dùng thử nghiệm` |
| **Địa chỉ Email gửi** | Email sinh tự động (ví dụ: `test.user@ethereal.email`) |
| **Máy chủ SMTP (Host)** | `smtp.ethereal.email` |
| **Cổng SMTP (Port)** | `587` |
| **Bảo mật SMTP** | `STARTTLS (Cổng 587)` |
| **Giao thức gửi SMTP** | `Socket TCP thuần` hoặc `Thư viện Jakarta Mail` |
| **Giao thức nhận** | `IMAP (Jakarta Mail)` |
| **Máy chủ nhận (Host)** | `imap.ethereal.email` |
| **Cổng nhận (Port)** | `993` |
| **Bảo mật nhận** | `SSL/TLS` |
| **Tên đăng nhập** | Địa chỉ email Ethereal của bạn |
| **Mật khẩu phiên** | Mật khẩu Ethereal được cấp |

---

### Mẫu 2: Dịch vụ Gmail thật (Google Account)

Nếu sử dụng Gmail thực tế để gửi và nhận thư thật:

| Thông số | Giá trị thiết lập mẫu |
|---|---|
| **Tên hiển thị** | Tên của bạn (ví dụ: `Nguyễn Văn A`) |
| **Địa chỉ Email gửi** | `your_account@gmail.com` |
| **Máy chủ SMTP (Host)** | `smtp.gmail.com` |
| **Cổng SMTP (Port)** | `587` (với STARTTLS) hoặc `465` (với SSL/TLS) |
| **Bảo mật SMTP** | `STARTTLS (Cổng 587)` |
| **Giao thức gửi SMTP** | `Socket TCP thuần` |
| **Giao thức nhận** | `POP3 (Socket TCP thuần)` hoặc `IMAP (Jakarta Mail)` |
| **Máy chủ nhận (Host)** | `pop.gmail.com` (POP3) hoặc `imap.gmail.com` (IMAP) |
| **Cổng nhận (Port)** | `995` (với POP3) hoặc `993` (với IMAP) |
| **Bảo mật nhận** | `SSL/TLS` |
| **Tên đăng nhập** | `your_account@gmail.com` |
| **Mật khẩu phiên** | *Mật khẩu ứng dụng 16 ký tự* |

> **Hướng dẫn tạo Mật khẩu ứng dụng cho Gmail trong 3 bước:**
> 1. Truy cập trang quản lý Google: [https://myaccount.google.com/security](https://myaccount.google.com/security)
> 2. Đảm bảo tài khoản đã bật **Xác minh 2 bước (2-Step Verification)**.
> 3. Tìm mục **Mật khẩu ứng dụng (App passwords)**, nhập tên ứng dụng (ví dụ: `JavaMailClient`) và nhấn Tạo. Google sẽ cấp cho bạn một chuỗi gồm 16 chữ cái (ví dụ: `abcd efgh ijkl mnop`). Hãy sao chép chuỗi này (bỏ dấu cách) và dán vào ô **Mật khẩu phiên**.

---

### Mẫu 3: Máy chủ Test nội bộ (Localhost / Papercut / Mailpit)

Nếu trường hoặc máy của bạn có cài máy chủ SMTP cục bộ (như Papercut SMTP, MailHog, FakeSMTP):

| Thông số | Giá trị thiết lập mẫu |
|---|---|
| **Máy chủ SMTP (Host)** | `127.0.0.1` hoặc `localhost` |
| **Cổng SMTP (Port)** | `25` (hoặc cổng cấu hình của phần mềm) |
| **Bảo mật SMTP** | `Không mã hóa (Cổng 25)` |
| **Yêu cầu xác thực tài khoản** | Bỏ tích (không cần mật khẩu) |
| **Địa chỉ Email gửi** | Bất kỳ email nào (ví dụ: `student@test.local`) |

---

## 4. Hướng dẫn kiểm thử cụ thể với 2 tài khoản của bạn (`vinh4371z@gmail.com` và `hoangvinh4371@gmail.com`)

Dưới đây là kịch bản kiểm thử thực tế hai chiều giữa 2 tài khoản của bạn.

### Giai đoạn 1: Chuẩn bị Mật khẩu ứng dụng (App Password) cho `vinh4371z@gmail.com`

Vì Google bảo vệ tài khoản rất nghiêm ngặt, **bắt buộc phải tạo Mật khẩu ứng dụng 16 ký tự** (không dùng mật khẩu đăng nhập Gmail thông thường):

1. Mở trình duyệt, đăng nhập vào tài khoản Google: **`vinh4371z@gmail.com`**.
2. Truy cập trực tiếp liên kết cài đặt bảo mật: [https://myaccount.google.com/security](https://myaccount.google.com/security)
3. Đảm bảo mục **Xác minh 2 bước (2-Step Verification)** đã được BẬT.
4. Truy cập trang tạo mật khẩu ứng dụng: [https://myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
5. Nhập tên ứng dụng là `JavaMailClient` và bấm **Tạo (Create)**.
6. Google sẽ cấp cho bạn một chuỗi gồm **16 chữ cái màu vàng** (ví dụ: `abcd efgh ijkl mnop`). Hãy copy chuỗi này (bỏ dấu cách để thành 16 chữ liền nhau) để dùng cho ô **Mật khẩu phiên**.

---

### Giai đoạn 2: Cấu hình ứng dụng bằng tài khoản `vinh4371z@gmail.com`

Khởi chạy ứng dụng Java Mail Client, vào tab **Cài đặt tài khoản** và nhập chính xác các ô sau:

| Tên trường trên giao diện | Giá trị cần điền chính xác | Ghi chú quan trọng |
|---|---|---|
| **Tên hiển thị người gửi** | `Vinh (Sender)` | Tên xuất hiện trong hộp thư người nhận |
| **Địa chỉ Email gửi \*** | `vinh4371z@gmail.com` | Email của người gửi |
| **Máy chủ SMTP (Host) \*** | `smtp.gmail.com` | **Chính xác là `smtp.gmail.com` (Không điền email vào đây)** |
| **Cổng SMTP (Port) \*** | `587` | Cổng STARTTLS chuẩn của Google |
| **Bảo mật SMTP** | `STARTTLS (Cổng 587)` | Chế độ mã hóa bảo mật |
| **Giao thức gửi SMTP** | `Socket TCP thuần` | Kiểm thử trực tiếp thuật toán Socket Stream |
| **Giao thức nhận** | `IMAP (Jakarta Mail)` | Để nhận thư nhanh và đồng bộ chuẩn |
| **Máy chủ nhận (Host) \*** | `imap.gmail.com` | Máy chủ nhận thư của Google |
| **Cổng nhận (Port) \*** | `993` | Cổng nhận IMAP qua SSL |
| **Bảo mật nhận** | `SSL/TLS` | Chế độ bảo mật nhận thư |
| **Yêu cầu xác thực tài khoản** | Tích chọn `[X]` | Bắt buộc để đăng nhập vào Gmail |
| **Tên đăng nhập \*** | `vinh4371z@gmail.com` | Tài khoản xác thực |
| **Mật khẩu phiên \*** | *(Dán 16 ký tự App Password vừa tạo)* | Ví dụ: `abcdefghijklmnop` |

**Kiểm tra và lưu:**
1. Nhấn nút **Kiểm tra gửi (SMTP)**: Hệ thống sẽ hiển thị hộp thoại `Đã kết nối và xác thực thành công tới máy chủ SMTP!`.
2. Nhấn nút **Lưu cấu hình**.

---

### Giai đoạn 3: Soạn và gửi thư sang `hoangvinh4371@gmail.com`

1. Tại thanh điều hướng bên trái, bấm vào mục **Soạn thư**.
2. Điền các thông tin sau:
   - **Người nhận (To) \*:** `hoangvinh4371@gmail.com`
   - **Tiêu đề \*:** `[Thử nghiệm] Email gửi từ Java Mail Client tới hoangvinh4371`
   - **Nội dung \*:**
     ```text
     Xin chào Hoàng Vinh,
     Đây là email thử nghiệm gửi tự động từ ứng dụng Java Mail Client Desktop.
     Giao thức sử dụng: Socket TCP thuần (RFC 5321), bảo mật STARTTLS cổng 587.
     ```
   - *(Tùy chọn)* Nhấn nút **Thêm tệp...** và chọn 1 tệp tin nhỏ từ máy tính để kiểm thử chức năng đính kèm tệp MIME đa phần.
3. Nhấn nút **Gửi thư** (hoặc bấm tổ hợp phím **Ctrl + Enter**).
4. Quan sát thanh trạng thái dưới cùng: Hệ thống sẽ tiến hành các bước EHLO -> STARTTLS -> AUTH LOGIN -> MAIL FROM -> RCPT TO -> DATA.
5. Khi hộp thoại báo **Gửi thư thành công**, hãy mở trình duyệt hoặc điện thoại đăng nhập vào hòm thư **`hoangvinh4371@gmail.com`** để kiểm tra thư vừa nhận được trong Hộp thư đến (Inbox)!

---

### Giai đoạn 4: Kiểm thử Nhận thư (Đọc thư trong ứng dụng)

Có 2 cách để bạn kiểm thử chức năng đọc thư trên ứng dụng:

- **Cách 1 (Nhanh nhất — Tự gửi cho chính mình):**
  1. Trong tab **Soạn thư**, ở ô **Người nhận (To) \*:** hãy điền chính địa chỉ `vinh4371z@gmail.com`.
  2. Bấm **Gửi thư**.
  3. Sau khi gửi thành công, chuyển sang tab **Hộp thư đến**, bấm nút **Đồng bộ**.
  4. Ứng dụng sẽ kéo ngay bức thư vừa gửi về danh sách bên trái. Bấm vào bức thư để xem nội dung chi tiết bên phải và bấm nút **Lưu tệp** nếu có tệp đính kèm.

- **Cách 2 (Nhận thư trên tài khoản `hoangvinh4371@gmail.com`):**
  1. Tạo Mật khẩu ứng dụng 16 ký tự cho tài khoản `hoangvinh4371@gmail.com` trên Google Account.
  2. Vào tab **Cài đặt tài khoản** trên app:
     - Tên đăng nhập: `hoangvinh4371@gmail.com`
     - Mật khẩu phiên: *(Mật khẩu ứng dụng của hoangvinh4371)*
     - Giao thức nhận: `IMAP (Jakarta Mail)`
     - Máy chủ nhận: `imap.gmail.com`, Cổng `993`, Bảo mật `SSL/TLS`
  3. Bấm **Kiểm tra nhận thư** > Bấm **Lưu cấu hình**.
  4. Sang tab **Hộp thư đến** > Bấm **Đồng bộ**: Ứng dụng sẽ tải danh sách các thư mà `vinh4371z@gmail.com` đã gửi sang!

---

## 5. Xử lý các sự cố thường gặp (Troubleshooting)

- **Lỗi `535-5.7.8 Username and Password not accepted`:**
  - *Nguyên nhân:* Bạn đang nhập mật khẩu tài khoản Google thông thường thay vì Mật khẩu ứng dụng 16 ký tự.
  - *Khắc phục:* Bắt buộc bật Xác minh 2 bước và tạo Mật khẩu ứng dụng 16 ký tự theo hướng dẫn ở Giai đoạn 1.
- **Lỗi `Không tìm thấy địa chỉ máy chủ (Unknown Host): [email]`:**
  - *Nguyên nhân:* Nhập nhầm địa chỉ email vào ô Máy chủ SMTP/POP3.
  - *Khắc phục:* Ô Máy chủ SMTP phải là `smtp.gmail.com`, ô Máy chủ nhận phải là `pop.gmail.com` hoặc `imap.gmail.com`.
- **Lỗi `Connect timeout` hoặc `Connection refused`:**
  - *Nguyên nhân:* Cấu hình sai cổng (Port) hoặc mạng công ty/trường học chặn cổng 587/993.
  - *Khắc phục:* Kiểm tra lại bảng ánh xạ cổng (SMTP 587 với STARTTLS, SMTP 465 với SSL, IMAP 993 với SSL).
- **Lỗi `Chưa cấu hình tài khoản gửi thư`:**
  - *Khắc phục:* Chuyển sang tab **Cài đặt tài khoản**, nhập đầy đủ thông tin, kiểm tra kết nối thành công và nhấn **Lưu cấu hình**.
