package com.example.brokerage.controller;

import com.example.brokerage.dto.request.CreateOrderRequest;
import com.example.brokerage.dto.response.BaseApiResponse;
import com.example.brokerage.entity.Order;
import com.example.brokerage.service.Impl.OrderServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Order APIs", description = "Apis for managing orders")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderServiceImpl orderService;

    public OrderController(OrderServiceImpl orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Get my orders", description = "List all orders that I have")
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public List<Order> getMyOrders(Principal principal) {
        return orderService.getOrdersForUser(principal);
    }

    @Operation(summary = "Get all orders", description = "List and filter all orders for admin")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Order> getOrdersForAdmin(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate)
    {
        return orderService.getOrdersForAdmin(customerId, startDate, endDate);
    }

    @Operation(summary = "Create order",description = "create order of customer")
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Order createOrder(@RequestBody CreateOrderRequest request, Principal principal) {
        return orderService.createOrder(request, principal);
    }

    @Operation(summary = "Cancel Order",description = "cancel order of customer")
    @DeleteMapping("cancelOrder/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<BaseApiResponse> cancelOrder(@PathVariable Long orderId, Principal principal) {
        orderService.cancelOrder(orderId, principal);
        return ResponseEntity.ok(new BaseApiResponse(true, orderId + " numaralı emir iptal edildi."));
    }
}
