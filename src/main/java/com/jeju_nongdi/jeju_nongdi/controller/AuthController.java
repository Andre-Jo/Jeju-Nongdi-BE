package com.jeju_nongdi.jeju_nongdi.controller;

import com.jeju_nongdi.jeju_nongdi.dto.*;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "인증", description = "회원 인증 관련 API")
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "이메일 또는 닉네임 중복",
                    content = @Content)
    })
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(userService.signup(request));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 인증을 통해 JWT 토큰을 발급받습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content)
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/profile")
    @Operation(summary = "현재 사용자 정보 조회", description = "JWT 토큰을 통해 현재 로그인한 사용자의 정보를 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content)
    })
    public ResponseEntity<User> getCurrentUser(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(userService.getCurrentUser(user.getEmail()));
    }

    @PostMapping("/check-nickname")
    @Operation(summary = "닉네임 중복 확인", description = "닉네임 사용 가능 여부를 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "중복 확인 완료",
                    content = @Content(schema = @Schema(implementation = CheckNicknameResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content)
    })
    public ResponseEntity<CheckNicknameResponse> checkNickname(@Valid @RequestBody CheckNicknameRequest request) {
        return ResponseEntity.ok(userService.checkNicknameAvailability(request));
    }

    @PutMapping("/update-nickname")
    @Operation(summary = "닉네임 변경", description = "사용자의 닉네임을 변경합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "닉네임 변경 성공",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패 또는 중복된 닉네임",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content)
    })
    public ResponseEntity<User> updateNickname(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateNicknameRequest request) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User updatedUser = userService.updateNickname(user.getEmail(), request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/update-password")
    @Operation(summary = "비밀번호 변경", description = "사용자의 비밀번호를 변경합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패 또는 현재 비밀번호 불일치",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content)
    })
    public ResponseEntity<com.jeju_nongdi.jeju_nongdi.dto.ApiResponse<String>> updatePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdatePasswordRequest request) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        userService.updatePassword(user.getEmail(), request);
        return ResponseEntity.ok(com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.success("비밀번호가 성공적으로 변경되었습니다.", null));
    }

    @PutMapping("/update-profile-image")
    @Operation(summary = "프로필 이미지 변경", description = "사용자의 프로필 이미지를 변경합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 이미지 변경 성공",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content)
    })
    public ResponseEntity<User> updateProfileImage(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileImageRequest request) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User updatedUser = userService.updateProfileImage(user.getEmail(), request);
        return ResponseEntity.ok(updatedUser);
    }
}
