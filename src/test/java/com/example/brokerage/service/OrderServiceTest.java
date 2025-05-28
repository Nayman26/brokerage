package com.example.brokerage.service;

import com.example.brokerage.dto.request.CreateOrderRequest;
import com.example.brokerage.entity.Customer;
import com.example.brokerage.entity.Order;
import com.example.brokerage.enums.OrderSide;
import com.example.brokerage.enums.OrderStatus;
import com.example.brokerage.enums.Role;
import com.example.brokerage.repository.CustomerRepository;
import com.example.brokerage.repository.OrderRepository;
import com.example.brokerage.service.Impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AssetService assetService;

    @Mock
    private Principal principal;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Customer regularCustomer;
    private Customer adminCustomer;
    private CreateOrderRequest buyOrderRequest;
    private CreateOrderRequest sellOrderRequest;

    @BeforeEach
    void setUp() {
        regularCustomer = new Customer();
        regularCustomer.setId(1L);
        regularCustomer.setUsername("regularUser");
        regularCustomer.setRoles(Collections.singleton(Role.USER));

        adminCustomer = new Customer();
        adminCustomer.setId(2L);
        adminCustomer.setUsername("adminUser");
        adminCustomer.setRoles(Collections.singleton(Role.ADMIN));

        buyOrderRequest = new CreateOrderRequest();
        buyOrderRequest.setAssetName("AAPL");
        buyOrderRequest.setOrderSide(OrderSide.BUY);
        buyOrderRequest.setSize(BigDecimal.TEN);
        buyOrderRequest.setPrice(BigDecimal.valueOf(150));

        sellOrderRequest = new CreateOrderRequest();
        sellOrderRequest.setAssetName("AAPL");
        sellOrderRequest.setOrderSide(OrderSide.SELL);
        sellOrderRequest.setSize(BigDecimal.TEN);
        sellOrderRequest.setPrice(BigDecimal.valueOf(150));
    }

    @Test
    void getOrdersForUser_ShouldReturnUserOrders() {
        // Arrange
        when(principal.getName()).thenReturn("regularUser");
        when(customerRepository.findByUsername("regularUser")).thenReturn(Optional.of(regularCustomer));
        when(orderRepository.findByCustomerId(1L)).thenReturn(Collections.emptyList());

        // Act
        List<Order> result = orderService.getOrdersForUser(principal);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository).findByCustomerId(1L);
    }

    @Test
    void getOrdersForAdmin_WithAllFilters_ShouldReturnFilteredOrders() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();
        Long customerId = 1L;
        when(orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate))
                .thenReturn(Collections.emptyList());

        // Act
        List<Order> result = orderService.getOrdersForAdmin(customerId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository).findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate);
    }

    @Test
    void getOrdersForAdmin_WithCustomerIdOnly_ShouldReturnCustomerOrders() {
        // Arrange
        Long customerId = 1L;
        when(orderRepository.findByCustomerId(customerId)).thenReturn(Collections.emptyList());

        // Act
        List<Order> result = orderService.getOrdersForAdmin(customerId, null, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository).findByCustomerId(customerId);
    }

    @Test
    void getOrdersForAdmin_WithDateRangeOnly_ShouldReturnDateFilteredOrders() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();
        when(orderRepository.findByCreateDateBetween(startDate, endDate)).thenReturn(Collections.emptyList());

        // Act
        List<Order> result = orderService.getOrdersForAdmin(null, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository).findByCreateDateBetween(startDate, endDate);
    }

    @Test
    void getOrdersForAdmin_NoFilters_ShouldReturnAllOrders() {
        // Arrange
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Order> result = orderService.getOrdersForAdmin(null, null, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(orderRepository).findAll();
    }

    @Test
    void createOrder_AsRegularUser_ShouldCreateOrder() {
        // Arrange
        when(principal.getName()).thenReturn("regularUser");
        when(customerRepository.findByUsername("regularUser")).thenReturn(Optional.of(regularCustomer));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Order result = orderService.createOrder(buyOrderRequest, principal);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(regularCustomer.getId(), result.getCustomerId());
        verify(assetService).reserveCash(regularCustomer.getId(), buyOrderRequest.getSize().multiply(buyOrderRequest.getPrice()));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_AsAdminForAnotherUser_ShouldCreateOrder() {
        // Arrange
        buyOrderRequest.setCustomerId(1L);
        when(principal.getName()).thenReturn("adminUser");
        when(customerRepository.findByUsername("adminUser")).thenReturn(Optional.of(adminCustomer));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Order result = orderService.createOrder(buyOrderRequest, principal);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(1L, result.getCustomerId());
        verify(assetService).reserveCash(1L, buyOrderRequest.getSize().multiply(buyOrderRequest.getPrice()));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_WithInvalidSize_ShouldThrowException() {
        // Arrange
        buyOrderRequest.setSize(BigDecimal.ZERO);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(buyOrderRequest, principal));
    }

    @Test
    void createOrder_WithInvalidPrice_ShouldThrowException() {
        // Arrange
        buyOrderRequest.setPrice(BigDecimal.ZERO);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(buyOrderRequest, principal));
    }

    @Test
    void createSellOrder_ShouldReserveStock() {
        // Arrange
        when(principal.getName()).thenReturn("regularUser");
        when(customerRepository.findByUsername("regularUser")).thenReturn(Optional.of(regularCustomer));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Order result = orderService.createOrder(sellOrderRequest, principal);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(assetService).reserveStock(regularCustomer.getId(), sellOrderRequest.getAssetName(), sellOrderRequest.getSize());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void cancelOrder_AsOrderOwner_ShouldCancelOrder() {
        // Arrange
        Order pendingOrder = new Order();
        pendingOrder.setId(1L);
        pendingOrder.setCustomerId(1L);
        pendingOrder.setOrderSide(OrderSide.BUY);
        pendingOrder.setSize(BigDecimal.TEN);
        pendingOrder.setPrice(BigDecimal.valueOf(150));
        pendingOrder.setStatus(OrderStatus.PENDING);

        when(principal.getName()).thenReturn("regularUser");
        when(customerRepository.findByUsername("regularUser")).thenReturn(Optional.of(regularCustomer));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

        // Act
        orderService.cancelOrder(1L, principal);

        // Assert
        assertEquals(OrderStatus.CANCELED, pendingOrder.getStatus());
        verify(assetService).releaseCash(1L, BigDecimal.TEN.multiply(BigDecimal.valueOf(150)));
        verify(orderRepository).save(pendingOrder);
    }

    @Test
    void cancelOrder_AsAdmin_ShouldCancelOrder() {
        // Arrange
        Order pendingOrder = new Order();
        pendingOrder.setId(1L);
        pendingOrder.setCustomerId(1L);
        pendingOrder.setOrderSide(OrderSide.BUY);
        pendingOrder.setSize(BigDecimal.TEN);
        pendingOrder.setPrice(BigDecimal.valueOf(150));
        pendingOrder.setStatus(OrderStatus.PENDING);

        when(principal.getName()).thenReturn("adminUser");
        when(customerRepository.findByUsername("adminUser")).thenReturn(Optional.of(adminCustomer));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

        // Act
        orderService.cancelOrder(1L, principal);

        // Assert
        assertEquals(OrderStatus.CANCELED, pendingOrder.getStatus());
        verify(assetService).releaseCash(1L, BigDecimal.TEN.multiply(BigDecimal.valueOf(150)));
        verify(orderRepository).save(pendingOrder);
    }

    @Test
    void cancelOrder_NotOwnerNotAdmin_ShouldThrowException() {
        // Arrange
        Order pendingOrder = new Order();
        pendingOrder.setId(1L);
        pendingOrder.setCustomerId(1L);
        pendingOrder.setStatus(OrderStatus.PENDING);

        Customer anotherCustomer = new Customer();
        anotherCustomer.setId(3L);
        anotherCustomer.setUsername("anotherUser");
        anotherCustomer.setRoles(Collections.singleton(Role.USER));

        when(principal.getName()).thenReturn("anotherUser");
        when(customerRepository.findByUsername("anotherUser")).thenReturn(Optional.of(anotherCustomer));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> orderService.cancelOrder(1L, principal));
    }

    @Test
    void cancelOrder_NonPendingOrder_ShouldThrowException() {
        // Arrange
        Order completedOrder = new Order();
        completedOrder.setId(1L);
        completedOrder.setCustomerId(1L);
        completedOrder.setStatus(OrderStatus.CANCELED);

        when(principal.getName()).thenReturn("regularUser");
        when(customerRepository.findByUsername("regularUser")).thenReturn(Optional.of(regularCustomer));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(completedOrder));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> orderService.cancelOrder(1L, principal));
    }

    @Test
    void cancelOrder_SellOrder_ShouldReleaseStock() {
        // Arrange
        Order sellOrder = new Order();
        sellOrder.setId(1L);
        sellOrder.setCustomerId(1L);
        sellOrder.setOrderSide(OrderSide.SELL);
        sellOrder.setAssetName("AAPL");
        sellOrder.setSize(BigDecimal.TEN);
        sellOrder.setStatus(OrderStatus.PENDING);

        when(principal.getName()).thenReturn("regularUser");
        when(customerRepository.findByUsername("regularUser")).thenReturn(Optional.of(regularCustomer));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sellOrder));

        // Act
        orderService.cancelOrder(1L, principal);

        // Assert
        assertEquals(OrderStatus.CANCELED, sellOrder.getStatus());
        verify(assetService).releaseStock(1L, "AAPL", BigDecimal.TEN);
        verify(orderRepository).save(sellOrder);
    }
}