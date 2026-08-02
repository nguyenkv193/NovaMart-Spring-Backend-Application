package com.novamart.modules.auth.services.impl;

import com.novamart.common.exception.UnauthorizedException;
import com.novamart.modules.auth.constants.AuthMessageConstants;
import com.novamart.modules.auth.dto.AuthResponse;
import com.novamart.modules.auth.dto.LoginRequest;
import com.novamart.modules.auth.dto.RegisterRequest;
import com.novamart.modules.auth.services.AuthService;
import com.novamart.modules.users.dto.CreateUserRequest;
import com.novamart.modules.users.dto.UserResponse;
import com.novamart.modules.users.services.UserService;
import com.novamart.security.constants.SecurityConstants;
import com.novamart.security.jwt.JwtProperties;
import com.novamart.security.jwt.JwtTokenProvider;
import com.novamart.security.userdetails.UserDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserDetailService userDetailService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        final CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .dateOfBirth(registerRequest.getDateOfBirth())
                .email(registerRequest.getEmail())
                .password(registerRequest.getPassword())
                .build();

        final UserResponse user = userService.createUser(createUserRequest);

        final UserDetails userDetails = userDetailService.loadUserByUsername(user.getEmail());

        return createAuthResponse(userDetails, user);
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            final Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            final UserResponse user = userService.getUserByEmail(userDetails.getUsername());

            return createAuthResponse(userDetails, user);
        } catch (AuthenticationException exception) {
            log.warn(
                    "Authentication failed for login request: {}",
                    exception.getClass().getSimpleName()
            );
            throw new UnauthorizedException(AuthMessageConstants.INVALID_CREDENTIALS);
        }
    }

    private AuthResponse createAuthResponse(UserDetails userDetails, UserResponse user) {
        return new AuthResponse(
                jwtTokenProvider.generateAccessToken(userDetails),
                SecurityConstants.BEARER_TOKEN_TYPE,
                jwtProperties.getExpiration().toSeconds(),
                user
        );
    }
}
