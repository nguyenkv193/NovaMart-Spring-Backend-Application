package com.novamart.modules.orders.repository;

import com.novamart.modules.orders.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByUser_IdOrderByOrderAtDesc(Long userId);

    Optional<Order> findByIdAndUser_Id(Long orderId, Long userId);
}
