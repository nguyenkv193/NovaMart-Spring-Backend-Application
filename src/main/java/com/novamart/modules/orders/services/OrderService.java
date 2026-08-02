package com.novamart.modules.orders.services;

import com.novamart.modules.orders.dto.OrderRequest;
import com.novamart.modules.orders.dto.OrderResponse;
import com.novamart.modules.orders.dto.UpdateOrderStatusRequest;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(OrderRequest orderRequest, Long userId);

    List<OrderResponse> getOrders(Long userId);

    OrderResponse getOrderById(Long orderId, Long requesterId);

    OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest statusRequest);

    OrderResponse cancelOrder(Long orderId, Long userId);
}
