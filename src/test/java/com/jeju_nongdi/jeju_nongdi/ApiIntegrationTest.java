package com.jeju_nongdi.jeju_nongdi;

import com.jeju_nongdi.jeju_nongdi.client.price.PriceApiClient;
import com.jeju_nongdi.jeju_nongdi.client.price.PriceInfo;
import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherApiClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ApiIntegrationTest {

    @Autowired
    private WeatherApiClient weatherApiClient;

    @Autowired  
    private PriceApiClient priceApiClient;

    @Test
    public void testWeatherApiIntegration() {
        System.out.println("=== 4일 기상 예보 API 연동 테스트 시작 ===");
        
        var forecast = weatherApiClient.get4DaysForecast(33.4996, 126.5312).block();
        
        assertThat(forecast).isNotNull();
        assertThat(forecast.getDailyForecasts()).isNotEmpty();
        assertThat(forecast.getAlerts()).isNotNull();
        
        System.out.println("4일 예보 테스트 결과: " + forecast.getDailyForecasts().size() + "일 데이터, " + 
                          forecast.getAlerts().size() + "개 경보");
        System.out.println("=== 4일 기상 예보 API 연동 테스트 완료 ===");
    }

    @Test
    public void testAiAgricultureTip() {
        System.out.println("=== AI 농업 팁 API 연동 테스트 시작 ===");
        
        var tip = weatherApiClient.generateAgricultureTip(33.4996, 126.5312).block();
        
        assertThat(tip).isNotNull();
        assertThat(tip.getMainMessage()).isNotNull();
        assertThat(tip.getAlerts()).isNotNull();
        assertThat(tip.getPreparationActions()).isNotNull();
        
        System.out.println("AI 농업 팁 테스트 결과: " + tip.getMainMessage());
        System.out.println("=== AI 농업 팁 API 연동 테스트 완료 ===");
    }

    @Test
    public void testPriceApiIntegration() {
        System.out.println("=== KAMIS API 연동 테스트 시작 ===");
        
        PriceInfo priceInfo = priceApiClient.getCropPrice("감귤").block();
        
        assertThat(priceInfo).isNotNull();
        assertThat(priceInfo.getCropName()).isEqualTo("감귤");
        assertThat(priceInfo.getCurrentPrice()).isNotNull();
        
        System.out.println("KAMIS API 테스트 결과: " + priceInfo.getFormattedSummary());
        System.out.println("=== KAMIS API 연동 테스트 완료 ===");
    }

    @Test
    public void testJejuSpecialtyPrices() {
        System.out.println("=== 제주 특산물 가격 조회 테스트 시작 ===");
        
        var priceList = priceApiClient.getJejuSpecialtyPrices().block();
        
        assertThat(priceList).isNotNull();
        assertThat(priceList).isNotEmpty();
        
        priceList.forEach(price -> {
            System.out.println(String.format("작물: %s, 가격: %s, 변동: %s", 
                    price.getCropName(), 
                    price.getFormattedPrice(), 
                    price.getPriceChangeDescription()));
        });
        
        System.out.println("=== 제주 특산물 가격 조회 테스트 완료 ===");
    }
}
