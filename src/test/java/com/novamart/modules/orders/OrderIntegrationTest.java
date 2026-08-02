package com.novamart.modules.orders;

import com.novamart.modules.orders.entity.Order;
import com.novamart.modules.orders.entity.OrderItem;
import com.novamart.modules.orders.enums.OrderStatus;
import com.novamart.modules.orders.repository.OrderRepository;
import com.novamart.modules.products.entity.Product;
import com.novamart.modules.products.repository.ProductRepository;
import com.novamart.modules.users.entity.User;
import com.novamart.modules.users.enums.Permission;
import com.novamart.modules.users.enums.Role;
import com.novamart.modules.users.repository.UserRepository;
import com.novamart.security.constants.SecurityConstants;
import com.novamart.security.jwt.JwtTokenProvider;
import com.novamart.security.userdetails.UserDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderIntegrationTest {

    private static final String PASSWORD = "password";
    private static final BigDecimal PRODUCT_PRICE = BigDecimal.valueOf(129.99);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User regularUser;
    private User otherUser;
    private User adminUser;
    private Product product;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        regularUser = userRepository.save(createUser(
                "user@example.com",
                Role.USER,
                Permission.READ
        ));
        otherUser = userRepository.save(createUser(
                "other@example.com",
                Role.USER,
                Permission.READ
        ));
        adminUser = userRepository.save(createUser(
                "admin@example.com",
                Role.ADMIN,
                Permission.DELETE
        ));
        product = productRepository.save(createProduct(5L));
    }

    @Test
    void shouldCreateOrderWithServerTotalSnapshotPriceAndReservedStock() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(regularUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequest(product.getId(), 2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.totalAmount").value(259.98))
                .andExpect(jsonPath("$.data.items[0].unitPrice").value(129.99))
                .andExpect(jsonPath("$.data.items[0].subtotal").value(259.98));

        final Product savedProduct = productRepository.findById(product.getId()).orElseThrow();
        final Order savedOrder = orderRepository.findAllByUser_IdOrderByOrderAtDesc(regularUser.getId())
                .get(0);
        assertEquals(3L, savedProduct.getQuantity());
        assertEquals(BigDecimal.valueOf(259.98), savedOrder.getTotalAmount());
    }

    @Test
    void shouldRejectOrderCreationWhenRequesterIsUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequest(product.getId(), 1)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void shouldRejectCheckoutWhenStockIsInsufficient() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(regularUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderRequest(product.getId(), 6)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));

        assertEquals(5L, productRepository.findById(product.getId()).orElseThrow().getQuantity());
        assertEquals(0, orderRepository.findAllByUser_IdOrderByOrderAtDesc(regularUser.getId()).size());
    }

    @Test
    void shouldReturnOnlyOrdersOwnedByRequester() throws Exception {
        final Order order = saveOrder(regularUser, product, 1);

        mockMvc.perform(get("/api/v1/orders/{id}", order.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(otherUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void shouldAllowAdminToReadOrderOwnedByAnotherUser() throws Exception {
        final Order order = saveOrder(regularUser, product, 1);

        mockMvc.perform(get("/api/v1/orders/{id}", order.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(adminUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(order.getId().intValue()))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void shouldRejectOrderStatusUpdateWhenRequesterIsNotAdmin() throws Exception {
        final Order order = saveOrder(regularUser, product, 1);

        mockMvc.perform(patch("/api/v1/orders/{id}/status", order.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(regularUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONFIRMED\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void shouldAllowAdminToConfirmPendingOrder() throws Exception {
        final Order order = saveOrder(regularUser, product, 1);

        mockMvc.perform(patch("/api/v1/orders/{id}/status", order.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(adminUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONFIRMED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
    }

    @Test
    void shouldRejectInvalidOrderStatusTransition() throws Exception {
        final Order order = saveOrder(regularUser, product, 1);

        mockMvc.perform(patch("/api/v1/orders/{id}/status", order.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(adminUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHIPPING\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void shouldCancelPendingOrderAndRestoreStock() throws Exception {
        final Order order = saveOrder(regularUser, product, 2);
        product.setQuantity(3L);
        productRepository.save(product);

        mockMvc.perform(delete("/api/v1/orders/{id}", order.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(regularUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        assertEquals(5L, productRepository.findById(product.getId()).orElseThrow().getQuantity());
        assertEquals(
                OrderStatus.CANCELLED,
                orderRepository.findById(order.getId()).orElseThrow().getStatus()
        );
    }

    private User createUser(String email, Role role, Permission permission) {
        final User user = new User();
        user.setFirstName("Nova");
        user.setLastName(role.name());
        user.setDateOfBirth(LocalDate.of(2000, 1, 1));
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(PASSWORD));
        user.setRole(role);
        user.setPermission(permission);
        return user;
    }

    private Product createProduct(Long quantity) {
        final Product product = new Product();
        product.setName("Mechanical Keyboard");
        product.setDescription("Wireless mechanical keyboard");
        product.setPrice(PRODUCT_PRICE);
        product.setQuantity(quantity);
        return product;
    }

    private Order saveOrder(User user, Product product, Integer quantity) {
        final Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setOrderAt(LocalDateTime.of(2026, 8, 2, 10, 0));
        order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        order.setUser(user);

        final OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        orderItem.setUnitPrice(product.getPrice());
        order.getItems().add(orderItem);

        return orderRepository.saveAndFlush(order);
    }

    private String bearerToken(User user) {
        return SecurityConstants.BEARER_PREFIX
                + jwtTokenProvider.generateAccessToken(new UserDetail(user));
    }

    private String orderRequest(Long productId, Integer quantity) {
        return String.format(
                "{\"items\":[{\"productId\":%d,\"quantity\":%d}]}",
                productId,
                quantity
        );
    }
}
