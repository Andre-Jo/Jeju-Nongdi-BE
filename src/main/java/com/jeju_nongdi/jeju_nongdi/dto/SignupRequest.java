package com.jeju_nongdi.jeju_nongdi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest (
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식을 입력해주세요")
        String email,
        
        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        String password,
        
        @NotBlank(message = "이름은 필수입니다")
        String name,
        
        @NotBlank(message = "닉네임은 필수입니다")
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다")
        String nickname,
        
        @NotBlank(message = "전화번호는 필수입니다")
        @Pattern(regexp = "^010\\d{8}$", message = "올바른 전화번호 형식을 입력해주세요 (010xxxxxxxx)")
        String phone
) {}
