package com.novamart.modules.orders.dto;

import com.novamart.common.constants.ValidationMessageConstants;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequest {

    @NotNull(message = ValidationMessageConstants.PRODUCT_ID_REQUIRED)
    @Positive(message = ValidationMessageConstants.FIELD_TOO_SHORT)
    private Long productId;

    @NotNull(message = ValidationMessageConstants.QUANTITY_REQUIRED)
    @Positive(message = ValidationMessageConstants.QUANTITY_MUST_BE_POSITIVE)
    private Integer quantity;
}
