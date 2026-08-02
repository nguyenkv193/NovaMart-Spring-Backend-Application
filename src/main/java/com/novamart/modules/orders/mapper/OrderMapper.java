package com.novamart.modules.orders.mapper;

import com.novamart.modules.orders.dto.OrderItemResponse;
import com.novamart.modules.orders.dto.OrderResponse;
import com.novamart.modules.orders.entity.Order;
import com.novamart.modules.orders.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    OrderResponse toResponse(Order order);

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "subtotal", ignore = true)
    OrderItemResponse toItemResponse(OrderItem orderItem);
}
