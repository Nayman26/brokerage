package com.example.brokerage.service;

import com.example.brokerage.enums.OrderSide;
import com.example.brokerage.enums.OrderStatus;
import com.example.brokerage.entity.Order;
import com.example.brokerage.repository.OrderRepository;
import com.example.brokerage.service.Impl.MatchServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AssetService assetService;

    @InjectMocks
    private MatchServiceImpl matchService;

    // Test Data
    private final Long customerId = 1L;
    private final String assetName = "AAPL";
    private final BigDecimal price = BigDecimal.valueOf(150);
    private final int size = 10;
    private final BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(size));

    @Test
    void matchOrders_shouldSuccessfullyMatchBuyOrder() {
        // Given
        Order buyOrder = createOrder(OrderSide.BUY, OrderStatus.PENDING);
        when(orderRepository.findAllById(any())).thenReturn(List.of(buyOrder));
        when(assetService.hasEnoughCash(anyLong(), any())).thenReturn(true);

        // When
        List<Long> result = matchService.matchOrders(List.of(1L));

        // Then
        assertEquals(1, result.size());
        assertEquals(OrderStatus.MATCHED, buyOrder.getStatus());

        verify(assetService).reserveCash(customerId, totalAmount);
        verify(assetService).releaseStock(customerId, assetName, BigDecimal.valueOf(size));
    }

    @Test
    void matchOrders_shouldSuccessfullyMatchSellOrder() {
        // Given
        Order sellOrder = createOrder(OrderSide.SELL, OrderStatus.PENDING);
        when(orderRepository.findAllById(any())).thenReturn(List.of(sellOrder));
        when(assetService.hasEnoughStock(anyLong(), anyString(), any())).thenReturn(true);

        // When
        List<Long> result = matchService.matchOrders(List.of(1L));

        // Then
        assertEquals(1, result.size());
        assertEquals(OrderStatus.MATCHED, sellOrder.getStatus());

        verify(assetService).reserveStock(customerId, assetName, BigDecimal.valueOf(size));
        verify(assetService).releaseCash(customerId, totalAmount);
    }

    @Test
    void matchOrders_shouldSkipNonPendingOrders() {
        // Given
        Order matchedOrder = createOrder(OrderSide.BUY, OrderStatus.MATCHED);
        when(orderRepository.findAllById(any())).thenReturn(List.of(matchedOrder));

        // When
        List<Long> result = matchService.matchOrders(List.of(1L));

        // Then
        assertTrue(result.isEmpty());
        verify(assetService, never()).reserveCash(anyLong(), any());
    }

    @Test
    void matchOrders_shouldThrowInsufficientFundsForBuyOrder() {
        // Given
        Order buyOrder = createOrder(OrderSide.BUY, OrderStatus.PENDING);
        when(orderRepository.findAllById(any())).thenReturn(List.of(buyOrder));
        when(assetService.hasEnoughCash(anyLong(), any())).thenReturn(false);

        // When & Then
        assertDoesNotThrow(() -> {
            List<Long> result = matchService.matchOrders(List.of(1L));
            assertTrue(result.isEmpty());
        });

        verify(assetService, never()).reserveCash(anyLong(), any());
    }

    @Test
    void matchOrders_shouldThrowInsufficientAssetsForSellOrder() {
        // Given
        Order sellOrder = createOrder(OrderSide.SELL, OrderStatus.PENDING);
        when(orderRepository.findAllById(any())).thenReturn(List.of(sellOrder));
        when(assetService.hasEnoughStock(anyLong(), anyString(), any())).thenReturn(false);

        // When & Then
        assertDoesNotThrow(() -> {
            List<Long> result = matchService.matchOrders(List.of(1L));
            assertTrue(result.isEmpty());
        });

        verify(assetService, never()).reserveStock(anyLong(), anyString(), any());
    }

    @Test
    void matchOrders_shouldProcessMultipleOrders() {
        // Given
        Order buyOrder = createOrder(OrderSide.BUY, OrderStatus.PENDING);
        Order sellOrder = createOrder(OrderSide.SELL, OrderStatus.PENDING);
        sellOrder.setId(2L);

        when(orderRepository.findAllById(any())).thenReturn(Arrays.asList(buyOrder, sellOrder));
        when(assetService.hasEnoughCash(anyLong(), any())).thenReturn(true);
        when(assetService.hasEnoughStock(anyLong(), anyString(), any())).thenReturn(true);

        // When
        List<Long> result = matchService.matchOrders(List.of(1L, 2L));

        // Then
        assertEquals(2, result.size());
        verify(assetService).reserveCash(customerId, totalAmount);
        verify(assetService).reserveStock(customerId, assetName, BigDecimal.valueOf(size));
    }

    @Test
    void matchOrders_shouldContinueAfterException() {
        // Given
        Order validOrder = createOrder(OrderSide.BUY, OrderStatus.PENDING);
        Order invalidOrder = createOrder(OrderSide.BUY, OrderStatus.PENDING);
        invalidOrder.setId(2L);

        when(orderRepository.findAllById(any())).thenReturn(Arrays.asList(validOrder, invalidOrder));
        when(assetService.hasEnoughCash(anyLong(), any()))
                .thenReturn(true)  // First call for validOrder
                .thenReturn(false); // Second call for invalidOrder

        // When
        List<Long> result = matchService.matchOrders(List.of(1L, 2L));

        // Then
        assertEquals(1, result.size());
        assertEquals(validOrder.getId(), result.get(0));
    }

    private Order createOrder(OrderSide side, OrderStatus status) {
        Order order = new Order();
        order.setId(1L);
        order.setCustomerId(customerId);
        order.setAssetName(assetName);
        order.setOrderSide(side);
        order.setSize(BigDecimal.valueOf(size));
        order.setPrice(price);
        order.setStatus(status);
        return order;
    }
}