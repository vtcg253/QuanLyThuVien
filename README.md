# Quản lý thư viện

Ứng dụng web quản lý thư viện xây dựng bằng Java 17, Spring Boot và Oracle Database.

## Chức năng

- Quản lý sách, thể loại, tác giả, nhà xuất bản, độc giả và nhân viên.
- Lập phiếu mượn, theo dõi trạng thái và xử lý trả sách.
- Tìm kiếm và phân trang danh sách.
- Giao diện responsive, có thông báo và hộp thoại xác nhận thao tác.
- Đăng nhập và phân quyền cho tài khoản nhân viên.

## Công nghệ

- Java 17
- Spring Boot 3.3
- Spring MVC, Spring Data JPA, Spring Security, Thymeleaf
- Oracle Database
- Maven Wrapper

## Yêu cầu

- JDK 17 trở lên
- Oracle Database đang chạy và có schema phù hợp
- Maven không bắt buộc cài riêng nếu dùng Maven Wrapper

Schema tham khảo nằm trong [`sql/expected-schema.sql`](sql/expected-schema.sql). Ứng dụng dùng các bảng đã được tạo sẵn và không tự tạo/cập nhật schema.

## Cấu hình cơ sở dữ liệu

Thiết lập các biến môi trường trước khi chạy ứng dụng:

```bash
export DB_URL='jdbc:oracle:thin:@localhost:1521/FREEPDB1'
export DB_USERNAME='your-oracle-username'
export DB_PASSWORD='your-oracle-password'
```

`DB_URL` mặc định là `jdbc:oracle:thin:@localhost:1521/FREEPDB1`; `DB_USERNAME` mặc định là `thuthu`. `DB_PASSWORD` phải được cung cấp trong môi trường chạy.

Không đưa mật khẩu hoặc thông tin kết nối thật lên Git. Trước khi đẩy dự án lên remote, hãy kiểm tra cấu hình và lịch sử thay đổi để chắc chắn không có thông tin bí mật.

## Chạy ứng dụng

macOS / Linux:

```bash
./mvnw spring-boot:run
```

Windows:

```bat
mvnw.cmd spring-boot:run
```

Mở <http://localhost:8080>. Trang đăng nhập sẽ yêu cầu tài khoản nhân viên đã được cấu hình trong cơ sở dữ liệu.

## Kiểm thử và đóng gói

```bash
./mvnw test
./mvnw package
```

File JAR được tạo trong thư mục `target/`.

## Đẩy dự án lên GitHub

Trong thư mục dự án, sau khi tạo repository trống trên GitHub và kiểm tra không có thông tin nhạy cảm:

```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin <github-repository-url>
git push -u origin main
```

Nếu dự án đã được khởi tạo Git và có remote, bỏ qua `git init` và `git remote add origin`; kiểm tra remote bằng `git remote -v`.
