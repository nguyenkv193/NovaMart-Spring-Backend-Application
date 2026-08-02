# NovaMart API Design

Tài liệu này mô tả API contract của NovaMart Backend theo implementation hiện tại. Những phần chưa được expose bởi controller được đánh dấu là **Planned** và không nên được gọi từ client cho đến khi được triển khai.

## 1. Phạm vi module

| Module | Trách nhiệm | Trạng thái API |
|---|---|---|
| `auth` | Đăng ký, đăng nhập và phát hành JWT access token | Implemented |
| `users` | Tra cứu thông tin tài khoản | Implemented một phần |
| `products` | Đọc và quản lý sản phẩm | Implemented |
| `orders` | Mô hình đơn hàng và trạng thái đơn hàng | Planned; hiện mới có entity/enum |

Module `auth` sử dụng service của `users` để tạo tài khoản khi đăng ký. Module `security` chịu trách nhiệm xác thực JWT cho các request cần đăng nhập; đây không phải là một nhóm endpoint riêng.

## 2. Quy ước chung

- Base URL local: `http://localhost:8080`
- API prefix: `/api/v1`
- Content type: `application/json`
- ID resource: số nguyên dạng `Long`
- Ngày: ISO-8601, ví dụ `1998-05-20`
- Thời gian: ISO-8601 `LocalDateTime`, ví dụ `2026-08-02T10:00:00`
- Token: gửi trong header `Authorization: Bearer <access-token>`

Các endpoint không được đánh dấu **Public** yêu cầu JWT hợp lệ.

## 3. Authentication và authorization

### 3.1 Chính sách hiện tại

| Phạm vi | Quyền hiện tại |
|---|---|
| `/api/v1/auth/**` | Public |
| `GET /api/v1/products/**` | Public |
| `GET /api/v1/users/me` | JWT required; chỉ user hiện tại |
| `GET /api/v1/users/{id}` | `ADMIN` hoặc chính user |
| `GET /api/v1/users`, `/api/v1/users/search` | `ADMIN` |
| Product `POST`, `PUT`, `DELETE` | `ADMIN` |
| Các request còn lại | JWT required |

Role (`USER`, `ADMIN`) hiện được dùng để giới hạn các use case quản trị. Permission (`READ`, `WRITE`, `UPDATE`, `DELETE`) đã có trong user model nhưng chưa được dùng làm policy chi tiết.

### 3.2 Luồng xác thực

```text
Client -> POST /auth/register hoặc /auth/login
       <- accessToken (Bearer JWT)
Client -> gọi API protected với Authorization header
       <- API response hoặc 401 nếu token thiếu/không hợp lệ
```

Access token hiện có thời hạn khoảng 1 giờ theo `app.jwt.expiration=1h`.

## 4. Response contract

### 4.1 Response thành công

Các controller hiện tại trả về wrapper `ApiResponse`:

```json
{
  "code": 200,
  "message": "Yêu cầu thành công",
  "data": {},
  "timestamp": "2026-08-02T10:00:00"
}
```

`data` có thể là object, array hoặc `null`. Field thời gian dùng tên `timestamp`.

### 4.2 Response lỗi

Business exception và validation exception được xử lý bởi `GlobalExceptionHandler`:

```json
{
  "code": "BAD_REQUEST",
  "message": "Yêu cầu không hợp lệ",
  "path": "/api/v1/auth/register",
  "errors": {
    "email": "ERR-005 - Email không hợp lệ"
  },
  "timestamp": "2026-08-02T10:00:00"
}
```

Các mã lỗi và HTTP mapping được định nghĩa:

| HTTP | Code | Ý nghĩa |
|---:|---|---|
| 400 | `BAD_REQUEST` | Request không hợp lệ hoặc email đã tồn tại |
| 401 | `UNAUTHORIZED` | Thiếu JWT hoặc thông tin đăng nhập không đúng |
| 403 | `FORBIDDEN` | Đã xác thực nhưng không có quyền thực hiện |
| 404 | `NOT_FOUND` | Không tìm thấy resource |
| 500 | `INTERNAL_ERROR` | Lỗi hệ thống |

Authentication entry point và access denied handler của Spring Security hiện trả JSON `ErrorResponse` thống nhất cho lỗi `401` và `403`.

## 5. Auth API

### 5.1 Đăng ký

```http
POST /api/v1/auth/register
Content-Type: application/json
```

Request:

```json
{
  "firstName": "Nguyen",
  "lastName": "Van A",
  "dateOfBirth": "1998-05-20",
  "email": "user@example.com",
  "password": "secret123"
}
```

| Field | Type | Required | Rule |
|---|---|---:|---|
| `firstName` | string | Yes | Không rỗng |
| `lastName` | string | Yes | Không rỗng |
| `dateOfBirth` | date | Yes | `yyyy-MM-dd` |
| `email` | string | Yes | Email hợp lệ |
| `password` | string | Yes | Tối thiểu 6 ký tự |

Response: `201 Created`

```json
{
  "code": 201,
  "message": "SUC-013 - Đăng ký tài khoản thành công",
  "data": {
    "accessToken": "<jwt>",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "firstName": "Nguyen",
      "lastName": "Van A",
      "dateOfBirth": "1998-05-20",
      "email": "user@example.com",
      "role": "USER",
      "permission": "READ",
      "createdAt": "2026-08-02T10:00:00",
      "updatedAt": "2026-08-02T10:00:00"
    }
  },
  "timestamp": "2026-08-02T10:00:00"
}
```

Tài khoản mới được gán mặc định `role=USER` và `permission=READ`. Password được hash bằng BCrypt và không xuất hiện trong response.

Các lỗi chính:

- `400 BAD_REQUEST`: validation thất bại hoặc email đã tồn tại.
- `401 UNAUTHORIZED`: không áp dụng cho đăng ký hiện tại.

### 5.2 Đăng nhập

```http
POST /api/v1/auth/login
Content-Type: application/json
```

Request:

```json
{
  "email": "user@example.com",
  "password": "secret123"
}
```

| Field | Type | Required | Rule |
|---|---|---:|---|
| `email` | string | Yes | Email hợp lệ |
| `password` | string | Yes | Không rỗng |

Response thành công: `200 OK`, trả về cùng cấu trúc `AuthResponse` như endpoint register.

Sai email hoặc password trả về `401 UNAUTHORIZED` với message `ERR-007 - Email hoặc mật khẩu không hợp lệ`.

### 5.3 Refresh token và logout

Chưa được triển khai. Hiện client cần đăng nhập lại khi access token hết hạn; server chưa có cơ chế revoke token.

## 6. Users API

Module users hiện expose các API đọc. Tạo user bên ngoài được thực hiện qua `POST /api/v1/auth/register`; các API quản trị yêu cầu role `ADMIN`.

### 6.1 Danh sách user

```http
GET /api/v1/users
Authorization: Bearer <access-token>
```

Yêu cầu role `ADMIN`. Response: `200 OK`, `data` là mảng `UserResponse`.

### 6.2 User hiện tại

```http
GET /api/v1/users/me
Authorization: Bearer <access-token>
```

User đã đăng nhập có thể lấy thông tin của chính mình. Response: `200 OK`.

### 6.3 Lấy user theo ID

```http
GET /api/v1/users/{id}
Authorization: Bearer <access-token>
```

User có thể lấy chính mình; `ADMIN` có thể lấy bất kỳ user nào. Nếu không tìm thấy user: `404 NOT_FOUND`.

### 6.4 Tìm user theo email

```http
GET /api/v1/users/search?email=user@example.com
Authorization: Bearer <access-token>
```

Yêu cầu role `ADMIN`. Nếu không tìm thấy email: `404 NOT_FOUND`.

### 6.5 UserResponse

```json
{
  "id": 1,
  "firstName": "Nguyen",
  "lastName": "Van A",
  "dateOfBirth": "1998-05-20",
  "email": "user@example.com",
  "role": "USER",
  "permission": "READ",
  "createdAt": "2026-08-02T10:00:00",
  "updatedAt": "2026-08-02T10:00:00"
}
```

Các API sau chưa được expose:

| Method | Endpoint dự kiến | Trạng thái |
|---|---|---|
| `PUT` | `/api/v1/users/{id}` | Chưa triển khai |
| `DELETE` | `/api/v1/users/{id}` | Chưa triển khai |

## 7. Products API

### 7.1 Danh sách sản phẩm

```http
GET /api/v1/products
```

Public, không yêu cầu JWT. Response: `200 OK`, `data` là mảng sản phẩm.

### 7.2 Chi tiết sản phẩm

```http
GET /api/v1/products/{id}
```

Public, không yêu cầu JWT. Nếu không tìm thấy sản phẩm: `404 NOT_FOUND`.

### 7.3 Tạo sản phẩm

```http
POST /api/v1/products
Authorization: Bearer <access-token>
Content-Type: application/json
```

Request dùng cho cả create và update:

```json
{
  "name": "Mechanical Keyboard",
  "description": "Wireless mechanical keyboard",
  "price": 129.99,
  "quantity": 20
}
```

| Field | Type | Required | Rule |
|---|---|---:|---|
| `name` | string | Yes | Không rỗng, tối đa 20 ký tự |
| `description` | string | Yes | Không rỗng, tối đa 255 ký tự |
| `price` | number | Yes | Giá trị tối thiểu 1 |
| `quantity` | integer | Yes | Giá trị tối thiểu 1 |

Response: `201 Created`.

### 7.4 Cập nhật sản phẩm

```http
PUT /api/v1/products/{id}
Authorization: Bearer <access-token>
Content-Type: application/json
```

Response: `200 OK`.

### 7.5 Xóa sản phẩm

```http
DELETE /api/v1/products/{id}
Authorization: Bearer <access-token>
```

Response hiện tại: `200 OK` với `data: null`. Nếu không tìm thấy sản phẩm: `404 NOT_FOUND`.

### 7.6 ProductResponse

```json
{
  "id": 1,
  "name": "Mechanical Keyboard",
  "description": "Wireless mechanical keyboard",
  "price": 129.99,
  "quantity": 20
}
```

`price` dùng `BigDecimal` với precision/scale phù hợp cho giá trị tiền tệ.

## 8. Orders API — Planned

Module orders hiện có `Order`, `OrderItem` và `OrderStatus`, nhưng chưa có controller, service hoặc repository. Các endpoint dưới đây là đề xuất thiết kế, chưa gọi được ở môi trường hiện tại.

### 8.1 Tạo đơn hàng

```http
POST /api/v1/orders
Authorization: Bearer <access-token>
Content-Type: application/json
```

Request đề xuất:

```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ]
}
```

Business rules đề xuất:

- User lấy từ JWT, không nhận `userId` từ request body.
- Server kiểm tra sản phẩm tồn tại và tồn kho.
- `unitPrice` được snapshot từ giá sản phẩm tại thời điểm đặt hàng.
- `totalAmount` và `status` do server tính/gán; client không được tự gửi.
- Đơn mới có status `PENDING`.

### 8.2 Các endpoint dự kiến

| Method | Endpoint | Quyền dự kiến | Mục đích |
|---|---|---|---|
| `POST` | `/api/v1/orders` | Authenticated user | Tạo đơn hàng |
| `GET` | `/api/v1/orders` | Authenticated user | Danh sách đơn của user hiện tại |
| `GET` | `/api/v1/orders/{id}` | Owner hoặc `ADMIN` | Xem chi tiết đơn |
| `PATCH` | `/api/v1/orders/{id}/status` | `ADMIN` | Cập nhật trạng thái |
| `DELETE` | `/api/v1/orders/{id}` | Owner, trước khi xác nhận | Hủy đơn |

### 8.3 Order status

```text
PENDING -> CONFIRMED -> SHIPPING -> COMPLETED
   |          |
   +----------+------> CANCELLED
```

Các giá trị enum hiện có: `PENDING`, `CONFIRMED`, `SHIPPING`, `COMPLETED`, `CANCELLED`.

## 9. Authorization matrix

### Hiện tại

| Resource | Read | Write |
|---|---|---|
| Auth | Public | Public register/login |
| Users | `ADMIN`; user đọc chính mình | Chưa expose |
| Products | Public | `ADMIN` |
| Orders | Chưa có API | Chưa có API |

### Định hướng

- `USER`: đọc sản phẩm, quản lý đơn hàng của chính mình.
- `ADMIN`: quản lý sản phẩm, xem user, xử lý trạng thái đơn hàng.
- Permission chi tiết (`READ`, `WRITE`, `UPDATE`, `DELETE`) chỉ nên được áp dụng sau khi mapping rõ với từng use case.

## 10. Các quyết định cần chốt trước khi mở rộng API

1. Mapping chi tiết `Permission` (`READ`, `WRITE`, `UPDATE`, `DELETE`) với từng use case nếu cần.
2. Bổ sung pagination/filter/sort cho danh sách users và products.
3. Triển khai order API, refresh token và logout/revoke token.
