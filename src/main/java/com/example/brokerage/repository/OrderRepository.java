package com.example.brokerage.repository;

import com.example.brokerage.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);

    List<Order> findByCustomerIdAndCreateDateBetween(Long customerId, LocalDateTime start, LocalDateTime end);

    List<Order> findByCreateDateBetween(LocalDateTime start, LocalDateTime end);
}
