# FU Cinema Ticket System

Website bán vé xem phim — Spring Boot 3, Spring Data JPA, Thymeleaf, SQL Server.

## Công nghệ
Java 17 · Spring Boot 3.3 · Spring Data JPA (Hibernate 6) · Thymeleaf · Bootstrap 5 · SQL Server · Maven

## Cài đặt & chạy
1. Tạo database: `CREATE DATABASE FUCinemaTicketDB;`
2. Copy `src/main/resources/application-local.properties.example` → `application-local.properties`, điền tài khoản SQL Server.
3. Chạy: `mvn spring-boot:run` → mở http://localhost:8080
4. Lần chạy đầu tiên, dữ liệu mẫu được tạo tự động.

## Tài khoản mẫu
| Vai trò | Email | Mật khẩu |
|---|---|---|
| Admin | admin@fucinema.vn | Admin@123 |
| Customer | an.nv@gmail.com | Customer@123 |
| Customer (15 tuổi) | binh.tt@gmail.com | Customer@123 |

## Thành viên & phân công
| Thành viên | MSSV | Chức năng |
|---|---|---|
| A |Trần Công Bảo| ... | Nền tảng, entity, đăng nhập/đăng ký/phân quyền |
| B | Võ Nguyên Khánh | Quản lý khách hàng, hồ sơ |
| C | Lã Trọng Huy | Thể loại, phim, trang chủ, đánh giá |
| D | Đinh Gia Bảo | Danh sách & chi tiết đơn đặt vé (Admin),Xác nhận / huỷ đơn |

## Ảnh màn hình
<img width="1892" height="1000" alt="image" src="https://github.com/user-attachments/assets/998aaa7a-744d-4ce3-ac9c-84f9313a3d5e" />

