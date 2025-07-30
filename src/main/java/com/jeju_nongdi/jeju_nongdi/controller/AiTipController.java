package com.jeju_nongdi.jeju_nongdi.controller;

import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherApiClient;
import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherApiClient.AiAgricultureTip;
import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherApiClient.WeatherForecast5Days;
import com.jeju_nongdi.jeju_nongdi.service.AiTipSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * AI 농업 팁 컨트롤러
 * 5일 예보 분석 및 AI 팁 제공
 */
@RestController
@RequestMapping("/api/ai-tip")
@RequiredArgsConstructor
@Slf4j
public class AiTipController {
    
    private final WeatherApiClient weatherApiClient;
    private final AiTipSchedulerService schedulerService;
    
    /**
     * 5일 기상 예보 및 위험 패턴 분석
     * GET /api/ai-tip/forecast?lat=33.4996&lon=126.5312
     */
    @GetMapping("/forecast")
    public Mono<ResponseEntity<WeatherForecast5Days>> get5DaysForecast(
            @RequestParam(required = false, defaultValue = "33.4996") double lat,
            @RequestParam(required = false, defaultValue = "126.5312") double lon) {
        
        log.info("5일 예보 분석 요청 - 위치: ({}, {})", lat, lon);
        
        return weatherApiClient.get5DaysForecast(lat, lon)
                .map(forecast -> {
                    log.info("5일 예보 분석 완료: {}일 데이터, {}개 경보", 
                            forecast.getDailyForecasts().size(),
                            forecast.getAlerts().size());
                    return ResponseEntity.ok(forecast);
                })
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }
    
    /**
     * 🌅 아침 AI 농업 팁
     * GET /api/ai-tip/morning?lat=33.4996&lon=126.5312
     */
    @GetMapping("/morning")
    public Mono<ResponseEntity<AiAgricultureTip>> getMorningTip(
            @RequestParam(required = false, defaultValue = "33.4996") double lat,
            @RequestParam(required = false, defaultValue = "126.5312") double lon) {
        
        log.info("아침 AI 팁 요청 - 위치: ({}, {})", lat, lon);
        
        return weatherApiClient.generateMorningTip(lat, lon)
                .map(tip -> {
                    log.info("아침 AI 팁 생성 완료: {} 개 경보, {} 개 액션", 
                            tip.getAlerts().size(), 
                            tip.getTodayActions().size());
                    return ResponseEntity.ok(tip);
                })
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }
    
    /**
     * 🌙 저녁 AI 농업 팁
     * GET /api/ai-tip/evening?lat=33.4996&lon=126.5312
     */
    @GetMapping("/evening")
    public Mono<ResponseEntity<AiAgricultureTip>> getEveningTip(
            @RequestParam(required = false, defaultValue = "33.4996") double lat,
            @RequestParam(required = false, defaultValue = "126.5312") double lon) {
        
        log.info("저녁 AI 팁 요청 - 위치: ({}, {})", lat, lon);
        
        return weatherApiClient.generateEveningTip(lat, lon)
                .map(tip -> {
                    log.info("저녁 AI 팁 생성 완료: {} 개 경보, {} 개 준비사항", 
                            tip.getAlerts().size(), 
                            tip.getPreparationActions().size());
                    return ResponseEntity.ok(tip);
                })
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }
    
    /**
     * 📱 아침 팁 푸시 알림 테스트
     * POST /api/ai-tip/test/morning
     */
    @PostMapping("/test/morning")
    public ResponseEntity<String> testMorningNotification() {
        log.info("아침 푸시 알림 테스트 요청");
        
        try {
            schedulerService.testMorningTip();
            return ResponseEntity.ok("🌅 아침 AI 팁 테스트 실행 완료! 로그를 확인하세요.");
        } catch (Exception e) {
            log.error("아침 팁 테스트 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("❌ 아침 팁 테스트 실패: " + e.getMessage());
        }
    }
    
    /**
     * 📱 저녁 팁 푸시 알림 테스트
     * POST /api/ai-tip/test/evening
     */
    @PostMapping("/test/evening")
    public ResponseEntity<String> testEveningNotification() {
        log.info("저녁 푸시 알림 테스트 요청");
        
        try {
            schedulerService.testEveningTip();
            return ResponseEntity.ok("🌙 저녁 AI 팁 테스트 실행 완료! 로그를 확인하세요.");
        } catch (Exception e) {
            log.error("저녁 팁 테스트 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("❌ 저녁 팁 테스트 실패: " + e.getMessage());
        }
    }
    
    /**
     * 📊 위험 기상 경보 요약
     * GET /api/ai-tip/alerts?lat=33.4996&lon=126.5312
     */
    @GetMapping("/alerts")
    public Mono<ResponseEntity<String>> getWeatherAlertsSummary(
            @RequestParam(required = false, defaultValue = "33.4996") double lat,
            @RequestParam(required = false, defaultValue = "126.5312") double lon) {
        
        log.info("기상 경보 요약 요청 - 위치: ({}, {})", lat, lon);
        
        return weatherApiClient.get5DaysForecast(lat, lon)
                .map(forecast -> {
                    StringBuilder summary = new StringBuilder();
                    summary.append("🚨 향후 5일 기상 경보 요약\n");
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
    
    /**
     * 🔧 스케줄러 상태 확인
     * GET /api/ai-tip/scheduler/status
     */
    @GetMapping("/scheduler/status")
    public ResponseEntity<String> getSchedulerStatus() {
        String status = """
            📅 AI 농업 팁 스케줄러 상태
            ━━━━━━━━━━━━━━━━━━━
            
            🌅 아침 알림: 매일 오전 7시 (KST)
            🌙 저녁 알림: 매일 오후 7시 (KST)
            
            📊 제공 정보:
            • 5일 기상 예보 분석
            • 위험 기상 패턴 감지
            • 농업 작업 가이드
            • 사전 준비 사항 안내
            
            🎯 위험 기상 감지 기준:
            • 폭염: 연속 3일 이상 35°C 초과
            • 집중호우: 강수확률 80% 이상 또는 30mm 이상
            • 강풍: 풍속 10m/s 이상
            • 급격한 기온변화: 일교차 15°C 이상
            
            ✅ 스케줄러 정상 동작 중
            """;
            
        return ResponseEntity.ok(status);
    }
    
    /**
     * API 상태 확인
     * GET /api/ai-tip/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("AI 팁 API 상태 확인");
        return ResponseEntity.ok("AI Agriculture Tip API is running! 🌾🤖");
    }
}
