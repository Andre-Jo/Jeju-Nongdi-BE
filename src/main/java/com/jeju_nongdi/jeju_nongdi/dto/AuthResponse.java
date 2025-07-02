package com.jeju_nongdi.jeju_nongdi.dto;

public record AuthResponse (
        String token,
        String email,
        String name,
        String nickname,
        String role
) { }