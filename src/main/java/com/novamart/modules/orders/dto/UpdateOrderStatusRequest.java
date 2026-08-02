package com.novamart.modules.orders.dto;

import com.novamart.common.constants.ValidationMessageConstants;
import com.novamart.modules.orders.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {

    @NotNull(message = ValidationMessageConstants.ORDER_STATUS_REQUIRED)
    private OrderStatus status;
}
