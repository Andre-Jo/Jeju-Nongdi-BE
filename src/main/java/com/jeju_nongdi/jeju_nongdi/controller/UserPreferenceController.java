package com.jeju_nongdi.jeju_nongdi.controller;

import com.jeju_nongdi.jeju_nongdi.dto.ai.UserPreferenceDto;
import com.jeju_nongdi.jeju_nongdi.entity.UserPreference;
import com.jeju_nongdi.jeju_nongdi.service.UserPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/preferences")
@RequiredArgsConstructor
@Tag(name = "사용자 설정", description = "사용자 농업 개인화 설정 관련 API")
public class UserPreferenceController {
    
    private final UserPreferenceService userPreferenceService;
    
    @GetMapping("/{userId}")
    @Operation(summary = "사용자 설정 조회", description = "특정 사용자의 농업 개인화 설정을 조회합니다.")
    public ResponseEntity<UserPreferenceDto> getUserPreference(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        
        UserPreferenceDto preference = userPreferenceService.getUserPreferenceDto(userId);
        if (preference == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(preference);
    }
    
    @GetMapping("/my")
    @Operation(summary = "내 설정 조회", description = "현재 로그인한 사용자의 설정을 조회합니다.")
    public ResponseEntity<UserPreferenceDto> getMyPreference() {
        Long userId = getCurrentUserId();
        UserPreferenceDto preference = userPreferenceService.getUserPreferenceDto(userId);
        
        if (preference == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(preference);
    }
    
    @PostMapping("/{userId}")
    @Operation(summary = "사용자 설정 생성/수정", description = "사용자의 농업 개인화 설정을 생성하거나 수정합니다.")
    public ResponseEntity<UserPreferenceDto> createOrUpdatePreference(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @RequestBody UserPreferenceDto preferenceDto) {
        
        if (!userPreferenceService.isValidPreference(preferenceDto)) {
            return ResponseEntity.badRequest().build();
        }
        
        UserPreferenceDto savedPreference = userPreferenceService.createOrUpdateUserPreference(userId, preferenceDto);
        return ResponseEntity.ok(savedPreference);
    }
    
    @PutMapping("/my")
    @Operation(summary = "내 설정 수정", description = "현재 로그인한 사용자의 설정을 수정합니다.")
    public ResponseEntity<UserPreferenceDto> updateMyPreference(
            @RequestBody UserPreferenceDto preferenceDto) {
        
        Long userId = getCurrentUserId();
        
        if (!userPreferenceService.isValidPreference(preferenceDto)) {
            return ResponseEntity.badRequest().build();
        }
        
        UserPreferenceDto savedPreference = userPreferenceService.createOrUpdateUserPreference(userId, preferenceDto);
        return ResponseEntity.ok(savedPreference);
    }
    
    @PostMapping("/{userId}/default")
    @Operation(summary = "기본 설정 생성", description = "사용자에게 기본 농업 설정을 생성해줍니다.")
    public ResponseEntity<UserPreferenceDto> createDefaultPreference(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        
        UserPreferenceDto defaultPreference = userPreferenceService.createDefaultPreference(userId);
        return ResponseEntity.ok(defaultPreference);
    }
    
    @DeleteMapping("/{userId}")
    @Operation(summary = "사용자 설정 삭제", description = "특정 사용자의 설정을 삭제합니다.")
    public ResponseEntity<String> deletePreference(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        
        userPreferenceService.deleteUserPreference(userId);
        return ResponseEntity.ok("설정이 성공적으로 삭제되었습니다.");
    }
    
    @GetMapping("/crop/{cropName}")
    @Operation(summary = "작물별 사용자 조회", description = "특정 작물을 기르는 사용자들을 조회합니다.")
    public ResponseEntity<List<UserPreferenceDto>> getUsersByCrop(
            @Parameter(description = "작물명") @PathVariable String cropName) {
        
        List<UserPreference> preferences = userPreferenceService.getUsersByPrimaryCrop(cropName);
        List<UserPreferenceDto> dtos = preferences.stream()
                .map(UserPreferenceDto::from)
                .toList();
        
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/location/{location}")
    @Operation(summary = "지역별 사용자 조회", description = "특정 지역의 사용자들을 조회합니다.")
    public ResponseEntity<List<UserPreferenceDto>> getUsersByLocation(
            @Parameter(description = "지역명") @PathVariable String location) {
        
        List<UserPreference> preferences = userPreferenceService.getUsersByLocation(location);
        List<UserPreferenceDto> dtos = preferences.stream()
                .map(UserPreferenceDto::from)
                .toList();
        
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/notification/{type}")
    @Operation(summary = "알림 유형별 사용자 조회", description = "특정 알림을 활성화한 사용자들을 조회합니다.")
    public ResponseEntity<List<UserPreferenceDto>> getUsersByNotificationType(
            @Parameter(description = "알림 유형 (WEATHER, PEST, MARKET, LABOR)") @PathVariable String type) {
        
        List<UserPreference> preferences = userPreferenceService.getUsersByNotificationType(type);
        List<UserPreferenceDto> dtos = preferences.stream()
                .map(UserPreferenceDto::from)
                .toList();
        
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping("/farming-types")
    @Operation(summary = "농업 유형 목록", description = "사용 가능한 농업 유형 목록을 조회합니다.")
    public ResponseEntity<List<FarmingTypeInfo>> getFarmingTypes() {
        List<FarmingTypeInfo> farmingTypes = List.of(
                new FarmingTypeInfo("TRADITIONAL", "전통농업", "일반적인 전통 방식의 농업"),
                new FarmingTypeInfo("ORGANIC", "유기농업", "화학 비료나 농약을 사용하지 않는 농업"),
                new FarmingTypeInfo("SMART_FARM", "스마트팜", "ICT 기술을 활용한 첨단 농업"),
                new FarmingTypeInfo("GREENHOUSE", "시설농업", "온실 등 시설을 이용한 농업")
        );
        
        return ResponseEntity.ok(farmingTypes);
    }
    
    @GetMapping("/validation")
    @Operation(summary = "설정 유효성 검사", description = "사용자 설정의 유효성을 검사합니다.")
    public ResponseEntity<Boolean> validatePreference(@RequestBody UserPreferenceDto preferenceDto) {
        boolean isValid = userPreferenceService.isValidPreference(preferenceDto);
        return ResponseEntity.ok(isValid);
    }
    
    // 현재 로그인한 사용자 ID 가져오기 (임시 구현)
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // 실제로는 JWT에서 사용자 정보를 추출해야 함
            // 현재는 테스트용으로 1L 반환
            return 1L;
        }
        return 1L; // 기본값
    }
    
    // 농업 유형 정보 클래스
    public record FarmingTypeInfo(String code, String name, String description) {}
}
