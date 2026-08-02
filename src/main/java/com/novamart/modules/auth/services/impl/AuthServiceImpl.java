package com.novamart.modules.auth.services.impl;

import com.novamart.common.exception.UnauthorizedException;
import com.novamart.modules.auth.dto.AuthResponse;
import com.novamart.modules.auth.dto.LoginRequest;
import com.novamart.modules.auth.dto.RegisterRequest;
import com.novamart.modules.auth.services.AuthService;
import com.novamart.modules.users.dto.CreateUserRequest;
import com.novamart.modules.users.dto.UserResponse;
import com.novamart.modules.users.services.UserService;
import com.novamart.security.jwt.JwtProperties;
import com.novamart.security.jwt.JwtTokenProvider;
import com.novamart.security.userdetails.UserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserDetailService userDetailService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        UserResponse user = userService.createUser(
                CreateUserRequest.builder()
                        .firstName(registerRequest.getFirstName())
                        .lastName(registerRequest.getLastName())
                        .dateOfBirth(registerRequest.getDateOfBirth())
                        .email(registerRequest.getEmail())
                        .password(registerRequest.getPassword())
                        .build()
        );

        UserDetails userDetails = userDetailService.loadUserByUsername(user.getEmail());

        return createAuthResponse(userDetails, user);
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            UserResponse user = userService.getUserByEmail(userDetails.getUsername());

            return createAuthResponse(userDetails, user);
        } catch (AuthenticationException exception) {
            throw new UnauthorizedException("Invalid email or password");
        }
    }

    private AuthResponse createAuthResponse(UserDetails userDetails, UserResponse user) {
        return new AuthResponse(
                jwtTokenProvider.generateAccessToken(userDetails),
                "Bearer",
                jwtProperties.getExpiration().toSeconds(),
                user
        );
    }
}
