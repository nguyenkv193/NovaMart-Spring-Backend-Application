package com.novamart.modules.users.services.impl;

import com.novamart.common.exception.BadRequestException;
import com.novamart.common.exception.NotFoundException;
import com.novamart.modules.users.constants.UserMessageConstants;
import com.novamart.modules.users.dto.CreateUserRequest;
import com.novamart.modules.users.dto.UpdateUserRequest;
import com.novamart.modules.users.dto.UserResponse;
import com.novamart.modules.users.entity.User;
import com.novamart.modules.users.enums.Permission;
import com.novamart.modules.users.enums.Role;
import com.novamart.modules.users.mapper.UserMapper;
import com.novamart.modules.users.repository.UserRepository;
import com.novamart.modules.users.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        if(userRepository.existsByEmail(createUserRequest.getEmail())) {
            throw new BadRequestException(UserMessageConstants.EMAIL_ALREADY_EXISTS);
        }

        User user = userMapper.toEntity(createUserRequest);
        user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
        user.setRole(Role.USER);
        user.setPermission(Permission.READ);

        return userMapper.toDTO(userRepository.save(user));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or #a0 == authentication.principal.userId")
    public UserResponse getUserById(Long id) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(UserMessageConstants.USER_NOT_FOUND, id)));

        return userMapper.toDTO(user);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public UserResponse getCurrentUser(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new NotFoundException(String.format(UserMessageConstants.EMAIL_NOT_FOUND, email)));

        return userMapper.toDTO(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new NotFoundException(String.format(UserMessageConstants.EMAIL_NOT_FOUND, email)));

        return userMapper.toDTO(user);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or #a0 == authentication.principal.userId")
    public UserResponse updateUser(Long id, UpdateUserRequest updateUserRequest) {
        return null;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or #a0 == authentication.principal.userId")
    public void deleteUser(Long id) {

    }

    @Override
    public boolean existsByEmail(String email) {
        return false;
    }
}
