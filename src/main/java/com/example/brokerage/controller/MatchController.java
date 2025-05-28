package com.example.brokerage.controller;

import com.example.brokerage.dto.response.BaseApiResponse;
import com.example.brokerage.service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/match")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseApiResponse> matchOrders(@RequestBody List<Long> orderIds) {
        List<Long> matchedIds = matchService.matchOrders(orderIds);
        orderIds.removeAll(matchedIds);
        String message = orderIds.isEmpty()
                ? "All Orders Matched"
                : "Matched Orders: " + matchedIds + " UnMatched Orders: " + orderIds;
        return ResponseEntity.ok(new BaseApiResponse(true, message));
    }
}

