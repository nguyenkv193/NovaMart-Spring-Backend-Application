package com.novamart.security;

import com.novamart.modules.products.repository.ProductRepository;
import com.novamart.modules.users.entity.User;
import com.novamart.modules.users.enums.Permission;
import com.novamart.modules.users.enums.Role;
import com.novamart.modules.users.repository.UserRepository;
import com.novamart.security.jwt.JwtTokenProvider;
import com.novamart.security.constants.SecurityConstants;
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

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoleBasedAuthorizationIntegrationTest {

    private static final String PASSWORD = "password";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User regularUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        userRepository.deleteAll();

        regularUser = userRepository.save(createUser(
                "user@example.com",
                Role.USER,
                Permission.READ
        ));
        adminUser = userRepository.save(createUser(
                "admin@example.com",
                Role.ADMIN,
                Permission.DELETE
        ));
    }

    @Test
    void shouldRejectProductCreationWhenUserIsNotAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(regularUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequest()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void shouldAllowProductCreationWhenUserIsAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(adminUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Mechanical Keyboard"));
    }

    @Test
    void shouldRejectUserListWhenRequesterIsNotAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(regularUser)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void shouldAllowUserListWhenRequesterIsAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(adminUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void shouldReturnCurrentUserForAuthenticatedRequester() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(regularUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("user@example.com"));
    }

    @Test
    void shouldReturnOwnUserByIdForRegularUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", regularUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(regularUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("user@example.com"));
    }

    @Test
    void shouldRejectUserLookupWhenRegularUserDoesNotOwnUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", adminUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearerToken(regularUser)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void shouldAllowLoginAfterUserSearchIsProtected() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty());
    }

    @Test
    void shouldReturnUnauthorizedJsonWhenRequestIsUnauthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productRequest()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void shouldReturnUnauthorizedJsonWhenUnauthenticatedUserListsUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
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

    private String bearerToken(User user) {
        return SecurityConstants.BEARER_PREFIX
                + jwtTokenProvider.generateAccessToken(new UserDetail(user));
    }

    private String productRequest() {
        return """
                {
                  "name": "Mechanical Keyboard",
                  "description": "Wireless mechanical keyboard",
                  "price": 129.99,
                  "quantity": 5
                }
                """;
    }
}
