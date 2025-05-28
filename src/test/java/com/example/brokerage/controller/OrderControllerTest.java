package com.example.brokerage.controller;

import com.example.brokerage.dto.request.CreateOrderRequest;
import com.example.brokerage.dto.response.BaseApiResponse;
import com.example.brokerage.entity.Order;
import com.example.brokerage.enums.OrderSide;
import com.example.brokerage.enums.OrderStatus;
import com.example.brokerage.service.Impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderServiceImpl orderService;

    @InjectMocks
    private OrderController orderController;

    private Principal userPrincipal;
    private Principal adminPrincipal;
    private Order testOrder;
    private CreateOrderRequest testRequest;

    @BeforeEach
    void setUp() {
        // Test verilerini hazırla
        userPrincipal = () -> "testUser";
        adminPrincipal = () -> "adminUser";

        // SecurityContext'i ayarla
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        // Test order
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setAssetName("AAPL");
        testOrder.setOrderSide(OrderSide.BUY);
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setSize(BigDecimal.TEN);
        testOrder.setPrice(BigDecimal.valueOf(150));

        // Test request
        testRequest = new CreateOrderRequest();
        testRequest.setAssetName("AAPL");
        testRequest.setOrderSide(OrderSide.BUY);
        testRequest.setSize(BigDecimal.TEN);
        testRequest.setPrice(BigDecimal.valueOf(150));
    }

    @Test
    void getMyOrders_ShouldReturnUserOrders() {
        // Arrange
        when(orderService.getOrdersForUser(userPrincipal)).thenReturn(Collections.singletonList(testOrder));

        // Act
        List<Order> result = orderController.getMyOrders(userPrincipal);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder, result.get(0));
        verify(orderService).getOrdersForUser(userPrincipal);
    }

    @Test
    void getOrdersForAdmin_WithAllParameters_ShouldReturnFilteredOrders() {
        // Arrange
        Long customerId = 1L;
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();

        when(orderService.getOrdersForAdmin(customerId, startDate, endDate))
                .thenReturn(Collections.singletonList(testOrder));

        // Act
        List<Order> result = orderController.getOrdersForAdmin(customerId, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testOrder, result.get(0));
        verify(orderService).getOrdersForAdmin(customerId, startDate, endDate);
    }

    @Test
    void getOrdersForAdmin_WithCustomerIdOnly_ShouldReturnCustomerOrders() {
        // Arrange
        Long customerId = 1L;
        when(orderService.getOrdersForAdmin(customerId, null, null))
                .thenReturn(Collections.singletonList(testOrder));

        // Act
        List<Order> result = orderController.getOrdersForAdmin(customerId, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderService).getOrdersForAdmin(customerId, null, null);
    }

    @Test
    void getOrdersForAdmin_WithDateRangeOnly_ShouldReturnDateFilteredOrders() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now();
        when(orderService.getOrdersForAdmin(null, startDate, endDate))
                .thenReturn(Collections.singletonList(testOrder));

        // Act
        List<Order> result = orderController.getOrdersForAdmin(null, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderService).getOrdersForAdmin(null, startDate, endDate);
    }

    @Test
    void getOrdersForAdmin_NoParameters_ShouldReturnAllOrders() {
        // Arrange
        when(orderService.getOrdersForAdmin(null, null, null))
                .thenReturn(Collections.singletonList(testOrder));

        // Act
        List<Order> result = orderController.getOrdersForAdmin(null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderService).getOrdersForAdmin(null, null, null);
    }

    @Test
    void createOrder_AsUser_ShouldReturnCreatedOrder() {
        // Arrange
        when(orderService.createOrder(any(CreateOrderRequest.class), any(Principal.class)))
                .thenReturn(testOrder);

        // Act
        Order result = orderController.createOrder(testRequest, userPrincipal);

        // Assert
        assertNotNull(result);
        assertEquals(testOrder, result);
        verify(orderService).createOrder(testRequest, userPrincipal);
    }

    @Test
    void createOrder_AsAdmin_ShouldReturnCreatedOrder() {
        // Arrange
        when(orderService.createOrder(any(CreateOrderRequest.class), any(Principal.class)))
                .thenReturn(testOrder);

        // Act
        Order result = orderController.createOrder(testRequest, adminPrincipal);

        // Assert
        assertNotNull(result);
        assertEquals(testOrder, result);
        verify(orderService).createOrder(testRequest, adminPrincipal);
    }

    @Test
    void cancelOrder_AsOrderOwner_ShouldReturnSuccessResponse() {
        // Arrange
        Long orderId = 1L;
        doNothing().when(orderService).cancelOrder(orderId, userPrincipal);

        // Act
        ResponseEntity<BaseApiResponse> response = orderController.cancelOrder(orderId, userPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        assertEquals(orderId + " numaralı emir iptal edildi.", response.getBody().getMessage());
        verify(orderService).cancelOrder(orderId, userPrincipal);
    }

    @Test
    void cancelOrder_AsAdmin_ShouldReturnSuccessResponse() {
        // Arrange
        Long orderId = 1L;
        doNothing().when(orderService).cancelOrder(orderId, adminPrincipal);

        // Act
        ResponseEntity<BaseApiResponse> response = orderController.cancelOrder(orderId, adminPrincipal);

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isSuccess());
        verify(orderService).cancelOrder(orderId, adminPrincipal);
    }

    @Test
    void testSecurityContextSetup() {
        // Arrange
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(
                new UsernamePasswordAuthenticationToken(
                        "testUser",
                        "password",
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
        );
        // Act
        Principal principal = SecurityContextHolder.getContext().getAuthentication();

        // Assert
        assertNotNull(principal);
        assertEquals("testUser", principal.getName());
    }
}