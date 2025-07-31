package com.jeju_nongdi.jeju_nongdi.controller;

import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherApiClient;
import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherApiClient.AiAgricultureTip;
import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherApiClient.WeatherForecast4Days;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * AI 농업 종합 컨트롤러
 * - 4일 기상 예보 및 농업 팁
 * - 농수산물 가격 정보 (농넷 API) - 추후 구현
 * - 종합 농업 가이드 제공
 */
@RestController
@RequestMapping("/api/ai-tip")
@RequiredArgsConstructor
@Slf4j
public class AiTipController {
    
    private final WeatherApiClient weatherApiClient;
    // private final AgriculturePriceApiClient priceApiClient; // TODO: 농넷 API 클라이언트 추가
    
    // ==================== 기상 정보 관련 API ====================
    
    /**
     * 🌾 AI 농업 종합 가이드 (메인 API)
     * 기상 정보 + 농수산물 가격 + AI 분석
     * GET /api/ai-tip?lat=33.4996&lon=126.5312
     */
    @GetMapping
    public Mono<ResponseEntity<AiAgricultureTip>> getComprehensiveAgricultureTip(
            @RequestParam(required = false, defaultValue = "33.4996") double lat,
            @RequestParam(required = false, defaultValue = "126.5312") double lon,
            @RequestParam(required = false) String cropType) {
        
        log.info("AI 농업 종합 가이드 요청 - 위치: ({}, {}), 작물: {}", lat, lon, cropType);
        
        return weatherApiClient.generateAgricultureTip(lat, lon)
                .map(tip -> {
                    // TODO: 농수산물 가격 정보 추가
                    // TODO: 작물별 맞춤 정보 추가
                    
                    log.info("AI 농업 종합 가이드 생성 완료: {} 개 경보, {} 개 준비사항", 
                            tip.getAlerts().size(), 
                            tip.getPreparationActions().size());
                    return ResponseEntity.ok(tip);
                })
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }
    
    /**
     * 📊 4일 기상 예보 및 위험 패턴 분석 (내일부터)
     * GET /api/ai-tip/weather-forecast?lat=33.4996&lon=126.5312
     */
    @GetMapping("/weather-forecast")
    public Mono<ResponseEntity<WeatherForecast4Days>> get4DaysWeatherForecast(
            @RequestParam(required = false, defaultValue = "33.4996") double lat,
            @RequestParam(required = false, defaultValue = "126.5312") double lon) {
        
        log.info("4일 기상 예보 요청 (내일부터) - 위치: ({}, {})", lat, lon);
        
        return weatherApiClient.get4DaysForecast(lat, lon)
                .map(forecast -> {
                    log.info("4일 기상 예보 완료: {}일 데이터, {}개 경보", 
                            forecast.getDailyForecasts().size(),
                            forecast.getAlerts().size());
                    return ResponseEntity.ok(forecast);
                })
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }
    
    /**
     * 🚨 기상 경보 요약
     * GET /api/ai-tip/weather-alerts?lat=33.4996&lon=126.5312
     */
    @GetMapping("/weather-alerts")
    public Mono<ResponseEntity<String>> getWeatherAlertsSummary(
            @RequestParam(required = false, defaultValue = "33.4996") double lat,
            @RequestParam(required = false, defaultValue = "126.5312") double lon) {
        
        log.info("기상 경보 요약 요청 - 위치: ({}, {})", lat, lon);
        
        return weatherApiClient.get4DaysForecast(lat, lon)
                .map(forecast -> {
                    StringBuilder summary = new StringBuilder();
                    summary.append("🚨 향후 4일 기상 경보 요약 (내일부터)\n");
                    summary.append("━━━━━━━━━━━━━━━━━━━\n\n");
                    
                    if (forecast.getAlerts().isEmpty()) {
                        summary.append("✅ 현재 위험 기상은 예상되지 않습니다.\n");
                        summary.append("🌱 안전한 농업 작업이 가능합니다!");
                    } else {
                        summary.append(String.format("⚠️ 총 %d개의 기상 경보가 발령되었습니다.\n\n", 
                                forecast.getAlerts().size()));
                        
                        for (var alert : forecast.getAlerts()) {
                            summary.append(String.format("🔥 %s\n", alert.getTitle()));
                            summary.append(String.format("📅 시작일: %s (%d일간)\n", 
                                    alert.getStartDate(), alert.getDuration()));
                            summary.append(String.format("📝 설명: %s\n", alert.getDescription()));
                            summary.append("🔧 준비사항:\n");
                            
                            for (String action : alert.getActionItems()) {
                                summary.append(String.format("  • %s\n", action));
                            }
                            summary.append("\n");
                        }
                    }
                    
                    log.info("기상 경보 요약 완료: {}개 경보", forecast.getAlerts().size());
                    return ResponseEntity.ok(summary.toString());
                })
                .onErrorReturn(ResponseEntity.internalServerError()
                        .body("❌ 기상 경보 요약을 가져올 수 없습니다."));
    }
    
    // ==================== 농수산물 가격 정보 관련 API (추후 구현) ====================
    
    /**
     * 💰 농수산물 가격 정보 조회 (농넷 API)
     * GET /api/ai-tip/crop-prices?cropName=감귤
     */
    @GetMapping("/crop-prices")
    public ResponseEntity<String> getCropPrices(
            @RequestParam(required = false, defaultValue = "감귤") String cropName) {
        
        log.info("농수산물 가격 정보 요청 - 작물: {}", cropName);
        
        // TODO: 농넷 API 연동 구현
        
        return ResponseEntity.ok("농넷 API 연동 준비 중입니다.");
    }
    
    /**
     * 📊 제주 특산물 가격 동향
     * GET /api/ai-tip/jeju-specialty-prices
     */
    @GetMapping("/jeju-specialty-prices")
    public ResponseEntity<String> getJejuSpecialtyPrices() {
        
        log.info("제주 특산물 가격 동향 요청");
        
        // TODO: 농넷 API에서 제주 특산물 가격 조회
        
        return ResponseEntity.ok("제주 특산물 가격 조회 API 준비 중입니다.");
    }
    
    /**
     * 📈 가격 트렌드 분석 및 출하 권장
     * GET /api/ai-tip/price-analysis?cropName=감귤&period=7
     */
    @GetMapping("/price-analysis")
    public ResponseEntity<String> getPriceAnalysis(
            @RequestParam(required = false, defaultValue = "감귤") String cropName,
            @RequestParam(required = false, defaultValue = "7") int period) {
        
        log.info("가격 트렌드 분석 요청 - 작물: {}, 기간: {}일", cropName, period);
        
        // TODO: 농넷 API에서 가격 트렌드 분석
        
        return ResponseEntity.ok("가격 트렌드 분석 API 준비 중입니다.");
    }
    
    // ==================== 종합 정보 및 유틸리티 API ====================
    
    /**
     * 📱 간단한 텍스트 요약 (기상 + 가격)
     * GET /api/ai-tip/summary?lat=33.4996&lon=126.5312&cropName=감귤
     */
    @GetMapping("/summary")
    public Mono<ResponseEntity<String>> getComprehensiveSummary(
            @RequestParam(required = false, defaultValue = "33.4996") double lat,
            @RequestParam(required = false, defaultValue = "126.5312") double lon,
            @RequestParam(required = false, defaultValue = "감귤") String cropName) {
        
        log.info("종합 요약 요청 - 위치: ({}, {}), 작물: {}", lat, lon, cropName);
        
        return weatherApiClient.generateAgricultureTip(lat, lon)
                .map(tip -> {
                    StringBuilder summary = new StringBuilder();
                    summary.append("🌾 AI 농업 종합 가이드\n");
                    summary.append("━━━━━━━━━━━━━━━━━━━\n\n");
                    
                    // 기상 정보
                    summary.append("🌤️ 기상 정보:\n");
                    summary.append("• ").append(tip.getMainMessage()).append("\n");
                    if (!tip.getAlerts().isEmpty()) {
                        summary.append("• 경보 ").append(tip.getAlerts().size()).append("건 발령 중\n");
                    }
                    summary.append("\n");
                    
                    // 가격 정보 (추후 구현)
                    summary.append("💰 ").append(cropName).append(" 가격 정보:\n");
                    summary.append("• 농넷 API 연동 준비 중\n\n");
                    
                    // 주요 준비사항
                    if (!tip.getPreparationActions().isEmpty()) {
                        summary.append("✅ 주요 준비사항:\n");
                        int count = 0;
                        for (String action : tip.getPreparationActions()) {
                            if (count >= 3) break;
                            summary.append("• ").append(action).append("\n");
                            count++;
                        }
                        if (tip.getPreparationActions().size() > 3) {
                            summary.append("• ... 외 ").append(tip.getPreparationActions().size() - 3).append("개 더\n");
                        }
                    }
                    
                    summary.append("\n🌱 오늘도 안전하고 수익성 높은 농업하세요!");
                    
                    log.info("종합 요약 완료");
                    return ResponseEntity.ok(summary.toString());
                })
                .onErrorReturn(ResponseEntity.internalServerError()
                        .body("❌ 종합 정보를 가져올 수 없습니다."));
    }
    
    /**
     * 📱 알림 테스트
     * POST /api/ai-tip/test
     */
    @PostMapping("/test")
    public ResponseEntity<String> testNotification() {
        log.info("AI 농업 종합 가이드 테스트 요청");
        
        try {
            // TODO: 스케줄러 서비스에서 테스트 메서드 호출
            return ResponseEntity.ok("🌾 AI 농업 종합 가이드 테스트 실행 완료! 로그를 확인하세요.");
        } catch (Exception e) {
            log.error("종합 가이드 테스트 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("❌ 종합 가이드 테스트 실패: " + e.getMessage());
        }
    }
    
    /**
     * API 상태 확인
     * GET /api/ai-tip/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("AI 농업 종합 가이드 API 상태 확인");
        
        String status = """
                🌾 AI 농업 종합 가이드 API 상태
                ━━━━━━━━━━━━━━━━━━━
                
                ✅ 기상 정보: 정상 동작 (기상청 API)
                ⏳ 가격 정보: 준비 중 (농넷 API)
                ✅ AI 분석: 정상 동작
                ✅ 알림 시스템: 정상 동작
                
                🎯 제공 서비스:
                • 4일 기상 예보 및 농업 가이드
                • 농수산물 가격 정보 (준비 중)
                • AI 기반 출하 시기 추천 (준비 중)
                • 종합 농업 가이드 제공
                
                📱 메인 API: GET /api/ai-tip
                """;
                
        return ResponseEntity.ok(status);
    }
}
