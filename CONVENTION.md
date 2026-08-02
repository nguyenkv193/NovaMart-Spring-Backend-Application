# NovaMart Engineering Convention

Tài liệu này là coding convention và testing convention chính của NovaMart Backend. Mọi code mới hoặc code được chỉnh sửa trong project phải tuân theo các quy tắc dưới đây.

## 1. Project baseline

- Java 17.
- Spring Boot 4.x.
- Gradle Wrapper là build tool chuẩn của project.
- Root package: `com.novamart`.
- API prefix: `/api/v1`.
- Kiến trúc chính: package-by-feature kết hợp các layer Controller, Service, Repository.
- Mọi file source dùng UTF-8.

Không đổi package root, tên module hoặc public API hiện có nếu task không yêu cầu rõ ràng. Phải giữ lại các thay đổi có sẵn của người dùng và chỉ chỉnh file liên quan đến task.

## 2. Naming convention

| Thành phần | Quy tắc | Ví dụ |
|---|---|---|
| Package | lowercase, không dùng dấu gạch dưới | `com.novamart.modules.products` |
| Class/interface | PascalCase | `ProductController`, `ProductService` |
| Method/field | camelCase | `getProductById`, `productRepository` |
| Constant | UPPER_SNAKE_CASE | `MAX_PAGE_SIZE` |
| Entity | Danh từ số ít | `User`, `Product`, `Order` |
| Request DTO | `<Feature>Request` | `ProductRequest` |
| Response DTO | `<Feature>Response` | `ProductResponse` |
| Controller | `<Feature>Controller` | `AuthController` |
| Service | `<Feature>Service` và `<Feature>ServiceImpl` | `ProductServiceImpl` |
| Repository | `<Entity>Repository` | `ProductRepository` |
| Mapper | `<Feature>Mapper` | `ProductMapper` |
| Test unit | `<ClassName>Test` | `ProductServiceImplTest` |
| Test integration | `<Feature>IntegrationTest` | `RoleBasedAuthorizationIntegrationTest` |

Tên method test nên mô tả hành vi, ưu tiên dạng:

```text
should<ExpectedResult>When<Condition>
```

Ví dụ: `shouldRejectProductCreationWhenUserIsNotAdmin`.

## 3. Package structure

Giữ cấu trúc package-by-feature hiện tại:

```text
com.novamart
├── common
│   ├── constants
│   ├── exception
│   └── response
├── config
├── modules
│   ├── auth
│   ├── users
│   ├── products
│   └── orders
└── security
    ├── config
    ├── jwt
    └── userdetails
```

Trong mỗi business module, dùng các package phù hợp:

```text
<module>
├── controllers
├── dto
├── entity
├── enums
├── mapper
├── repository
└── services
    └── impl
```

Không đặt business code mới vào một package `utils` hoặc `helpers` chung nếu code đó chỉ phục vụ một module.

## 4. Layering and dependency rules

### Controller

- Chỉ xử lý HTTP mapping, input validation, status code và response.
- Dùng `@Valid` cho request body cần validation.
- Không truy cập Repository trực tiếp.
- Không chứa business rule, tính tổng tiền, kiểm tra tồn kho hoặc xử lý quyền sở hữu.
- Inject service interface, không inject class `*ServiceImpl`.
- Không expose Entity trực tiếp ra API; dùng Response DTO.

### Service

- Chứa business rule và transaction boundary.
- Dùng interface cho service public và class `impl` cho implementation.
- Dùng constructor injection, có thể dùng Lombok `@RequiredArgsConstructor`.
- Dùng `@Transactional` cho use case có nhiều thao tác ghi cần tính nguyên tử.
- Authorization nghiệp vụ đặt tại service bằng `@PreAuthorize` hoặc policy method.
- Không trả password, secret, JWT signing key hoặc thông tin nội bộ ra response.

### Repository

- Chỉ chứa persistence query.
- Dùng Spring Data JPA repository.
- Không đặt business rule trong Repository.
- Query method phải có tên thể hiện rõ điều kiện tìm kiếm.

### Mapper and DTO

- Dùng MapStruct cho mapping giữa Entity và DTO khi phù hợp.
- Request DTO chỉ nhận dữ liệu client được phép gửi.
- Response DTO chỉ trả dữ liệu client cần biết.
- Tiền tệ dùng `BigDecimal`, không dùng `double` hoặc `float`.
- Ngày sinh dùng `LocalDate`; thời điểm tạo/cập nhật dùng `LocalDateTime` hoặc `Instant` theo contract.

### Function and method convention

- Mỗi method chỉ nên có một responsibility rõ ràng.
- Tên method phải là động từ thể hiện hành động: `findById`, `createOrder`, `calculateTotal`.
- Method query không được tạo side effect; method command phải thể hiện rõ việc thay đổi state.
- Không trả `null` cho kết quả business thông thường. Dùng exception cho resource bắt buộc phải tồn tại, hoặc `Optional` ở tầng query phù hợp.
- Không để method stub (`return null`, `return false`, method rỗng) trong flow đã được expose.
- Giữ method ngắn và ít nhánh. Khi một method có nhiều trách nhiệm, tách thành private method có tên mô tả rõ mục đích.
- Tránh quá ba tham số cho một method; nếu nhiều dữ liệu liên quan, tạo Request/Command DTO.
- Không truyền Entity từ Controller vào Service nếu request DTO là đủ.
- Không dùng boolean parameter mơ hồ như `process(true, false)`; dùng enum hoặc object có tên rõ nghĩa.
- Guard clause và validation nên đặt sớm để giảm nesting.
- Không dùng `catch (Exception)` để che lỗi. Chỉ catch exception khi có thể xử lý, chuyển đổi hoặc bổ sung context hợp lý.
- Không đặt business logic trong getter/setter, mapper hoặc utility chung.
- Method public phải có contract rõ ràng về input, output, exception và authorization.
- Dùng `final` cho dependency và local value không cần thay đổi.

### Logging convention

- Dùng SLF4J qua Lombok `@Slf4j` hoặc `LoggerFactory`; không dùng `System.out`, `System.err` hoặc `printStackTrace`.
- Dùng message parameterized với `{}`, không nối chuỗi thủ công:

  ```java
  log.info("Product created with id={}", productId);
  ```

- Chọn log level theo mục đích:
  - `ERROR`: lỗi hệ thống hoặc exception không thể xử lý tại boundary; kèm exception object.
  - `WARN`: request bị từ chối, dữ liệu bất thường hoặc fallback có thể ảnh hưởng vận hành.
  - `INFO`: business event quan trọng ở mức vừa phải như tạo order hoặc thay đổi trạng thái.
  - `DEBUG`: thông tin chẩn đoán phục vụ development/troubleshooting.
  - `TRACE`: chỉ dùng tạm thời cho flow rất chi tiết, không để mặc định trong production.
- Không log password, access token, refresh token, JWT secret, request body nhạy cảm hoặc thông tin thanh toán.
- Hạn chế log email/PII; ưu tiên `userId`, `orderId`, `productId` hoặc giá trị đã mask.
- Không log cùng một exception ở nhiều layer. Layer xử lý cuối cùng nên log stack trace; layer bên trên chỉ chuyển tiếp.
- Không dùng logging để thay thế exception handling hoặc validation.
- Log message phải có context đủ để điều tra, nhưng không chứa dữ liệu nhạy cảm.

## 5. REST API convention

- Dùng resource name số nhiều: `/products`, `/users`, `/orders`.
- Không dùng action verb trong URL mới. Ưu tiên `PUT /api/v1/products/{id}` thay vì `/products/{id}/update`.
- Dùng HTTP method đúng ngữ nghĩa:
  - `GET`: đọc dữ liệu.
  - `POST`: tạo resource hoặc thực hiện command không idempotent.
  - `PUT`: thay thế/cập nhật toàn bộ resource.
  - `PATCH`: cập nhật một phần resource.
  - `DELETE`: xóa hoặc hủy resource theo contract.
- Status code chuẩn:
  - `200 OK`: đọc hoặc cập nhật thành công.
  - `201 Created`: tạo thành công.
  - `204 No Content`: thành công nhưng không có response body.
  - `400 Bad Request`: request không hợp lệ.
  - `401 Unauthorized`: thiếu hoặc không hợp lệ JWT.
  - `403 Forbidden`: đã xác thực nhưng không đủ quyền.
  - `404 Not Found`: không tìm thấy resource.
  - `409 Conflict`: xung đột trạng thái hoặc dữ liệu.
- Endpoint mới phải dùng wrapper response chung của project (`ApiResponse` hoặc `ErrorResponse`).
- Response wrapper dùng field `errors` và `timestamp`; không tạo thêm biến thể typo cho endpoint mới.

## 6. Validation convention

- Validate ở boundary bằng Jakarta Validation.
- Dùng `@NotBlank` cho text bắt buộc.
- Dùng `@Email` cho email.
- Dùng `@Past` cho ngày sinh trong quá khứ.
- Dùng `@DecimalMin` cho giá trị tiền tệ.
- Dùng `@Min` hoặc `@Positive` cho số nguyên.
- Không dùng annotation số học như `@Min` trên `LocalDate`.
- Message validation phải rõ field và giữ format lỗi thống nhất.

## 7. Security convention

- Password luôn hash bằng BCrypt trước khi lưu.
- JWT secret và credential phải lấy từ environment variable hoặc secret manager ở môi trường deploy.
- Không log password, access token, refresh token hoặc secret.
- SecurityConfig chỉ định public route và yêu cầu authenticated tổng quát.
- Rule nghiệp vụ dùng method security với `@PreAuthorize` hoặc policy rõ ràng.
- Dùng `hasRole('ADMIN')` cho role; không tự nối thêm `ROLE_` trong expression vì `UserDetail` đã mapping role authority.
- User thường chỉ được đọc/sửa dữ liệu của chính mình; ADMIN mới được thao tác dữ liệu quản trị.
- Authorization phải fail closed: nếu không xác định được identity hoặc ownership thì từ chối.
- Public endpoint hiện tại:
  - `/api/v1/auth/**`.
  - `GET /api/v1/products/**`.
- Product write và API quản trị users yêu cầu `ADMIN`.
- Không dùng `Permission` để tạo policy phức tạp cho đến khi model hỗ trợ nhiều permission trên một user.

## 8. Exception and error handling

- Dùng custom `BusinessException` hoặc subclass phù hợp cho lỗi nghiệp vụ.
- Không bắt exception chung chung rồi bỏ qua lỗi.
- Controller không tự format error response.
- `GlobalExceptionHandler` chịu trách nhiệm xử lý business exception và validation exception.
- Security entry point/access denied handler phải trả response JSON thống nhất.
- Không trả stack trace hoặc thông tin database cho client.

## 9. JPA and database convention

- Table dùng tên số nhiều, snake_case: `users`, `products`, `orders`, `order_items`.
- Field tiền tệ dùng `BigDecimal` và phải thống nhất precision/scale khi schema được chốt.
- Email user phải unique ở cả application level và database level.
- Quan hệ collection khởi tạo collection rỗng, không để `null` nếu có thể.
- Dùng `LAZY` cho quan hệ `ManyToOne`/collection khi phù hợp.
- Cẩn thận với bidirectional relationship để tránh vòng lặp JSON và N+1 query.
- Không trừ tồn kho hoặc tạo order ngoài transaction.

## 10. Unit test convention

### Nguyên tắc chung

- Dùng JUnit 5.
- Test theo mô hình Arrange - Act - Assert.
- Mỗi test kiểm tra một hành vi chính.
- Test độc lập, deterministic, không phụ thuộc thứ tự chạy.
- Không gọi network hoặc database thật trong unit test.
- Mock dependency trực tiếp của class đang test; không mock class đang test.
- Tên test mô tả kết quả mong đợi và điều kiện gây ra kết quả đó.
- Không test private method trực tiếp; test qua public behavior.
- Không test getter/setter hoặc behavior do framework cung cấp nếu không có business rule riêng.
- Không assert log message trong unit test thông thường; chỉ test logging khi log là một phần bắt buộc của behavior hoặc audit contract.
- Dùng fixture/factory cho object phức tạp, tránh lặp lại setup dài trong mọi test.
- Không dùng `Thread.sleep`, random không có seed hoặc thời gian hệ thống trực tiếp trong test; inject `Clock` nếu logic phụ thuộc thời gian.

### Service unit test

Service unit test ưu tiên `@ExtendWith(MockitoExtension.class)`, `@Mock` và `@InjectMocks`. Phải bao phủ tối thiểu:

- Happy path.
- Resource không tồn tại.
- Dữ liệu trùng hoặc không hợp lệ.
- Business rule bị vi phạm.
- Repository được gọi đúng số lần và đúng tham số khi điều đó có ý nghĩa.
- Password được encode, không lưu plaintext.
- Dependency được verify ở mức behavior cần thiết, không verify implementation detail không mang giá trị.
- Exception phải được assert theo type và thông tin quan trọng, không assert toàn bộ message nếu message không phải contract.

### Controller/security integration test

Dùng `@WebMvcTest` cho controller slice khi chỉ cần test mapping/validation; dùng `@SpringBootTest` và `MockMvc` cho flow có security, JPA hoặc wiring thật. Mỗi protected endpoint nên có test cho:

- Không có token -> `401`.
- Token hợp lệ nhưng sai role -> `403`.
- Đúng role -> success.
- Đúng role nhưng không sở hữu resource -> `403` hoặc `404` theo policy.
- Response body đúng schema.

Test data phải được chuẩn bị rõ ràng trong `@BeforeEach` hoặc fixture; không dựa vào dữ liệu còn sót từ test khác.

### Test naming and structure

Ưu tiên tên test thể hiện kết quả:

```java
void shouldRejectProductCreationWhenUserIsNotAdmin()
```

Giữ cấu trúc test dễ đọc:

```java
// Arrange
ProductRequest request = validProductRequest();

// Act
ProductResponse result = productService.createProduct(request);

// Assert
assertThat(result.getName()).isEqualTo(request.getName());
```

Mỗi test nên có một lý do chính để fail. Nếu có nhiều scenario cùng cấu trúc, dùng parameterized test hoặc fixture thay vì copy/paste quá nhiều.

### Order and inventory test

Khi Orders được triển khai, bắt buộc test:

- Không đủ tồn kho.
- Giá snapshot tại thời điểm đặt hàng.
- Tính tổng tiền phía server.
- User không xem được order của user khác.
- Chuyển trạng thái không hợp lệ bị từ chối.
- Rollback khi một bước trong checkout thất bại.

## 11. Code quality

- Không dùng wildcard import trong code mới.
- Không để unused import, dead code hoặc method stub trong endpoint đã expose.
- Không dùng magic number/string nếu có thể đặt constant có tên rõ nghĩa.
- Ưu tiên code dễ đọc hơn abstraction sớm.
- Comment giải thích lý do hoặc business rule, không comment lại điều code đã nói rõ.
- Không thêm framework/dependency mới nếu JDK hoặc Spring hiện tại đã giải quyết được nhu cầu.
- Khi refactor, giữ thay đổi nhỏ và chạy test sau mỗi nhóm thay đổi.

## 12. Project context and technical debt

Các điểm sau đã biết và cần được xử lý có chủ đích, không âm thầm thay đổi trong task khác:

- H2 hiện là database in-memory; dữ liệu mất khi restart.
- JWT secret được lấy từ biến môi trường; production nên quản lý bằng secret manager.
- Orders mới có entity/enum, chưa có API hoàn chỉnh.
- `Permission` hiện là một giá trị trên User; chưa phải mô hình nhiều permission.
