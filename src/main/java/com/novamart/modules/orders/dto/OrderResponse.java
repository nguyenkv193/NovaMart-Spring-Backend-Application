package com.novamart.modules.orders.dto;

import com.novamart.modules.orders.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private OrderStatus status;
    private LocalDateTime orderAt;
    private BigDecimal totalAmount;
    private List<OrderItemResponse> items;
}
