package com.example.brokerage.dto.request;

import com.example.brokerage.enums.OrderSide;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateOrderRequest {
    private Long customerId; // sadece admin için zorunlu
    private String assetName;
    private OrderSide orderSide;
    private BigDecimal size;
    private BigDecimal price;
}
