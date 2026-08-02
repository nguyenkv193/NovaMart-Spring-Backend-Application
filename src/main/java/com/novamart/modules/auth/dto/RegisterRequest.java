package com.novamart.modules.auth.dto;

import com.novamart.common.constants.ValidationMessageConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = ValidationMessageConstants.FIRST_NAME_REQUIRED)
    private String firstName;

    @NotBlank(message = ValidationMessageConstants.LAST_NAME_REQUIRED)
    private String lastName;

    @NotNull(message = ValidationMessageConstants.DATE_OF_BIRTH_REQUIRED)
    @Past(message = ValidationMessageConstants.DATE_OF_BIRTH_MUST_BE_IN_PAST)
    private LocalDate dateOfBirth;

    @NotBlank(message = ValidationMessageConstants.EMAIL_REQUIRED)
    @Email(message = ValidationMessageConstants.EMAIL_INVALID)
    private String email;

    @NotBlank(message = ValidationMessageConstants.PASSWORD_REQUIRED)
    @Size(min = 6, message = ValidationMessageConstants.PASSWORD_TOO_SHORT)
    private String password;
}
