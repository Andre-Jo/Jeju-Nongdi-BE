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
    private String productName; // 상품명 (cropName과 동일하게 사용)
    private Double currentPrice; // 현재 가격
    private Double previousPrice; // 이전 가격 (비교용)
    private Double priceChange; // 가격 변동률 (%)
    private Double priceChangeRate; // 가격 변동률 (%) - priceChange와 동일
    private String unit; // 단위 (kg, 상자 등)
    private String marketType; // 시장 유형 (도매, 소매)
    private String region; // 지역
    private String lastUpdated; // 최종 업데이트 날짜
    private String marketCondition; // 시장 상황 (강세, 약세 등)
    private String tradeVolume; // 거래량
    
    // 편의 getter 메서드들
    public String getProductName() {
        return productName != null ? productName : cropName;
    }
    
    public Double getPriceChangeRate() {
        return priceChangeRate != null ? priceChangeRate : priceChange;
    }
    
    // 편의 메서드들
    public boolean isPriceRising() {
        Double rate = getPriceChangeRate();
        return rate != null && rate > 0;
    }
    
    public boolean isPriceFalling() {
        Double rate = getPriceChangeRate();
        return rate != null && rate < 0;
    }
    
    public boolean isSignificantChange() {
        Double rate = getPriceChangeRate();
        return rate != null && Math.abs(rate) > 10;
    }
    
    public String getPriceChangeDescription() {
        Double rate = getPriceChangeRate();
        if (rate == null) {
            return "변동 정보 없음";
        }
        
        if (rate > 0) {
            return String.format("%.1f%% 상승", rate);
        } else if (rate < 0) {
            return String.format("%.1f%% 하락", Math.abs(rate));
        } else {
            return "변동 없음";
        }
    }
    
    public String getFormattedPrice() {
        if (currentPrice == null) {
            return "가격 정보 없음";
        }
        return String.format("%.0f원/%s", currentPrice, unit != null ? unit : "kg");
    }
    
    public String getFormattedSummary() {
        return String.format("%s: %s (%s, %s시장)", 
                getProductName(), getFormattedPrice(), getPriceChangeDescription(), 
                marketType != null ? marketType : "일반");
    }
    
    public String getTradeRecommendation() {
        Double rate = getPriceChangeRate();
        if (rate == null) {
            return "정보 부족으로 판단 어려움";
        }
        
        if (rate > 15) {
            return "출하 적기 - 즉시 판매 권장";
        } else if (rate > 5) {
            return "출하 고려 시기";
        } else if (rate < -15) {
            return "저장 후 출하 권장";
        } else if (rate < -5) {
            return "출하 연기 고려";
        } else {
            return "일반적인 출하 시기";
        }
    }
}
