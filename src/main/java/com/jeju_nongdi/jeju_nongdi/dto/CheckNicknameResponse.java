package com.jeju_nongdi.jeju_nongdi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "닉네임 중복 확인 응답 정보")
public record CheckNicknameResponse(
        @Schema(description = "사용 가능 여부", example = "true")
        boolean available,
        
        @Schema(description = "메시지", example = "사용 가능한 닉네임입니다.")
        String message
) {}
