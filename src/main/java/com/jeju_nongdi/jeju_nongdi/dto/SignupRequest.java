package com.jeju_nongdi.jeju_nongdi.dto;

public record SignupRequest (
        String email,
        String password,
        String name,
        String nickname,
        String phone
) {}