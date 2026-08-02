package com.novamart.modules.products.dto;

import com.novamart.common.constants.ValidationMessageConstants;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {

    @NotBlank(message = ValidationMessageConstants.FIELD_REQUIRED)
    @Size(max = 20, message = ValidationMessageConstants.FIELD_TOO_LONG)
    private String name;

    @NotBlank(message = ValidationMessageConstants.FIELD_REQUIRED)
    @Size(max = 255, message = ValidationMessageConstants.FIELD_TOO_LONG)
    private String description;

    @NotNull(message = ValidationMessageConstants.FIELD_REQUIRED)
    @DecimalMin(value = "1.00", message = ValidationMessageConstants.FIELD_TOO_SHORT)
    private BigDecimal price;

    @NotNull(message = ValidationMessageConstants.FIELD_REQUIRED)
    @Positive(message = ValidationMessageConstants.FIELD_TOO_SHORT)
    private Long quantity;
}
