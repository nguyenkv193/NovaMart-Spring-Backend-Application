package com.novamart.modules.orders.dto;

import com.novamart.common.constants.ValidationMessageConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotEmpty(message = ValidationMessageConstants.ORDER_ITEMS_REQUIRED)
    @Valid
    private List<OrderItemRequest> items;
}
