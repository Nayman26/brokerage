package com.example.brokerage.service.Impl;

import com.example.brokerage.dto.request.CreateOrderRequest;
import com.example.brokerage.entity.*;
import com.example.brokerage.enums.OrderSide;
import com.example.brokerage.enums.OrderStatus;
import com.example.brokerage.repository.CustomerRepository;
import com.example.brokerage.repository.OrderRepository;
import com.example.brokerage.service.AssetService;
import com.example.brokerage.service.OrderService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final AssetService assetService;

    public OrderServiceImpl(OrderRepository orderRepository, CustomerRepository customerRepository, AssetService assetService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.assetService = assetService;
    }

    @Override
    public List<Order> getOrdersForUser(Principal principal) {
        return orderRepository.findByCustomerId(getCustomerByUsername(principal.getName()).getId());
    }

    @Override
    public List<Order> getOrdersForAdmin(Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        if (customerId != null && startDate != null && endDate != null)
            return orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate);
        if (customerId != null)
            return orderRepository.findByCustomerId(customerId);
        if (startDate != null && endDate != null)
            return orderRepository.findByCreateDateBetween(startDate, endDate);
        return orderRepository.findAll();
    }

    @Override
    @Transactional
    public Order createOrder(CreateOrderRequest request, Principal principal) {
        Long customerId = getTargetCustomerId(request.getCustomerId(), principal);
        validateOrderRequest(request);

        if (request.getOrderSide() == OrderSide.BUY)
            assetService.reserveCash(customerId, request.getSize().multiply(request.getPrice()));
        else
            assetService.reserveStock(customerId, request.getAssetName(), request.getSize());

        Order order = new Order();
        order.setCustomerId(customerId);
        order.setAssetName(request.getAssetName());
        order.setOrderSide(request.getOrderSide());
        order.setSize(request.getSize());
        order.setPrice(request.getPrice());
        order.setStatus(OrderStatus.PENDING);
        order.setCreateDate(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Principal principal) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order bulunamadı"));
        Long callerId = getCustomerByUsername(principal.getName()).getId();

        if (!callerId.equals(order.getCustomerId()) && !isAdmin(principal))
            throw new RuntimeException("Bu order'ı silme yetkiniz yok");
        if (order.getStatus() != OrderStatus.PENDING)
            throw new RuntimeException("Sadece PENDING emirler iptal edilebilir");

        if (order.getOrderSide() == OrderSide.BUY)
            assetService.releaseCash(order.getCustomerId(), order.getSize().multiply(order.getPrice()));
        else
            assetService.releaseStock(order.getCustomerId(), order.getAssetName(), order.getSize());

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }

    private Customer getCustomerByUsername(String username) {
        return customerRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));
    }

    private Long getTargetCustomerId(Long requestId, Principal principal) {
        Customer caller = getCustomerByUsername(principal.getName());
        return isAdmin(principal) ? validateAdminCustomerId(requestId) : caller.getId();
    }

    private Long validateAdminCustomerId(Long customerId) {
        if (customerId == null)
            throw new IllegalArgumentException("Admin için customerId zorunludur");
        return customerId;
    }

    private boolean isAdmin(Principal principal) {
        Customer customer = getCustomerByUsername(principal.getName());
        return customer.getRoles().stream().anyMatch(r -> r.name().equals("ADMIN"));
    }

    private void validateOrderRequest(CreateOrderRequest req) {
        if (req.getSize().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Miktar sıfırdan büyük olmalı");
        if (req.getPrice().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Fiyat sıfırdan büyük olmalı");
    }
}
