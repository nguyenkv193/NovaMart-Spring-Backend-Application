package com.novamart.modules.users.dto;

import com.novamart.modules.users.enums.Permission;
import com.novamart.modules.users.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String email;
    private Role role;
    private Permission permission;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
