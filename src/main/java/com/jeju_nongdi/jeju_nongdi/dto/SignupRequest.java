package com.jeju_nongdi.jeju_nongdi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청 정보")
public record SignupRequest (
        @Schema(description = "사용자 이메일", example = "user@example.com")
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식을 입력해주세요")
        String email,
        
        @Schema(description = "비밀번호 (최소 8자)", example = "password123")
        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        String password,
        
        @Schema(description = "사용자 실명", example = "홍길동")
        @NotBlank(message = "이름은 필수입니다")
        String name,
        
        @Schema(description = "사용자 닉네임 (2-20자)", example = "제주농부")
        @NotBlank(message = "닉네임은 필수입니다")
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다")
        String nickname,
        
        @Schema(description = "전화번호 (010으로 시작하는 11자리)", example = "01012345678")
        @NotBlank(message = "전화번호는 필수입니다")
        @Pattern(regexp = "^010\\d{8}$", message = "올바른 전화번호 형식을 입력해주세요 (010xxxxxxxx)")
        String phone
) {}
