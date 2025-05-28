package com.example.brokerage.service.Impl;

import com.example.brokerage.entity.Order;
import com.example.brokerage.enums.OrderSide;
import com.example.brokerage.enums.OrderStatus;
import com.example.brokerage.repository.OrderRepository;
import com.example.brokerage.service.AssetService;
import com.example.brokerage.service.MatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
public class MatchServiceImpl implements MatchService {

    private final OrderRepository orderRepository;
    private final AssetService assetService;

    public MatchServiceImpl(OrderRepository orderRepository, AssetService assetService) {
        this.orderRepository = orderRepository;
        this.assetService = assetService;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Long> matchOrders(List<Long> orderIds) {
        List<Order> orders = orderRepository.findAllById(orderIds);
        List<Long> matchedIds = new ArrayList<>();

        for (Order order : orders) {
            try {
                if (order.getStatus() != OrderStatus.PENDING) {
                    continue;
                }

                BigDecimal totalAmount = order.getPrice().multiply(order.getSize());

                if (order.getOrderSide() == OrderSide.BUY) {
                    processBuyOrder(order, totalAmount);
                } else {
                    processSellOrder(order, totalAmount);
                }

                order.setStatus(OrderStatus.MATCHED);
                orderRepository.save(order);
                matchedIds.add(order.getId());

            } catch (Exception e) {
                log.error("Order match failed. OrderID: {}", order.getId(), e);
            }
        }
        return matchedIds;
    }

    private void processBuyOrder(Order order, BigDecimal totalAmount) {
        if (!assetService.hasEnoughCash(order.getCustomerId(), totalAmount)) {
            throw new RuntimeException("Yetersiz TRY bakiyesi");
        }
        //TRY'yi rezerve et (usableSize'dan düş)
        assetService.reserveCash(order.getCustomerId(), totalAmount);

        //Hisseyi serbest bırak (size'a ekle)
        assetService.releaseStock(order.getCustomerId(), order.getAssetName(), order.getSize());
    }

    private void processSellOrder(Order order, BigDecimal totalAmount) {
        if (!assetService.hasEnoughStock(order.getCustomerId(), order.getAssetName(), order.getSize())) {
            throw new RuntimeException("Yetersiz hisse miktarı");
        }

        //Hisseyi rezerve et (usableSize'dan düş)
        assetService.reserveStock(order.getCustomerId(), order.getAssetName(), order.getSize());

        //TRY'yi serbest bırak (size'a ekle)
        assetService.releaseCash(order.getCustomerId(), totalAmount);
    }
}

