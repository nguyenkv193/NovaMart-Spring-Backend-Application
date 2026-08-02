package com.novamart.modules.users.controllers;

import com.novamart.common.response.ApiResponse;
import com.novamart.modules.users.constants.UserMessageConstants;
import com.novamart.modules.users.dto.UserResponse;
import com.novamart.modules.users.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUser() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                HttpStatus.OK.value(),
                                UserMessageConstants.USERS_FETCHED_SUCCESSFULLY,
                                userService.getUsers()
                        )
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                HttpStatus.OK.value(),
                                String.format(UserMessageConstants.USER_FOUND_SUCCESSFULLY, id),
                                userService.getUserById(id)
                        )
                );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@RequestParam String email) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        ApiResponse.success(
                                HttpStatus.OK.value(),
                                String.format("Tìm thấy người dùng với email %s", email),
                                userService.getUserByEmail(email)
                        )
                );
    }
}
