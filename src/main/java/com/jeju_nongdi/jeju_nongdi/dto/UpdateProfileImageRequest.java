package com.jeju_nongdi.jeju_nongdi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "프로필 이미지 변경 요청 정보")
public record UpdateProfileImageRequest(
        @Schema(description = "새로운 프로필 이미지 URL", example = "https://example.com/profile.jpg")
        @NotBlank(message = "프로필 이미지 URL은 필수입니다")
        String profileImage
) {}
