package com.jeju_nongdi.jeju_nongdi.client.ai;

import com.jeju_nongdi.jeju_nongdi.client.price.PriceInfo;
import com.jeju_nongdi.jeju_nongdi.prefernence.entity.UserPreference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class OpenAiClient {
    
    /**
     * 작물별 생육 단계 가이드 생성 (규칙 기반)
     */
    public String generateCropGuide(String cropName, String currentSeason) {
        log.info("규칙 기반 작물 가이드 생성: {} - {}", cropName, currentSeason);
        return generateFallbackCropGuide(cropName);
    }
    
    /**
     * 수익성 분석 및 출하 시기 조언 (규칙 기반)
     */
    public String generateProfitAnalysis(List<PriceInfo> priceInfos, UserPreference userPreference) {
        log.info("규칙 기반 수익성 분석 생성: {} 개 가격 정보", priceInfos.size());
        return generateFallbackProfitAnalysis(priceInfos);
    }
    
    /**
     * 일반적인 농업 질의응답 (규칙 기반)
     */
    public String generateFarmingAdvice(String question, UserPreference userPreference) {
        log.info("규칙 기반 농업 조언 생성: {}", question);
        return "제주도 농업 전문가 조언\n\n" +
               "문의해주신 내용에 대해 제주도의 기후와 토양 특성을 고려한 조언을 드립니다.\n\n" +
               "🌱 기본 권장사항:\n" +
               "- 정기적인 농장 점검 실시\n" +
               "- 계절에 맞는 적절한 관리\n" +
               "- 지역 농업기술센터와의 상담 활용\n\n" +
               "추가 상세한 정보가 필요하시면 제주농업기술센터(064-760-7000)로 문의하세요.";
    }
    
    /**
     * 작물별 기본 관리 가이드 생성 (폴백 메서드)
     */
    private String generateFallbackCropGuide(String cropName) {
        return String.format("""
                🌱 %s 기본 관리 가이드
                
                💧 물 관리: 토양 상태를 확인하여 적절히 급수
                🌿 비료 관리: 생육 단계에 맞는 비료 시비
                ✂️ 관리 작업: 정기적인 점검 및 관리
                🐛 병해충 예방: 예방적 방제 실시
                📅 향후 작업: 계절에 맞는 관리 작업 계획
                
                자세한 사항은 농업기술센터에 문의하세요.
                """, cropName);
    }
    
    /**
     * 가격 기반 수익성 분석 생성 (폴백 메서드)
     */
    private String generateFallbackProfitAnalysis(List<PriceInfo> priceInfos) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("📊 가격 동향 분석\n");
        for (PriceInfo price : priceInfos) {
            analysis.append(String.format("- %s: %s\n", price.getCropName(), price.getTradeRecommendation()));
        }
        
        analysis.append("\n💰 수익성 평가\n");
        analysis.append("현재 시장 상황을 종합적으로 검토하여 출하 계획을 수립하세요.\n");
        
        return analysis.toString();
    }
}
