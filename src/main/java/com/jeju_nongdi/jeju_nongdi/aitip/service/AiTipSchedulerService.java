package com.jeju_nongdi.jeju_nongdi.aitip.service;

import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherApiClient;
import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherApiClient.AiAgricultureTip;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * AI 농업 팁 스케줄러 서비스
 * 매일 정해진 시간에 4일 예보 기반 농업 팁 알림 생성 (내일부터)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiTipSchedulerService {
    
    private final WeatherApiClient weatherApiClient;
    
    // 제주시 기본 좌표 (추후 사용자별 좌표로 확장)
    private static final double JEJU_LAT = 33.4996;
    private static final double JEJU_LON = 126.5312;
    
    /**
     * 🌾 매일 농업 팁 알림 (오전 7시)
     * "내일부터 4일간 농업 가이드" - 4일 예보 기반 종합 팁
     */
    @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Seoul")
    public void sendDailyAgricultureTip() {
        try {
            log.info("🌾 매일 AI 농업 팁 생성 시작...");
            
            weatherApiClient.generateAgricultureTip(JEJU_LAT, JEJU_LON)
                    .subscribe(
                        tip -> {
                            String formattedTip = formatAgricultureTip(tip);
                            log.info("🎯 AI 농업 팁 생성 완료:\n{}", formattedTip);
                            
                            // TODO: 실제 푸시 알림 전송 (FCM, 웹소켓 등)
                            sendPushNotification(formattedTip);
                        },
                        error -> {
                            log.error("❌ AI 농업 팁 생성 실패: {}", error.getMessage(), error);
                            sendFallbackTip();
                        }
                    );
                    
        } catch (Exception e) {
            log.error("❌ AI 농업 팁 스케줄러 오류: {}", e.getMessage(), e);
            sendFallbackTip();
        }
    }
    
    /**
     * 농업 팁 포맷팅
     */
    private String formatAgricultureTip(AiAgricultureTip tip) {
        StringBuilder formatted = new StringBuilder();
        
        // 헤더
        formatted.append("🌾 AI 농업 가이드 (내일부터 4일간)\n");
        formatted.append("━━━━━━━━━━━━━━━━━━━\n\n");
        
        // 메인 메시지
        formatted.append("📢 ").append(tip.getMainMessage()).append("\n\n");
        
        // 위험 기상 알림들
        if (!tip.getAlerts().isEmpty()) {
            formatted.append("🚨 기상 경보:\n");
            for (var alert : tip.getAlerts()) {
                formatted.append("• ").append(alert.getTitle()).append("\n");
                formatted.append("  ").append(alert.getDescription()).append("\n");
                
                // 중요한 준비사항 2-3개만 표시
                if (!alert.getActionItems().isEmpty()) {
                    formatted.append("  🔧 주요 준비사항:\n");
                    int count = 0;
                    for (String action : alert.getActionItems()) {
                        if (count >= 2) break; // 최대 2개만
                        formatted.append("    - ").append(action).append("\n");
                        count++;
                    }
                    if (alert.getActionItems().size() > 2) {
                        formatted.append("    - ... 외 ").append(alert.getActionItems().size() - 2).append("개 더\n");
                    }
                }
                formatted.append("\n");
            }
        }
        
        // 전체 준비사항 요약
        if (!tip.getPreparationActions().isEmpty()) {
            formatted.append("✅ 오늘 미리 준비하세요:\n");
            // 중복 제거하고 중요한 것만 5개 정도 표시
            var uniqueActions = tip.getPreparationActions().stream()
                    .distinct()
                    .limit(5)
                    .toList();
            
            for (String action : uniqueActions) {
                formatted.append("• ").append(action).append("\n");
            }
            
            if (tip.getPreparationActions().size() > 5) {
                formatted.append("• ... 외 ").append(tip.getPreparationActions().size() - 5).append("개 더\n");
            }
        }
        
        // 푸터
        formatted.append("\n🌱 안전하고 효율적인 농업 작업하세요!");
        
        return formatted.toString();
    }
    
    /**
     * 푸시 알림 전송 (추후 구현)
     */
    private void sendPushNotification(String message) {
        // TODO: FCM, 웹소켓, 이메일 등으로 실제 알림 전송
        log.info("📱 푸시 알림 전송 준비: {}", message.substring(0, Math.min(100, message.length())));
        
        // 현재는 로그만 출력 (나중에 실제 알림 시스템 연동)
    }
    
    /**
     * 팁 생성 실패 시 폴백 메시지
     */
    private void sendFallbackTip() {
        String fallbackMessage = """
            🌾 AI 농업 가이드
            ━━━━━━━━━━━━━━━━━━━
            
            📢 기상 데이터를 불러올 수 없어 기본 가이드를 제공합니다.
            
            ✅ 오늘 기본 농업 준비사항:
            • 🌱 작물 상태 점검 및 물주기
            • 🔧 농기구 정비 및 정리
            • 📊 농작업 기록 정리
            • 🌿 잡초 제거 및 토양 관리
            • 💧 급수 시설 점검
            
            ⚠️ 작업 전 날씨를 직접 확인하세요!
            🌱 안전하고 효율적인 농업 작업하세요!
            """;
            
        log.info("🎯 폴백 메시지 전송:\n{}", fallbackMessage);
        sendPushNotification(fallbackMessage);
    }
    
    /**
     * 수동 테스트용 메서드
     */
    public void testDailyTip() {
        log.info("🧪 매일 농업 팁 테스트 실행");
        sendDailyAgricultureTip();
    }
}
