package com.jeju_nongdi.jeju_nongdi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식을 입력해주세요")
        String email,
        
        @NotBlank(message = "비밀번호는 필수입니다")
        String password
) { }
