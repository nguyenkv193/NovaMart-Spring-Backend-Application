package com.novamart.modules.products.dto;

import com.novamart.constants.MessageConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = MessageConstants.FIELD_NOT_VALID)
    @Size(max = 20, message = MessageConstants.FIELD_NOT_GREATER_THAN)
    private String name;

    @NotBlank(message = MessageConstants.FIELD_NOT_VALID)
    @Size(max = 255, message = MessageConstants.FIELD_NOT_GREATER_THAN)
    private String description;

    @NotNull(message = MessageConstants.FIELD_NOT_VALID)
    @Min(value = 1, message = MessageConstants.FIELD_NOT_LESS_THAN)
    private BigDecimal price;

    @NotNull(message = MessageConstants.FIELD_NOT_VALID)
    @Min(value = 1, message = MessageConstants.FIELD_NOT_LESS_THAN)
    private Long quantity;
}
