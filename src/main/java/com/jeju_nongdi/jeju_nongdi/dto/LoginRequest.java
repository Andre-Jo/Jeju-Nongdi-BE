package com.jeju_nongdi.jeju_nongdi.dto;

public record LoginRequest(
        String email,
        String password
) { }