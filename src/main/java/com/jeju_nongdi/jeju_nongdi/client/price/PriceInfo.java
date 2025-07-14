package com.jeju_nongdi.jeju_nongdi.client.price;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceInfo {
    
    private String cropName; // 작물명
    private Double currentPrice; // 현재 가격
    private Double previousPrice; // 이전 가격 (비교용)
    private Double priceChange; // 가격 변동률 (%)
    private String unit; // 단위 (kg, 상자 등)
    private String marketType; // 시장 유형 (도매, 소매)
    private String region; // 지역
    private String lastUpdated; // 최종 업데이트 날짜
    
    // 편의 메서드들
    public boolean isPriceRising() {
        return priceChange != null && priceChange > 0;
    }
    
    public boolean isPriceFalling() {
        return priceChange != null && priceChange < 0;
    }
    
    public boolean isSignificantChange() {
        return priceChange != null && Math.abs(priceChange) > 10;
    }
    
    public String getPriceChangeDescription() {
        if (priceChange == null) {
            return "변동 정보 없음";
        }
        
        if (priceChange > 0) {
            return String.format("%.1f%% 상승", priceChange);
        } else if (priceChange < 0) {
            return String.format("%.1f%% 하락", Math.abs(priceChange));
        } else {
            return "변동 없음";
        }
    }
    
    public String getFormattedPrice() {
        return String.format("%.0f원/%s", currentPrice, unit);
    }
    
    public String getFormattedSummary() {
        return String.format("%s: %s (%s, %s시장)", 
                cropName, getFormattedPrice(), getPriceChangeDescription(), marketType);
    }
    
    public String getTradeRecommendation() {
        if (priceChange == null) {
            return "정보 부족으로 판단 어려움";
        }
        
        if (priceChange > 15) {
            return "출하 적기 - 즉시 판매 권장";
        } else if (priceChange > 5) {
            return "출하 고려 시기";
        } else if (priceChange < -15) {
            return "저장 후 출하 권장";
        } else if (priceChange < -5) {
            return "출하 연기 고려";
        } else {
            return "일반적인 출하 시기";
        }
    }
}
