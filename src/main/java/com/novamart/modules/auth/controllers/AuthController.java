package com.novamart.modules.auth.controllers;

import com.novamart.common.response.ApiResponse;
import com.novamart.modules.auth.constants.AuthMessageConstants;
import com.novamart.modules.auth.dto.AuthResponse;
import com.novamart.modules.auth.dto.LoginRequest;
import com.novamart.modules.auth.dto.RegisterRequest;
import com.novamart.modules.auth.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        AuthMessageConstants.USER_REGISTERED_SUCCESSFULLY,
                        authService.register(registerRequest)
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                AuthMessageConstants.LOGIN_SUCCESSFUL,
                authService.login(loginRequest)
        ));
    }
}
