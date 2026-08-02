package com.novamart.modules.users.services.impl;

import com.novamart.common.exception.BadRequestException;
import com.novamart.common.exception.NotFoundException;
import com.novamart.modules.users.dto.CreateUserRequest;
import com.novamart.modules.users.dto.UpdateUserRequest;
import com.novamart.modules.users.dto.UserResponse;
import com.novamart.modules.users.entity.User;
import com.novamart.modules.users.enums.Permission;
import com.novamart.modules.users.enums.Role;
import com.novamart.modules.users.mapper.UserMapper;
import com.novamart.modules.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldCreateUserWithEncodedPasswordAndDefaultAuthorities() {
        final CreateUserRequest request = createUserRequest();
        final User user = new User();
        final UserResponse response = userResponse(1L);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(user);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        final UserResponse result = userService.createUser(request);

        assertSame(response, result);
        assertEquals("encoded-password", user.getPassword());
        assertEquals(Role.USER, user.getRole());
        assertEquals(Permission.READ, user.getPermission());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectUserCreationWhenEmailAlreadyExists() {
        final CreateUserRequest request = createUserRequest();
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> userService.createUser(request));

        verify(userMapper, never()).toEntity(request);
        verify(passwordEncoder, never()).encode(request.getPassword());
    }

    @Test
    void shouldUpdateUserWhenUserExists() {
        final User user = user(1L);
        final UpdateUserRequest request = new UpdateUserRequest(
                "Updated",
                "Name",
                LocalDate.of(1999, 2, 3)
        );
        final UserResponse response = userResponse(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        final UserResponse result = userService.updateUser(1L, request);

        assertSame(response, result);
        assertEquals(request.getFirstName(), user.getFirstName());
        assertEquals(request.getLastName(), user.getLastName());
        assertEquals(request.getDateOfBirth(), user.getDateOfBirth());
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingMissingUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> userService.updateUser(99L, new UpdateUserRequest(
                        "Nova",
                        "Mart",
                        LocalDate.of(1999, 2, 3)
                ))
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldReturnUsersWhenUsersExist() {
        final User firstUser = user(1L);
        final User secondUser = user(2L);
        final UserResponse firstResponse = userResponse(1L);
        final UserResponse secondResponse = userResponse(2L);
        when(userRepository.findAll()).thenReturn(List.of(firstUser, secondUser));
        when(userMapper.toResponse(firstUser)).thenReturn(firstResponse);
        when(userMapper.toResponse(secondUser)).thenReturn(secondResponse);

        final List<UserResponse> result = userService.getUsers();

        assertEquals(List.of(firstResponse, secondResponse), result);
    }

    @Test
    void shouldReturnUserByEmailWhenEmailExists() {
        final User user = user(1L);
        final UserResponse response = userResponse(1L);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        final UserResponse result = userService.getUserByEmail(user.getEmail());

        assertSame(response, result);
    }

    @Test
    void shouldDeleteUserWhenUserExists() {
        final User user = user(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void shouldReturnEmailExistenceFromRepository() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);
        when(userRepository.existsByEmail("missing@example.com")).thenReturn(false);

        assertTrue(userService.existsByEmail("user@example.com"));
        assertFalse(userService.existsByEmail("missing@example.com"));
    }

    private static CreateUserRequest createUserRequest() {
        return CreateUserRequest.builder()
                .firstName("Nova")
                .lastName("Mart")
                .dateOfBirth(LocalDate.of(1999, 2, 3))
                .email("user@example.com")
                .password("password")
                .build();
    }

    private static User user(Long id) {
        final User user = new User();
        user.setId(id);
        user.setFirstName("Nova");
        user.setLastName("Mart");
        user.setDateOfBirth(LocalDate.of(1999, 2, 3));
        user.setEmail("user@example.com");
        user.setRole(Role.USER);
        user.setPermission(Permission.READ);
        return user;
    }

    private static UserResponse userResponse(Long id) {
        return new UserResponse(
                id,
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
