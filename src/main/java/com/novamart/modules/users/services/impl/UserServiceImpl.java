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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        if (userRepository.existsByEmail(createUserRequest.getEmail())) {
            throw new BadRequestException(UserMessageConstants.EMAIL_ALREADY_EXISTS);
        }

        final User user = userMapper.toEntity(createUserRequest);
        user.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
        user.setRole(Role.USER);
        user.setPermission(Permission.READ);

        final User savedUser = userRepository.save(user);
        log.info("User created with id={}", savedUser.getId());

        return userMapper.toResponse(savedUser);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<UserResponse> getUsers() {
        final List<User> users = userRepository.findAll();

        return users
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or #a0 == authentication.principal.userId")
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return userMapper.toResponse(findUserById(id));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        return userMapper.toResponse(findUserByEmail(email));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        return userMapper.toResponse(findUserByEmail(email));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public UserResponse getUserByEmailForAdmin(String email) {
        return getUserByEmail(email);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or #a0 == authentication.principal.userId")
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest updateUserRequest) {
        final User user = findUserById(id);
        user.setFirstName(updateUserRequest.getFirstName());
        user.setLastName(updateUserRequest.getLastName());
        user.setDateOfBirth(updateUserRequest.getDateOfBirth());

        final User updatedUser = userRepository.save(user);
        log.info("User updated with id={}", updatedUser.getId());

        return userMapper.toResponse(updatedUser);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or #a0 == authentication.principal.userId")
    @Transactional
    public void deleteUser(Long id) {
        final User user = findUserById(id);
        userRepository.delete(user);
        log.info("User deleted with id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private User findUserById(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(String.format(UserMessageConstants.USER_NOT_FOUND, id)));
    }

    private User findUserByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new NotFoundException(String.format(UserMessageConstants.EMAIL_NOT_FOUND, email)));
    }
}
