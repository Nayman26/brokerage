package com.example.brokerage.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BaseApiResponse {
    private boolean success;
    private String message;
    private LocalDateTime timestamp;

    public BaseApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public BaseApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}