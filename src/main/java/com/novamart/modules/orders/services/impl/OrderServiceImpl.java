package com.novamart.modules.orders.services.impl;

import com.novamart.common.constants.ValidationMessageConstants;
import com.novamart.common.exception.BadRequestException;
import com.novamart.common.exception.ConflictException;
import com.novamart.common.exception.NotFoundException;
import com.novamart.modules.orders.constants.OrderMessageConstants;
import com.novamart.modules.orders.dto.OrderItemRequest;
import com.novamart.modules.orders.dto.OrderItemResponse;
import com.novamart.modules.orders.dto.OrderRequest;
import com.novamart.modules.orders.dto.OrderResponse;
import com.novamart.modules.orders.dto.UpdateOrderStatusRequest;
import com.novamart.modules.orders.entity.Order;
import com.novamart.modules.orders.entity.OrderItem;
import com.novamart.modules.orders.enums.OrderStatus;
import com.novamart.modules.orders.mapper.OrderMapper;
import com.novamart.modules.orders.policy.OrderAccessPolicy;
import com.novamart.modules.orders.repository.OrderRepository;
import com.novamart.modules.orders.services.OrderService;
import com.novamart.modules.products.constants.ProductMessageConstants;
import com.novamart.modules.products.entity.Product;
import com.novamart.modules.products.repository.ProductRepository;
import com.novamart.modules.users.constants.UserMessageConstants;
import com.novamart.modules.users.entity.User;
import com.novamart.modules.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_STATUS_TRANSITIONS = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.SHIPPING, OrderStatus.CANCELLED),
            OrderStatus.SHIPPING, Set.of(OrderStatus.COMPLETED),
            OrderStatus.COMPLETED, Set.of(),
            OrderStatus.CANCELLED, Set.of()
    );

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final OrderAccessPolicy orderAccessPolicy;
    private final Clock applicationClock;

    @Override
    @PreAuthorize("isAuthenticated() and #a1 == authentication.principal.userId")
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest, Long userId) {
        validateOrderRequest(orderRequest);

        final User user = findUserById(userId);
        final Map<Long, Product> productsById = loadProductsForOrder(orderRequest);
        final Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setOrderAt(LocalDateTime.now(applicationClock));
        order.setUser(user);

        final BigDecimal totalAmount = addOrderItems(order, orderRequest, productsById);
        order.setTotalAmount(totalAmount);

        final Order savedOrder = orderRepository.save(order);
        log.info(
                "Order created with id={} userId={} itemCount={}",
                savedOrder.getId(),
                userId,
                savedOrder.getItems().size()
        );

        return toResponse(savedOrder);
    }

    @Override
    @PreAuthorize("isAuthenticated() and #a0 == authentication.principal.userId")
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(Long userId) {
        return orderRepository.findAllByUser_IdOrderByOrderAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or #a1 == authentication.principal.userId")
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long requesterId) {
        final Order order = orderAccessPolicy.isAdmin()
                ? findOrderById(orderId)
                : orderRepository.findByIdAndUser_Id(orderId, requesterId)
                .orElseThrow(() -> orderNotFound(orderId));

        return toResponse(order);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public OrderResponse updateOrderStatus(
            Long orderId,
            UpdateOrderStatusRequest statusRequest
    ) {
        validateStatusRequest(statusRequest);

        final Order order = findOrderById(orderId);
        final OrderStatus targetStatus = statusRequest.getStatus();
        validateStatusTransition(order, targetStatus);

        if (targetStatus == OrderStatus.CANCELLED) {
            final Map<Long, Product> productsById = loadLockedProductsForOrder(order);
            restoreStock(order, productsById);
        }

        order.setStatus(targetStatus);
        final Order updatedOrder = orderRepository.save(order);
        log.info(
                "Order status updated with id={} status={}",
                orderId,
                targetStatus
        );

        return toResponse(updatedOrder);
    }

    @Override
    @PreAuthorize("isAuthenticated() and #a1 == authentication.principal.userId")
    @Transactional
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        final Order order = orderRepository.findByIdAndUser_Id(orderId, userId)
                .orElseThrow(() -> orderNotFound(orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ConflictException(OrderMessageConstants.CANCELLATION_NOT_ALLOWED);
        }

        final Map<Long, Product> productsById = loadLockedProductsForOrder(order);
        restoreStock(order, productsById);
        order.setStatus(OrderStatus.CANCELLED);

        final Order cancelledOrder = orderRepository.save(order);
        log.info("Order cancelled with id={} userId={}", orderId, userId);

        return toResponse(cancelledOrder);
    }

    private void validateOrderRequest(OrderRequest orderRequest) {
        if (orderRequest == null || orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
            throw new BadRequestException(ValidationMessageConstants.ORDER_ITEMS_REQUIRED);
        }
    }

    private void validateStatusRequest(UpdateOrderStatusRequest statusRequest) {
        if (statusRequest == null || statusRequest.getStatus() == null) {
            throw new BadRequestException(ValidationMessageConstants.ORDER_STATUS_REQUIRED);
        }
    }

    private Map<Long, Product> loadProductsForOrder(OrderRequest orderRequest) {
        final Map<Long, Product> productsById = new LinkedHashMap<>();

        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            validateOrderItemRequest(itemRequest);

            if (productsById.containsKey(itemRequest.getProductId())) {
                throw new BadRequestException(
                        String.format(
                                OrderMessageConstants.DUPLICATE_PRODUCT,
                                itemRequest.getProductId()
                        )
                );
            }

            final Product product = productRepository.findByIdForUpdate(itemRequest.getProductId())
                    .orElseThrow(() -> new NotFoundException(
                            String.format(
                                    ProductMessageConstants.PRODUCT_NOT_FOUND,
                                    itemRequest.getProductId()
                            )
                    ));

            validateStock(product, itemRequest.getQuantity());
            productsById.put(itemRequest.getProductId(), product);
        }

        return productsById;
    }

    private void validateOrderItemRequest(OrderItemRequest itemRequest) {
        if (itemRequest == null || itemRequest.getProductId() == null) {
            throw new BadRequestException(ValidationMessageConstants.PRODUCT_ID_REQUIRED);
        }

        if (itemRequest.getProductId() <= 0) {
            throw new BadRequestException(ValidationMessageConstants.FIELD_TOO_SHORT);
        }

        if (itemRequest.getQuantity() == null) {
            throw new BadRequestException(ValidationMessageConstants.QUANTITY_REQUIRED);
        }

        if (itemRequest.getQuantity() <= 0) {
            throw new BadRequestException(ValidationMessageConstants.QUANTITY_MUST_BE_POSITIVE);
        }
    }

    private void validateStock(Product product, Integer requestedQuantity) {
        if (product.getQuantity() < requestedQuantity) {
            throw new ConflictException(
                    String.format(
                            OrderMessageConstants.INSUFFICIENT_STOCK,
                            product.getId(),
                            product.getQuantity(),
                            requestedQuantity
                    )
            );
        }
    }

    private BigDecimal addOrderItems(
            Order order,
            OrderRequest orderRequest,
            Map<Long, Product> productsById
    ) {
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            final Product product = productsById.get(itemRequest.getProductId());
            final BigDecimal unitPrice = product.getPrice();
            final BigDecimal subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(itemRequest.getQuantity())
            );
            final OrderItem orderItem = new OrderItem();
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setOrder(order);
            orderItem.setProduct(product);

            product.setQuantity(product.getQuantity() - itemRequest.getQuantity());
            order.getItems().add(orderItem);
            totalAmount = totalAmount.add(subtotal);
        }

        return totalAmount;
    }

    private void validateStatusTransition(Order order, OrderStatus targetStatus) {
        final Set<OrderStatus> allowedStatuses = ALLOWED_STATUS_TRANSITIONS.get(order.getStatus());

        if (!allowedStatuses.contains(targetStatus)) {
            throw new ConflictException(
                    String.format(
                            OrderMessageConstants.INVALID_STATUS_TRANSITION,
                            order.getId(),
                            order.getStatus(),
                            targetStatus
                    )
            );
        }
    }

    private Map<Long, Product> loadLockedProductsForOrder(Order order) {
        final Map<Long, Product> productsById = new LinkedHashMap<>();

        for (OrderItem orderItem : order.getItems()) {
            final Long productId = orderItem.getProduct().getId();
            if (productsById.containsKey(productId)) {
                continue;
            }

            final Product product = productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> new NotFoundException(
                            String.format(ProductMessageConstants.PRODUCT_NOT_FOUND, productId)
                    ));
            productsById.put(productId, product);
        }

        return productsById;
    }

    private void restoreStock(Order order, Map<Long, Product> productsById) {
        for (OrderItem orderItem : order.getItems()) {
            final Long productId = orderItem.getProduct().getId();
            final Product product = productsById.get(productId);
            product.setQuantity(product.getQuantity() + orderItem.getQuantity());
        }
    }

    private OrderResponse toResponse(Order order) {
        final OrderResponse response = orderMapper.toResponse(order);

        if (response == null || response.getItems() == null) {
            return response;
        }

        final List<OrderItemResponse> itemResponses = response.getItems();
        for (int index = 0; index < itemResponses.size(); index++) {
            final OrderItem orderItem = order.getItems().get(index);
            itemResponses.get(index).setSubtotal(
                    orderItem.getUnitPrice().multiply(
                            BigDecimal.valueOf(orderItem.getQuantity())
                    )
            );
        }

        return response;
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format(UserMessageConstants.USER_NOT_FOUND, userId)
                ));
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> orderNotFound(orderId));
    }

    private NotFoundException orderNotFound(Long orderId) {
        return new NotFoundException(
                String.format(OrderMessageConstants.ORDER_NOT_FOUND, orderId)
        );
    }
}
