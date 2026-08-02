package com.novamart.modules.auth.services.impl;

import com.novamart.common.exception.UnauthorizedException;
import com.novamart.modules.auth.dto.AuthResponse;
import com.novamart.modules.auth.dto.LoginRequest;
import com.novamart.modules.auth.dto.RegisterRequest;
import com.novamart.modules.users.dto.CreateUserRequest;
import com.novamart.modules.users.dto.UserResponse;
import com.novamart.modules.users.enums.Permission;
import com.novamart.modules.users.enums.Role;
import com.novamart.modules.users.services.UserService;
import com.novamart.security.jwt.JwtProperties;
import com.novamart.security.jwt.JwtTokenProvider;
import com.novamart.security.userdetails.UserDetailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserService userService;

    @Mock
    private UserDetailService userDetailService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void shouldRegisterUserAndIssueAccessToken() {
        final RegisterRequest request = registerRequest();
        final UserResponse userResponse = userResponse();
        final UserDetails userDetails = userDetails();
        when(userService.createUser(any())).thenReturn(userResponse);
        when(userDetailService.loadUserByUsername(userResponse.getEmail())).thenReturn(userDetails);
        when(jwtTokenProvider.generateAccessToken(userDetails)).thenReturn("access-token");
        when(jwtProperties.getExpiration()).thenReturn(Duration.ofHours(1));

        final AuthResponse result = authService.register(request);

        assertEquals("access-token", result.getAccessToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(3600L, result.getExpiresIn());
        assertEquals(userResponse, result.getUser());

        final ArgumentCaptor<CreateUserRequest> captor =
                ArgumentCaptor.forClass(CreateUserRequest.class);
        verify(userService).createUser(captor.capture());
        assertEquals(request.getEmail(), captor.getValue().getEmail());
        assertEquals(request.getPassword(), captor.getValue().getPassword());
    }

    @Test
    void shouldLoginAndIssueAccessTokenWhenCredentialsAreValid() {
        final LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password");
        final UserDetails userDetails = userDetails();
        final UserResponse userResponse = userResponse();
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userService.getUserByEmail(userDetails.getUsername())).thenReturn(userResponse);
        when(jwtTokenProvider.generateAccessToken(userDetails)).thenReturn("access-token");
        when(jwtProperties.getExpiration()).thenReturn(Duration.ofHours(1));

        final AuthResponse result = authService.login(request);

        assertEquals("access-token", result.getAccessToken());
        assertEquals(userResponse, result.getUser());
    }

    @Test
    void shouldRejectLoginWhenCredentialsAreInvalid() {
        final LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrong-password");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("invalid credentials"));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    private static RegisterRequest registerRequest() {
        final RegisterRequest request = new RegisterRequest();
        request.setFirstName("Nova");
        request.setLastName("Mart");
        request.setDateOfBirth(LocalDate.of(1999, 2, 3));
        request.setEmail("user@example.com");
        request.setPassword("password");
        return request;
    }

    private static UserDetails userDetails() {
        return User.withUsername("user@example.com")
                .password("encoded-password")
                .roles(Role.USER.name())
                .build();
    }

    private static UserResponse userResponse() {
        return new UserResponse(
                1L,
                "Nova",
                "Mart",
                LocalDate.of(1999, 2, 3),
                "user@example.com",
                Role.USER,
                Permission.READ,
                null,
                null
        );
    }
}
