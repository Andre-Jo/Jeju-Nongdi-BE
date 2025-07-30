package com.jeju_nongdi.jeju_nongdi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "닉네임 변경 요청 정보")
public record UpdateNicknameRequest(
        @Schema(description = "새로운 닉네임 (2-20자)", example = "새제주농부")
        @NotBlank(message = "닉네임은 필수입니다")
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다")
        String nickname
) {}
