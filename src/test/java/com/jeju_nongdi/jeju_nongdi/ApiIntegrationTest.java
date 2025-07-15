package com.jeju_nongdi.jeju_nongdi;

import com.jeju_nongdi.jeju_nongdi.client.price.PriceApiClient;
import com.jeju_nongdi.jeju_nongdi.client.price.PriceInfo;
import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherApiClient;
import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherInfo;
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
        System.out.println("=== 기상청 API 연동 테스트 시작 ===");
        
        WeatherInfo weather = weatherApiClient.getJejuWeatherForecast().block();
        
        assertThat(weather).isNotNull();
        assertThat(weather.getTemperature()).isNotNull();
        assertThat(weather.getHumidity()).isNotNull();
        
        System.out.println("기상청 API 테스트 결과: " + weather.getFormattedSummary());
        System.out.println("=== 기상청 API 연동 테스트 완료 ===");
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
