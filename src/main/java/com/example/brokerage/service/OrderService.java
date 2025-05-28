package com.example.brokerage.service;

import com.example.brokerage.dto.request.CreateOrderRequest;
import com.example.brokerage.entity.Order;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {

    List<Order> getOrdersForUser(Principal principal);

    List<Order> getOrdersForAdmin(Long customerId, LocalDateTime startDate, LocalDateTime endDate);

    Order createOrder(CreateOrderRequest request, Principal principal);

    void cancelOrder(Long orderId, Principal principal);
}
