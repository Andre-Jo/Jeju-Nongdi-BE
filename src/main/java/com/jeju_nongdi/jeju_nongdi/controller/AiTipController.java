package com.jeju_nongdi.jeju_nongdi.controller;

import com.jeju_nongdi.jeju_nongdi.dto.ai.AiTipResponseDto;
import com.jeju_nongdi.jeju_nongdi.dto.ai.DailyTipRequestDto;
import com.jeju_nongdi.jeju_nongdi.dto.ai.DailyTipSummaryDto;
import com.jeju_nongdi.jeju_nongdi.entity.AiTip;
import com.jeju_nongdi.jeju_nongdi.service.AiTipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ai-tips")
@RequiredArgsConstructor
@Tag(name = "AI 농업 도우미", description = "AI 스마트 농업 도우미 관련 API")
public class AiTipController {
    
    private final AiTipService aiTipService;
    
    @GetMapping("/daily/{userId}")
    @Operation(summary = "일일 맞춤 팁 조회", description = "특정 사용자의 일일 맞춤 농업 팁을 조회합니다.")
    public ResponseEntity<DailyTipSummaryDto> getDailyTips(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "조회할 날짜") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate,
            @Parameter(description = "팁 유형 필터") @RequestParam(required = false) List<String> tipTypes,
            @Parameter(description = "작물 유형 필터") @RequestParam(required = false) String cropType,
            @Parameter(description = "최소 우선순위") @RequestParam(required = false) Integer priorityLevel,
            @Parameter(description = "읽지 않은 팁만 조회") @RequestParam(required = false, defaultValue = "false") Boolean onlyUnread) {
        
        // 요청 DTO 구성
        DailyTipRequestDto requestDto = DailyTipRequestDto.builder()
                .targetDate(targetDate != null ? targetDate : LocalDate.now())
                .tipTypes(tipTypes)
                .cropType(cropType)
                .priorityLevel(priorityLevel)
                .onlyUnread(onlyUnread)
                .build();
        
        DailyTipSummaryDto summary = aiTipService.getDailyTips(userId, requestDto);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/weather/{farmId}")
    @Operation(summary = "날씨 기반 알림", description = "특정 농장의 날씨 기반 맞춤 알림을 조회합니다.")
    public ResponseEntity<DailyTipSummaryDto> getWeatherBasedTips(
            @Parameter(description = "농장 ID (현재는 사용자 ID와 동일)") @PathVariable Long farmId,
            @Parameter(description = "조회할 날짜") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {
        
        DailyTipRequestDto requestDto = DailyTipRequestDto.builder()
                .targetDate(targetDate != null ? targetDate : LocalDate.now())
                .tipTypes(List.of("WEATHER_ALERT"))
                .build();
        
        DailyTipSummaryDto summary = aiTipService.getDailyTips(farmId, requestDto);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/crop-guide/{cropType}")
    @Operation(summary = "작물별 가이드", description = "특정 작물의 생육 단계별 가이드를 조회합니다.")
    public ResponseEntity<DailyTipSummaryDto> getCropGuide(
            @Parameter(description = "작물 유형") @PathVariable String cropType,
            @Parameter(description = "조회할 날짜") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {
        
        // 현재 로그인한 사용자 ID 가져오기 (임시로 1L 사용)
        Long userId = getCurrentUserId();
        
        DailyTipRequestDto requestDto = DailyTipRequestDto.builder()
                .targetDate(targetDate != null ? targetDate : LocalDate.now())
                .tipTypes(List.of("CROP_GUIDE"))
                .cropType(cropType)
                .build();
        
        DailyTipSummaryDto summary = aiTipService.getDailyTips(userId, requestDto);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/pest-alert/{region}")
    @Operation(summary = "병해충 경보", description = "특정 지역의 병해충 경보를 조회합니다.")
    public ResponseEntity<DailyTipSummaryDto> getPestAlert(
            @Parameter(description = "지역") @PathVariable String region,
            @Parameter(description = "조회할 날짜") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {
        
        Long userId = getCurrentUserId();
        
        DailyTipRequestDto requestDto = DailyTipRequestDto.builder()
                .targetDate(targetDate != null ? targetDate : LocalDate.now())
                .tipTypes(List.of("PEST_ALERT"))
                .build();
        
        DailyTipSummaryDto summary = aiTipService.getDailyTips(userId, requestDto);
        return ResponseEntity.ok(summary);
    }
    
    @PostMapping("/generate/{userId}")
    @Operation(summary = "일일 팁 생성", description = "특정 사용자를 위한 일일 맞춤 팁을 생성합니다.")
    public ResponseEntity<String> generateDailyTips(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        
        aiTipService.generateDailyTipsForUser(userId);
        return ResponseEntity.ok("일일 팁이 성공적으로 생성되었습니다.");
    }
    
    @PostMapping("/create")
    @Operation(summary = "수동 팁 생성", description = "관리자가 수동으로 팁을 생성합니다.")
    public ResponseEntity<AiTipResponseDto> createTip(
            @Parameter(description = "사용자 ID") @RequestParam Long userId,
            @Parameter(description = "팁 유형") @RequestParam String tipType,
            @Parameter(description = "제목") @RequestParam String title,
            @Parameter(description = "내용") @RequestParam String content,
            @Parameter(description = "작물 유형") @RequestParam(required = false) String cropType) {
        
        AiTip.TipType tipTypeEnum = AiTip.TipType.valueOf(tipType);
        AiTipResponseDto tip = aiTipService.createTip(userId, tipTypeEnum, title, content, cropType);
        
        return ResponseEntity.ok(tip);
    }
    
    @PutMapping("/{tipId}/read")
    @Operation(summary = "팁 읽음 처리", description = "특정 팁을 읽음 상태로 변경합니다.")
    public ResponseEntity<String> markTipAsRead(
            @Parameter(description = "팁 ID") @PathVariable Long tipId) {
        
        Long userId = getCurrentUserId();
        aiTipService.markTipAsRead(userId, tipId);
        
        return ResponseEntity.ok("팁이 읽음 상태로 변경되었습니다.");
    }
    
    @GetMapping("/types")
    @Operation(summary = "팁 유형 목록", description = "사용 가능한 모든 팁 유형을 조회합니다.")
    public ResponseEntity<List<TipTypeInfo>> getTipTypes() {
        List<TipTypeInfo> tipTypes = List.of(
                new TipTypeInfo("WEATHER_ALERT", "날씨 기반 알림", "🌡️"),
                new TipTypeInfo("CROP_GUIDE", "작물별 생육 가이드", "🌱"),
                new TipTypeInfo("PEST_ALERT", "병해충 조기 경보", "🚨"),
                new TipTypeInfo("PROFIT_TIP", "수익 최적화 팁", "📊"),
                new TipTypeInfo("AUTOMATION_SUGGESTION", "스마트팜 자동화 제안", "⚡"),
                new TipTypeInfo("LABOR_MATCHING", "일손 매칭 AI 추천", "🎯")
        );
        
        return ResponseEntity.ok(tipTypes);
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
    
    // 팁 유형 정보 클래스
    public record TipTypeInfo(String code, String description, String icon) {}
}
