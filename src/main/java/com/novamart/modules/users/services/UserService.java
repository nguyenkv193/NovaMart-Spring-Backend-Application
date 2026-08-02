package com.novamart.modules.users.services;

import com.novamart.modules.users.dto.CreateUserRequest;
import com.novamart.modules.users.dto.UpdateUserRequest;
import com.novamart.modules.users.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(CreateUserRequest createUserRequest);

    List<UserResponse> getUsers();

    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);

    UserResponse updateUser(Long id, UpdateUserRequest updateUserRequest);

    void deleteUser(Long id);

    boolean existsByEmail(String email);
}
