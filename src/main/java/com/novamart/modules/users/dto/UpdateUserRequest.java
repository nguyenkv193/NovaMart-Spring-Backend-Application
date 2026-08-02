package com.novamart.modules.users.dto;

import com.novamart.common.constants.ValidationMessageConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @NotBlank(message = ValidationMessageConstants.FIELD_REQUIRED)
    private String firstName;

    @NotBlank(message = ValidationMessageConstants.FIELD_REQUIRED)
    private String lastName;

    @NotNull(message = ValidationMessageConstants.FIELD_REQUIRED)
    @Past(message = ValidationMessageConstants.DATE_OF_BIRTH_MUST_BE_IN_PAST)
    private LocalDate dateOfBirth;
}
