package com.novamart.modules.orders.controllers;

import com.novamart.common.response.ApiResponse;
import com.novamart.modules.orders.constants.OrderMessageConstants;
import com.novamart.modules.orders.dto.OrderRequest;
import com.novamart.modules.orders.dto.OrderResponse;
import com.novamart.modules.orders.dto.UpdateOrderStatusRequest;
import com.novamart.modules.orders.services.OrderService;
import com.novamart.security.userdetails.UserDetail;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderRequest orderRequest,
            @AuthenticationPrincipal UserDetail userDetail
    ) {
        final OrderResponse orderResponse = orderService.createOrder(
                orderRequest,
                userDetail.getUserId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        HttpStatus.CREATED.value(),
                        OrderMessageConstants.ORDER_CREATED_SUCCESSFULLY,
                        orderResponse
                ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrders(
            @AuthenticationPrincipal UserDetail userDetail
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                OrderMessageConstants.ORDERS_FETCHED_SUCCESSFULLY,
                orderService.getOrders(userDetail.getUserId())
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable("id") Long orderId,
            @AuthenticationPrincipal UserDetail userDetail
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                OrderMessageConstants.ORDER_FOUND_SUCCESSFULLY,
                orderService.getOrderById(orderId, userDetail.getUserId())
        ));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable("id") Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest statusRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                OrderMessageConstants.ORDER_STATUS_UPDATED_SUCCESSFULLY,
                orderService.updateOrderStatus(orderId, statusRequest)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable("id") Long orderId,
            @AuthenticationPrincipal UserDetail userDetail
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                HttpStatus.OK.value(),
                OrderMessageConstants.ORDER_CANCELLED_SUCCESSFULLY,
                orderService.cancelOrder(orderId, userDetail.getUserId())
        ));
    }
}
