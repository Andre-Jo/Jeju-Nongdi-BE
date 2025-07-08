package com.jeju_nongdi.jeju_nongdi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인증 응답 정보")
public record AuthResponse (
        @Schema(description = "JWT 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,
        
        @Schema(description = "사용자 이메일", example = "user@example.com")
        String email,
        
        @Schema(description = "사용자 실명", example = "홍길동")
        String name,
        
        @Schema(description = "사용자 닉네임", example = "제주농부")
        String nickname,
        
        @Schema(description = "사용자 역할", example = "USER")
        String role
) { }