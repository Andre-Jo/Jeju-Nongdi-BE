package com.jeju_nongdi.jeju_nongdi.service;

import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherApiClient;
import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherApiClient.AiAgricultureTip;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * AI 농업 팁 스케줄러 서비스
 * 매일 아침/저녁에 농업 팁 알림을 생성하고 전송
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
     * 🌅 아침 AI 팁 알림 (매일 오전 7시)
     * "오늘 작업 가이드" - 오늘 날씨 기반 작업 계획
     */
    @Scheduled(cron = "0 0 7 * * *", zone = "Asia/Seoul")
    public void sendMorningTip() {
        try {
            log.info("🌅 아침 AI 농업 팁 생성 시작...");
            
            weatherApiClient.generateMorningTip(JEJU_LAT, JEJU_LON)
                    .subscribe(
                        tip -> {
                            String formattedTip = formatMorningTip(tip);
                            log.info("🎯 아침 AI 팁 생성 완료:\n{}", formattedTip);
                            
                            // TODO: 실제 푸시 알림 전송 (FCM, 웹소켓 등)
                            sendPushNotification("MORNING", formattedTip);
                        },
                        error -> {
                            log.error("❌ 아침 AI 팁 생성 실패: {}", error.getMessage(), error);
                            sendFallbackMorningTip();
                        }
                    );
                    
        } catch (Exception e) {
            log.error("❌ 아침 AI 팁 스케줄러 오류: {}", e.getMessage(), e);
            sendFallbackMorningTip();
        }
    }
    
    /**
     * 🌙 저녁 AI 팁 알림 (매일 오후 7시)
     * "내일 대비 준비" - 향후 3-5일 위험 기상 대비
     */
    @Scheduled(cron = "0 0 19 * * *", zone = "Asia/Seoul")
    public void sendEveningTip() {
        try {
            log.info("🌙 저녁 AI 농업 팁 생성 시작...");
            
            weatherApiClient.generateEveningTip(JEJU_LAT, JEJU_LON)
                    .subscribe(
                        tip -> {
                            String formattedTip = formatEveningTip(tip);
                            log.info("🎯 저녁 AI 팁 생성 완료:\n{}", formattedTip);
                            
                            // TODO: 실제 푸시 알림 전송 (FCM, 웹소켓 등)
                            sendPushNotification("EVENING", formattedTip);
                        },
                        error -> {
                            log.error("❌ 저녁 AI 팁 생성 실패: {}", error.getMessage(), error);
                            sendFallbackEveningTip();
                        }
                    );
                    
        } catch (Exception e) {
            log.error("❌ 저녁 AI 팁 스케줄러 오류: {}", e.getMessage(), e);
            sendFallbackEveningTip();
        }
    }
    
    /**
     * 아침 팁 포맷팅
     */
    private String formatMorningTip(AiAgricultureTip tip) {
        StringBuilder formatted = new StringBuilder();
        
        // 헤더
        formatted.append("🌅 오늘의 농업 가이드\n");
        formatted.append("━━━━━━━━━━━━━━━━━━━\n\n");
        
        // 메인 메시지
        formatted.append("📢 ").append(tip.getMainMessage()).append("\n\n");
        
        // 위험 기상 알림들
        if (!tip.getAlerts().isEmpty()) {
            formatted.append("⚠️ 기상 경보:\n");
            for (var alert : tip.getAlerts()) {
                formatted.append("• ").append(alert.getTitle()).append("\n");
                formatted.append("  ").append(alert.getDescription()).append("\n");
            }
            formatted.append("\n");
        }
        
        // 오늘 해야 할 일
        if (!tip.getTodayActions().isEmpty()) {
            formatted.append("✅ 오늘 작업 계획:\n");
            for (String action : tip.getTodayActions()) {
                formatted.append("• ").append(action).append("\n");
            }
            formatted.append("\n");
        }
        
        // 미리 준비할 일
        if (!tip.getPreparationActions().isEmpty()) {
            formatted.append("🔧 미리 준비하세요:\n");
            for (String action : tip.getPreparationActions()) {
                formatted.append("• ").append(action).append("\n");
            }
        }
        
        return formatted.toString();
    }
    
    /**
     * 저녁 팁 포맷팅
     */
    private String formatEveningTip(AiAgricultureTip tip) {
        StringBuilder formatted = new StringBuilder();
        
        // 헤더
        formatted.append("🌙 내일 대비 농업 가이드\n");
        formatted.append("━━━━━━━━━━━━━━━━━━━\n\n");
        
        // 메인 메시지
        formatted.append("📢 ").append(tip.getMainMessage()).append("\n\n");
        
        // 향후 위험 기상 알림들
        if (!tip.getAlerts().isEmpty()) {
            formatted.append("🚨 향후 기상 위험:\n");
            for (var alert : tip.getAlerts()) {
                formatted.append("• ").append(alert.getTitle()).append("\n");
                formatted.append("  ").append(alert.getDescription()).append("\n");
                
                // 준비 사항들
                if (!alert.getActionItems().isEmpty()) {
                    formatted.append("  📋 준비사항:\n");
                    for (String action : alert.getActionItems()) {
                        formatted.append("    - ").append(action).append("\n");
                    }
                }
            }
            formatted.append("\n");
        }
        
        // 오늘 저녁 해야 할 일
        if (!tip.getTodayActions().isEmpty()) {
            formatted.append("🌆 오늘 저녁 할 일:\n");
            for (String action : tip.getTodayActions()) {
                formatted.append("• ").append(action).append("\n");
            }
        }
        
        return formatted.toString();
    }
    
    /**
     * 푸시 알림 전송 (추후 구현)
     */
    private void sendPushNotification(String type, String message) {
        // TODO: FCM, 웹소켓, 이메일 등으로 실제 알림 전송
        log.info("📱 [{}] 푸시 알림 전송 준비: {}", type, message.substring(0, Math.min(100, message.length())));
        
        // 현재는 로그만 출력 (나중에 실제 알림 시스템 연동)
    }
    
    /**
     * 아침 팁 생성 실패 시 폴백 메시지
     */
    private void sendFallbackMorningTip() {
        String fallbackMessage = """
            🌅 오늘의 농업 가이드
            ━━━━━━━━━━━━━━━━━━━
            
            📢 기상 데이터를 불러올 수 없어 기본 가이드를 제공합니다.
            
            ✅ 오늘 기본 작업:
            • 🌱 작물 상태 점검 및 물주기
            • 🔧 농기구 정비 및 정리
            • 📊 농작업 기록 정리
            • 🌿 잡초 제거 및 토양 관리
            
            ⚠️ 작업 전 날씨를 직접 확인하세요!
            """;
            
        log.info("🎯 아침 폴백 메시지 전송:\n{}", fallbackMessage);
        sendPushNotification("MORNING_FALLBACK", fallbackMessage);
    }
    
    /**
     * 저녁 팁 생성 실패 시 폴백 메시지
     */
    private void sendFallbackEveningTip() {
        String fallbackMessage = """
            🌙 내일 대비 농업 가이드
            ━━━━━━━━━━━━━━━━━━━
            
            📢 기상 데이터를 불러올 수 없어 기본 가이드를 제공합니다.
            
            🌆 오늘 저녁 할 일:
            • 🔧 농기구 정리 및 실내 보관
            • 💧 급수 시설 점검
            • 📱 내일 날씨 예보 확인
            • 🌿 내일 작업 계획 수립
            
            ⚠️ 내일 아침 날씨를 꼭 확인하세요!
            """;
            
        log.info("🎯 저녁 폴백 메시지 전송:\n{}", fallbackMessage);
        sendPushNotification("EVENING_FALLBACK", fallbackMessage);
    }
    
    /**
     * 수동 테스트용 메서드들
     */
    public void testMorningTip() {
        log.info("🧪 아침 팁 테스트 실행");
        sendMorningTip();
    }
    
    public void testEveningTip() {
        log.info("🧪 저녁 팁 테스트 실행");
        sendEveningTip();
    }
}
