package com.novamart.modules.users.dto;

import com.novamart.constants.MessageConstants;
import com.novamart.modules.users.constants.UserMessageConstants;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Getter
@Setter
@Builder
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = MessageConstants.FIELD_NOT_VALID)
    private String firstName;

    @NotBlank(message = MessageConstants.FIELD_NOT_VALID)
    private String lastName;

    @NotNull(message = MessageConstants.FIELD_NOT_VALID)
    private LocalDate dateOfBirth;

    @NotBlank(message = MessageConstants.FIELD_NOT_VALID)
    @Email(message = UserMessageConstants.EMAIL_NOT_FOUND)
    private String email;

    @NotBlank(message = MessageConstants.FIELD_NOT_VALID)
    @Size(min = 6, message = MessageConstants.FIELD_NOT_LESS_THAN)
    private String password;

}