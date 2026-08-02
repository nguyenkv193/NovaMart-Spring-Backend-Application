package com.novamart.modules.orders.services.impl;

import com.novamart.common.exception.ConflictException;
import com.novamart.common.exception.NotFoundException;
import com.novamart.modules.orders.dto.OrderItemRequest;
import com.novamart.modules.orders.dto.OrderRequest;
import com.novamart.modules.orders.dto.OrderResponse;
import com.novamart.modules.orders.dto.UpdateOrderStatusRequest;
import com.novamart.modules.orders.entity.Order;
import com.novamart.modules.orders.entity.OrderItem;
import com.novamart.modules.orders.enums.OrderStatus;
import com.novamart.modules.orders.mapper.OrderMapper;
import com.novamart.modules.orders.policy.OrderAccessPolicy;
import com.novamart.modules.orders.repository.OrderRepository;
import com.novamart.modules.products.entity.Product;
import com.novamart.modules.products.repository.ProductRepository;
import com.novamart.modules.users.entity.User;
import com.novamart.modules.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderAccessPolicy orderAccessPolicy;

    @Mock
    private Clock applicationClock;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void shouldCreateOrderWithSnapshotPriceServerTotalAndReservedStock() {
        final User user = user(7L);
        final Product product = product(2L, BigDecimal.valueOf(125.50), 5L);
        final OrderRequest request = new OrderRequest(List.of(new OrderItemRequest(2L, 2)));
        final OrderResponse response = new OrderResponse();
        response.setStatus(OrderStatus.PENDING);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(productRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(response);
        when(applicationClock.getZone()).thenReturn(ZoneOffset.UTC);
        when(applicationClock.instant()).thenReturn(Instant.parse("2026-08-02T10:00:00Z"));

        final OrderResponse result = orderService.createOrder(request, 7L);

        assertSame(response, result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        final Order savedOrder = captureSavedOrder();
        assertEquals(OrderStatus.PENDING, savedOrder.getStatus());
        assertEquals(BigDecimal.valueOf(251.00), savedOrder.getTotalAmount());
        assertEquals(1, savedOrder.getItems().size());
        assertEquals(BigDecimal.valueOf(125.50), savedOrder.getItems().get(0).getUnitPrice());
        assertEquals(3L, product.getQuantity());
    }

    @Test
    void shouldRejectOrderWhenStockIsInsufficient() {
        final Product product = product(2L, BigDecimal.TEN, 1L);
        final OrderRequest request = new OrderRequest(List.of(new OrderItemRequest(2L, 2)));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user(7L)));
        when(productRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(product));

        assertThrows(ConflictException.class, () -> orderService.createOrder(request, 7L));

        assertEquals(1L, product.getQuantity());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldKeepEarlierProductStockWhenLaterProductIsMissing() {
        final Product firstProduct = product(2L, BigDecimal.TEN, 5L);
        final OrderRequest request = new OrderRequest(List.of(
                new OrderItemRequest(2L, 2),
                new OrderItemRequest(99L, 1)
        ));
        when(userRepository.findById(7L)).thenReturn(Optional.of(user(7L)));
        when(productRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(firstProduct));
        when(productRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.createOrder(request, 7L));

        assertEquals(5L, firstProduct.getQuantity());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldReturnOrderWhenRequesterOwnsIt() {
        final Order order = order(10L, 7L, OrderStatus.PENDING);
        final OrderResponse response = new OrderResponse();
        when(orderAccessPolicy.isAdmin()).thenReturn(false);
        when(orderRepository.findByIdAndUser_Id(10L, 7L)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);

        final OrderResponse result = orderService.getOrderById(10L, 7L);

        assertSame(response, result);
    }

    @Test
    void shouldRejectOrderLookupWhenRequesterDoesNotOwnIt() {
        when(orderAccessPolicy.isAdmin()).thenReturn(false);
        when(orderRepository.findByIdAndUser_Id(10L, 7L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> orderService.getOrderById(10L, 7L));

        verify(orderRepository, never()).findById(10L);
    }

    @Test
    void shouldCancelPendingOrderAndRestoreStock() {
        final Product product = product(2L, BigDecimal.TEN, 3L);
        final Order order = orderWithItem(10L, 7L, OrderStatus.PENDING, product, 2, BigDecimal.TEN);
        final OrderResponse response = new OrderResponse();
        when(orderRepository.findByIdAndUser_Id(10L, 7L)).thenReturn(Optional.of(order));
        when(productRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(product));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(response);

        final OrderResponse result = orderService.cancelOrder(10L, 7L);

        assertSame(response, result);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(5L, product.getQuantity());
        verify(orderRepository).save(order);
    }

    @Test
    void shouldRejectCancellationWhenOrderIsNotPending() {
        final Order order = order(10L, 7L, OrderStatus.CONFIRMED);
        when(orderRepository.findByIdAndUser_Id(10L, 7L)).thenReturn(Optional.of(order));

        assertThrows(ConflictException.class, () -> orderService.cancelOrder(10L, 7L));

        verify(productRepository, never()).findByIdForUpdate(any(Long.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldRejectInvalidOrderStatusTransition() {
        final Order order = order(10L, 7L, OrderStatus.PENDING);
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThrows(
                ConflictException.class,
                () -> orderService.updateOrderStatus(
                        10L,
                        new UpdateOrderStatusRequest(OrderStatus.SHIPPING)
                )
        );

        verify(orderRepository, never()).save(any(Order.class));
    }

    private Order captureSavedOrder() {
        final ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        return captor.getValue();
    }

    private static User user(Long id) {
        final User user = new User();
        user.setId(id);
        user.setEmail("user@example.com");
        return user;
    }

    private static Product product(Long id, BigDecimal price, Long quantity) {
        final Product product = new Product();
        product.setId(id);
        product.setName("Keyboard");
        product.setPrice(price);
        product.setQuantity(quantity);
        return product;
    }

    private static Order order(Long id, Long userId, OrderStatus status) {
        final Order order = new Order();
        order.setId(id);
        order.setStatus(status);
        order.setUser(user(userId));
        order.setItems(new ArrayList<>());
        return order;
    }

    private static Order orderWithItem(
            Long id,
            Long userId,
            OrderStatus status,
            Product product,
            Integer quantity,
            BigDecimal unitPrice
    ) {
        final Order order = order(id, userId, status);
        final OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        order.getItems().add(item);
        return order;
    }
}
