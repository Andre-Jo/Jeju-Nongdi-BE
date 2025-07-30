package com.jeju_nongdi.jeju_nongdi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "비밀번호 변경 요청 정보")
public record UpdatePasswordRequest(
        @Schema(description = "현재 비밀번호", example = "oldpassword123")
        @NotBlank(message = "현재 비밀번호는 필수입니다")
        String currentPassword,
        
        @Schema(description = "새로운 비밀번호 (최소 8자)", example = "newpassword123")
        @NotBlank(message = "새 비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        String newPassword
) {}
