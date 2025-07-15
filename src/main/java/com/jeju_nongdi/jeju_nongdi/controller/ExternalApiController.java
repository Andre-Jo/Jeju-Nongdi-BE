package com.jeju_nongdi.jeju_nongdi.controller;

import com.jeju_nongdi.jeju_nongdi.client.ai.OpenAiClient;
import com.jeju_nongdi.jeju_nongdi.client.price.PriceApiClient;
import com.jeju_nongdi.jeju_nongdi.client.price.PriceInfo;
import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherApiClient;
import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherInfo;
import com.jeju_nongdi.jeju_nongdi.service.UserPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/external")
@RequiredArgsConstructor
@Tag(name = "외부 API 테스트", description = "외부 API 연동 테스트 및 확인용 엔드포인트")
public class ExternalApiController {
    
    private final WeatherApiClient weatherApiClient;
    private final PriceApiClient priceApiClient;
    private final OpenAiClient openAiClient;
    private final UserPreferenceService userPreferenceService;
    
    @GetMapping("/weather/jeju")
    @Operation(summary = "제주 날씨 정보 조회", description = "기상청 API를 통해 제주 지역 날씨 정보를 조회합니다.")
    public Mono<ResponseEntity<WeatherInfo>> getJejuWeather() {
        return weatherApiClient.getJejuWeatherForecast()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/weather/summary")
    @Operation(summary = "날씨 요약 정보", description = "현재 날씨를 요약한 문자열을 반환합니다.")
    public Mono<ResponseEntity<String>> getWeatherSummary() {
        return weatherApiClient.getCurrentWeatherSummary()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/weather/farm-work")
    @Operation(summary = "농업 작업 적합성", description = "현재 날씨 기반 농업 작업 적합성을 판단합니다.")
    public Mono<ResponseEntity<String>> getFarmWorkRecommendation() {
        return weatherApiClient.getFarmWorkRecommendation()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/price/{cropName}")
    @Operation(summary = "작물 가격 정보", description = "특정 작물의 현재 가격 정보를 조회합니다.")
    public Mono<ResponseEntity<PriceInfo>> getCropPrice(
            @Parameter(description = "작물명") @PathVariable String cropName) {
        return priceApiClient.getCropPrice(cropName)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/price/jeju-specialties")
    @Operation(summary = "제주 특산물 가격", description = "제주 특산물들의 가격 정보를 조회합니다.")
    public Mono<ResponseEntity<List<PriceInfo>>> getJejuSpecialtyPrices() {
        return priceApiClient.getJejuSpecialtyPrices()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/price/trend/{cropName}")
    @Operation(summary = "가격 동향 분석", description = "특정 작물의 가격 동향을 분석합니다.")
    public Mono<ResponseEntity<String>> getPriceTrend(
            @Parameter(description = "작물명") @PathVariable String cropName) {
        return priceApiClient.getPriceTrendAnalysis(cropName)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @PostMapping("/ai/advice")
    @Operation(summary = "AI 농업 조언", description = "OpenAI를 활용한 농업 조언을 생성합니다.")
    public ResponseEntity<String> getAiAdvice(
            @Parameter(description = "질문") @RequestParam String question,
            @Parameter(description = "사용자 ID") @RequestParam(required = false) Long userId) {
        
        var userPreference = userId != null ? userPreferenceService.getUserPreference(userId) : null;
        String advice = openAiClient.generateFarmingAdvice(question, userPreference);
        
        return ResponseEntity.ok(advice);
    }
    
    @GetMapping("/ai/weather-advice/{userId}")
    @Operation(summary = "날씨 기반 AI 조언", description = "현재 날씨와 사용자 정보를 바탕으로 AI 조언을 생성합니다.")
    public Mono<ResponseEntity<String>> getWeatherBasedAiAdvice(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        
        var userPreference = userPreferenceService.getUserPreference(userId);
        
        return weatherApiClient.getJejuWeatherForecast()
                .map(weather -> {
                    String advice = openAiClient.generateWeatherBasedAdvice(weather, userPreference);
                    return ResponseEntity.ok(advice);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/ai/crop-guide/{cropName}")
    @Operation(summary = "작물 재배 가이드", description = "특정 작물의 AI 재배 가이드를 생성합니다.")
    public ResponseEntity<String> getCropGuide(
            @Parameter(description = "작물명") @PathVariable String cropName) {
        
        String season = getCurrentSeason();
        String guide = openAiClient.generateCropGuide(cropName, season);
        
        return ResponseEntity.ok(guide);
    }
    
    @PostMapping("/ai/profit-analysis/{userId}")
    @Operation(summary = "수익성 분석", description = "사용자의 작물들에 대한 AI 수익성 분석을 제공합니다.")
    public Mono<ResponseEntity<String>> getProfitAnalysis(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        
        var userPreference = userPreferenceService.getUserPreference(userId);
        
        return priceApiClient.getJejuSpecialtyPrices()
                .map(priceInfos -> {
                    String analysis = openAiClient.generateProfitAnalysis(priceInfos, userPreference);
                    return ResponseEntity.ok(analysis);
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/test/all/{userId}")
    @Operation(summary = "전체 API 테스트", description = "모든 외부 API를 한번에 테스트합니다.")
    public Mono<ResponseEntity<TestResult>> testAllApis(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        
        var userPreference = userPreferenceService.getUserPreference(userId);
        
        return weatherApiClient.getJejuWeatherForecast()
                .zipWith(priceApiClient.getJejuSpecialtyPrices())
                .map(tuple -> {
                    WeatherInfo weather = tuple.getT1();
                    List<PriceInfo> prices = tuple.getT2();
                    
                    TestResult result = TestResult.builder()
                            .weatherApiStatus("SUCCESS")
                            .weatherData(weather.getFormattedSummary())
                            .priceApiStatus("SUCCESS")
                            .priceDataCount(prices.size())
                            .aiApiStatus("SUCCESS")
                            .aiAdvice(openAiClient.generateWeatherBasedAdvice(weather, userPreference))
                            .build();
                    
                    return ResponseEntity.ok(result);
                })
                .onErrorReturn(ResponseEntity.status(500).body(
                        TestResult.builder()
                                .weatherApiStatus("FAILED")
                                .priceApiStatus("FAILED")
                                .aiApiStatus("FAILED")
                                .build()
                ));
    }
    
    private String getCurrentSeason() {
        int month = java.time.LocalDate.now().getMonthValue();
        return switch (month) {
            case 3, 4, 5 -> "봄";
            case 6, 7, 8 -> "여름";
            case 9, 10, 11 -> "가을";
            default -> "겨울";
        };
    }
    
    // 테스트 결과 DTO
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TestResult {
        private String weatherApiStatus;
        private String weatherData;
        private String priceApiStatus;
        private Integer priceDataCount;
        private String aiApiStatus;
        private String aiAdvice;
    }
}
