package com.novamart.modules.auth.dto;

import com.novamart.common.constants.ValidationMessageConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = ValidationMessageConstants.EMAIL_REQUIRED)
    @Email(message = ValidationMessageConstants.EMAIL_INVALID)
    private String email;

    @NotBlank(message = ValidationMessageConstants.PASSWORD_REQUIRED)
    private String password;
}
