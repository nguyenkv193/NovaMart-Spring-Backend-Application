# NovaMart Backend

## Documentation

- [API design](docs/API-DESIGN.md) — endpoint contract, request/response schema, authentication, authorization và roadmap cho các module.

Backend REST API cho nền tảng thương mại điện tử NovaMart, được xây dựng với Spring Boot và kiến trúc module hóa. Project hiện tập trung vào xác thực người dùng, quản lý tài khoản và quản lý sản phẩm; domain đơn hàng đã được khai báo ở tầng entity để tiếp tục phát triển.

## Tính năng hiện tại

- Đăng ký và đăng nhập bằng email/password.
- Mã hóa password bằng BCrypt.
- Xác thực stateless bằng JWT Bearer Token.
- Quản lý và tra cứu thông tin người dùng.
- CRUD sản phẩm.
- Validation request bằng Jakarta Validation.
- Response dùng format thống nhất thông qua `ApiResponse`.
- Global exception handler cho lỗi nghiệp vụ và lỗi validation.
- Mô hình hóa quan hệ `User`, `Order`, `OrderItem` và `Product`.

## Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.1.0 |
| Build tool | Gradle 9.5.1 Wrapper |
| Web | Spring MVC |
| Security | Spring Security + JWT |
| Persistence | Spring Data JPA / Hibernate |
| Database hiện tại | H2 in-memory |
| Object mapping | MapStruct |
| Boilerplate reduction | Lombok |
| Validation | Jakarta Bean Validation |

## Yêu cầu môi trường

- JDK 17 trở lên.
- Không cần cài Gradle riêng vì project đã bao gồm Gradle Wrapper.

Kiểm tra Java:

```bash
java -version
```

## Chạy project

### Windows PowerShell

```powershell
.\gradlew.bat bootRun
```

### macOS/Linux

```bash
./gradlew bootRun
```

Sau khi khởi động, API mặc định chạy tại:

```text
http://localhost:8080
```

## Build và test

```bash
# Chạy test
./gradlew test

# Tạo file JAR
./gradlew bootJar
```

Trên Windows, thay `./gradlew` bằng `.\gradlew.bat`.

File JAR được tạo trong thư mục `build/libs`, với tên dạng:

```text
novamart-backend-0.0.1-SNAPSHOT.jar
```

Chạy file JAR:

```bash
java -jar build/libs/novamart-backend-0.0.1-SNAPSHOT.jar
```

## Cấu hình hiện tại

Cấu hình nằm trong `src/main/resources/application.properties`:

```properties
spring.application.name=novamart-backend

spring.datasource.url=jdbc:h2:mem:productdb
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver

app.jwt.secret=<hex-secret>
app.jwt.expiration=3600000
```

`app.jwt.expiration=3600000` tương ứng thời hạn access token khoảng 1 giờ ở cấu hình hiện tại.

Database đang sử dụng H2 in-memory nên dữ liệu sẽ mất khi ứng dụng khởi động lại. Secret JWT hiện chỉ phù hợp cho môi trường local; khi deploy cần đưa secret vào biến môi trường hoặc secret manager.

## Cấu trúc project

```text
src/main/java/com/novamart
├── common
│   ├── exception      # Business exception và global exception handler
│   └── response       # ApiResponse và ErrorResponse
├── config             # Cấu hình chung, bao gồm JacksonConfig
├── constants          # Constant dùng chung
├── modules
│   ├── auth           # Register, login và tạo JWT
│   ├── users          # User entity, repository, service và query API
│   ├── products       # Product CRUD
│   └── orders         # Order/OrderItem entity và trạng thái đơn hàng
├── security
│   ├── config          # SecurityFilterChain và PasswordEncoder
│   ├── jwt             # JWT provider và authentication filter
│   └── userdetails     # Load user và authorities
└── NovaMartApplication.java
```

Package gốc của project là `com.novamart`. `JacksonConfig` và các dependency có chữ `jackson` vẫn giữ nguyên vì chúng thuộc thư viện xử lý JSON, không phải tên project.

## Authentication và authorization

### Đăng ký

```http
POST /api/v1/auth/register
Content-Type: application/json
```

Request body:

```json
{
  "firstName": "Nguyen",
  "lastName": "Van A",
  "dateOfBirth": "1998-05-20",
  "email": "user@example.com",
  "password": "secret123"
}
```

Password phải có tối thiểu 6 ký tự. User đăng ký mới mặc định có role `USER` và permission `READ`.

### Đăng nhập

```http
POST /api/v1/auth/login
Content-Type: application/json
```

Request body:

```json
{
  "email": "user@example.com",
  "password": "secret123"
}
```

Response trả về `accessToken`, `tokenType`, `expiresIn` và thông tin user.

### Gọi API cần xác thực

Gửi token trong header:

```http
Authorization: Bearer <access-token>
```

Các endpoint `/api/v1/auth/**` được public. `GET /api/v1/products/**` cũng được public; các endpoint còn lại yêu cầu JWT hợp lệ. Role/permission đã có trong user model nhưng phân quyền theo role ở từng endpoint chưa được cấu hình.

## API hiện có

### Auth

| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Public | Tạo tài khoản và trả access token |
| `POST` | `/api/v1/auth/login` | Public | Đăng nhập và trả access token |

### Users

| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| `GET` | `/api/v1/users` | JWT | Lấy danh sách user |
| `GET` | `/api/v1/users/{id}` | JWT | Lấy user theo ID |
| `GET` | `/api/v1/users/search?email={email}` | JWT | Tìm user theo email |

Đăng ký tài khoản được thực hiện qua Auth module; endpoint `POST /api/v1/users` không còn được expose.

### Products

| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| `GET` | `/api/v1/products` | Public | Lấy danh sách sản phẩm |
| `GET` | `/api/v1/products/{id}` | Public | Lấy sản phẩm theo ID |
| `POST` | `/api/v1/products` | JWT | Tạo sản phẩm |
| `PUT` | `/api/v1/products/{id}/update` | JWT | Cập nhật sản phẩm |
| `DELETE` | `/api/v1/products/{id}` | JWT | Xóa sản phẩm |

Request tạo/cập nhật sản phẩm:

```json
{
  "name": "Mechanical Keyboard",
  "description": "Wireless mechanical keyboard",
  "price": 129.99,
  "quantity": 20
}
```

Tên sản phẩm tối đa 20 ký tự, mô tả tối đa 255 ký tự, giá và số lượng phải lớn hơn hoặc bằng 1.

## Response format

Response thành công có dạng tổng quát:

```json
{
  "code": 200,
  "message": "Request successful",
  "data": {},
  "timestamps": "2026-08-02T10:00:00"
}
```

Lỗi validation hoặc lỗi nghiệp vụ được xử lý tập trung và trả về thông tin gồm mã lỗi, message, path, errors và timestamp.

## Domain model

- `User`: thông tin tài khoản, role, permission và quan hệ với đơn hàng.
- `Product`: tên, mô tả, giá và số lượng tồn kho.
- `Order`: trạng thái, thời gian đặt, tổng tiền và user sở hữu.
- `OrderItem`: sản phẩm, số lượng và đơn giá trong đơn hàng.
- `OrderStatus`: `PENDING`, `CONFIRMED`, `SHIPPING`, `COMPLETED`, `CANCELLED`.

Hiện tại Orders mới có entity và enum; REST controller, service và repository cho order chưa được triển khai.

## Trạng thái phát triển

### Đã có

- Auth flow với JWT.
- Password hashing bằng BCrypt.
- Product CRUD.
- User query API.
- Validation và xử lý exception tập trung.

### Dự kiến phát triển tiếp

- Order API và checkout flow.
- Phân quyền `USER`/`ADMIN` ở từng endpoint.
- Hoàn thiện update/delete user.
- Refresh token, logout và cơ chế revoke token.
- Chuyển từ H2 in-memory sang database persistent.
- Quản lý cấu hình bằng environment variables.
- Thêm migration bằng Flyway hoặc Liquibase.

## License

Chưa khai báo license cho project.
