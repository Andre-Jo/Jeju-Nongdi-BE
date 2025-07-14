package com.jeju_nongdi.jeju_nongdi.client.weather;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherInfo {
    
    private String temperature; // 기온 (°C)
    private String humidity; // 습도 (%)
    private Integer rainProbability; // 강수확률 (%)
    private String skyCondition; // 하늘상태 (맑음, 구름많음, 흐림)
    private String windSpeed; // 풍속 (m/s)
    private String region; // 지역명
    
    // 농업 작업 관련 편의 메서드들
    public boolean isHighTemperature() {
        try {
            return Double.parseDouble(temperature) > 30;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public boolean isLowTemperature() {
        try {
            return Double.parseDouble(temperature) < 5;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public boolean isHighHumidity() {
        try {
            return Integer.parseInt(humidity) > 80;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public boolean isRainExpected() {
        return rainProbability != null && rainProbability > 60;
    }
    
    public boolean isGoodForFarmWork() {
        return !isHighTemperature() && !isLowTemperature() && 
               !isHighHumidity() && !isRainExpected();
    }
    
    public String getFormattedSummary() {
        return String.format("기온: %s°C, 습도: %s%%, 강수확률: %d%%, 하늘: %s", 
                temperature, humidity, rainProbability, skyCondition);
    }
}
