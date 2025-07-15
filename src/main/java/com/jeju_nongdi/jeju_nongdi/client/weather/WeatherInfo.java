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
    private Integer maxTemperature; // 최고기온 (°C)
    private Integer minTemperature; // 최저기온 (°C)
    private String humidity; // 습도 (%)
    private Integer rainProbability; // 강수확률 (%)
    private String skyCondition; // 하늘상태 (맑음, 구름많음, 흐림)
    private String windSpeed; // 풍속 (m/s)
    private String region; // 지역명
    
    // 농업 작업 관련 편의 메서드들
    public boolean isHighTemperature() {
        if (maxTemperature != null) {
            return maxTemperature > 30;
        }
        try {
            return Double.parseDouble(temperature) > 30;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public boolean isLowTemperature() {
        if (minTemperature != null) {
            return minTemperature < 5;
        }
        try {
            return Double.parseDouble(temperature) < 5;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public Integer getMaxTemperature() {
        if (maxTemperature != null) {
            return maxTemperature;
        }
        try {
            return (int) Double.parseDouble(temperature);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Integer getMinTemperature() {
        if (minTemperature != null) {
            return minTemperature;
        }
        try {
            return (int) Double.parseDouble(temperature);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public Double getWindSpeed() {
        try {
            return Double.parseDouble(windSpeed);
        } catch (NumberFormatException e) {
            return null;
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
        if (maxTemperature != null && minTemperature != null) {
            return String.format("최고: %d°C, 최저: %d°C, 습도: %s%%, 강수확률: %d%%, %s", 
                    maxTemperature, minTemperature, humidity, rainProbability, skyCondition);
        }
        return String.format("기온: %s°C, 습도: %s%%, 강수확률: %d%%, 하늘: %s", 
                temperature, humidity, rainProbability, skyCondition);
    }
}
