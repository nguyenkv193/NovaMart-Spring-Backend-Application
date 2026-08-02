package com.novamart.modules.users.dto;

import com.novamart.common.constants.ValidationMessageConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = ValidationMessageConstants.FIELD_REQUIRED)
    private String firstName;

    @NotBlank(message = ValidationMessageConstants.FIELD_REQUIRED)
    private String lastName;

    @NotNull(message = ValidationMessageConstants.FIELD_REQUIRED)
    @Past(message = ValidationMessageConstants.DATE_OF_BIRTH_MUST_BE_IN_PAST)
    private LocalDate dateOfBirth;

    @NotBlank(message = ValidationMessageConstants.FIELD_REQUIRED)
    @Email(message = ValidationMessageConstants.EMAIL_INVALID)
    private String email;

    @NotBlank(message = ValidationMessageConstants.FIELD_REQUIRED)
    @Size(min = 6, message = ValidationMessageConstants.FIELD_TOO_SHORT)
    private String password;

}
