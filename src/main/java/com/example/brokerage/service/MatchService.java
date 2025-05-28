package com.example.brokerage.service;

import java.util.List;

public interface MatchService {
    List<Long> matchOrders(List<Long> orderIds);
}
