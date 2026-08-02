package com.novamart.modules.auth.services;

import com.novamart.modules.auth.dto.AuthResponse;
import com.novamart.modules.auth.dto.LoginRequest;
import com.novamart.modules.auth.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest registerRequest);

    AuthResponse login(LoginRequest loginRequest);
}
