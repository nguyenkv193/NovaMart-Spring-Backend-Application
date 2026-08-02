package com.novamart.modules.users.dto;

import com.novamart.constants.MessageConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateUserRequest {

    @NotBlank(message = MessageConstants.FIELD_NOT_VALID)
    @Size(min = 1)
    private String firstName;

    @NotBlank(message = MessageConstants.FIELD_NOT_VALID)
    @Size(min = 1)
    private String lastName;

    @NotNull(message = MessageConstants.FIELD_NOT_VALID)
    @Min(value = 10, message = MessageConstants.FIELD_NOT_LESS_THAN)
    private LocalDate dateOfBirth;
}