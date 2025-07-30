package com.jeju_nongdi.jeju_nongdi.controller;

import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherApiClient;
import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Slf4j
public class WeatherController {
    
    private final WeatherApiClient weatherApiClient;
    
    /**
     * 현재 날씨 조회
     * GET /api/weather/current?lat=33.4996&lon=126.5312
     */
    @GetMapping("/current")
    public Mono<ResponseEntity<WeatherInfo>> getCurrentWeather(
            @RequestParam(required = false, defaultValue = "33.4996") double lat,
            @RequestParam(required = false, defaultValue = "126.5312") double lon) {
        
        log.info("현재 날씨 조회 요청 - 위치: ({}, {})", lat, lon);
        
        return weatherApiClient.getWeatherByLocation(lat, lon)
                .map(weather -> {
                    log.info("현재 날씨 조회 완료: {}", weather.getFormattedSummary());
                    return ResponseEntity.ok(weather);
                })
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }
    
    /**
     * 오늘/내일 날씨 비교
     * GET /api/weather/today-tomorrow?lat=33.4996&lon=126.5312
     */
    @GetMapping("/today-tomorrow")
    public Mono<ResponseEntity<WeatherApiClient.TodayTomorrowWeather>> getTodayTomorrowWeather(
            @RequestParam(required = false, defaultValue = "33.4996") double lat,
            @RequestParam(required = false, defaultValue = "126.5312") double lon) {
        
        log.info("오늘/내일 날씨 비교 요청 - 위치: ({}, {})", lat, lon);
        
        return weatherApiClient.getTodayTomorrowWeather(lat, lon)
                .map(weather -> {
                    log.info("오늘/내일 날씨 조회 완료 - 오늘: {}, 내일: {}", 
                            weather.getToday().getFormattedSummary(),
                            weather.getTomorrow().getFormattedSummary());
                    return ResponseEntity.ok(weather);
                })
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }
    
    /**
     * 농업 작업 추천
     * GET /api/weather/recommendation?lat=33.4996&lon=126.5312
     */
    @GetMapping("/recommendation")
    public Mono<ResponseEntity<WeatherApiClient.FarmWorkRecommendation>> getFarmWorkRecommendation(
            @RequestParam(required = false, defaultValue = "33.4996") double lat,
            @RequestParam(required = false, defaultValue = "126.5312") double lon) {
        
        log.info("농업 작업 추천 요청 - 위치: ({}, {})", lat, lon);
        
        return weatherApiClient.getFarmWorkRecommendation(lat, lon)
                .map(recommendation -> {
                    log.info("농업 작업 추천 완료 - 오전 팁: {}개, 저녁 팁: {}개", 
                            recommendation.getMorningTips().size(),
                            recommendation.getEveningTips().size());
                    return ResponseEntity.ok(recommendation);
                })
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }
    
    /**
     * 좌표 변환 테스트
     * GET /api/weather/grid?lat=33.4996&lon=126.5312
     */
    @GetMapping("/grid")
    public ResponseEntity<WeatherApiClient.GridCoordinate> convertToGrid(
            @RequestParam(required = false, defaultValue = "33.4996") double lat,
            @RequestParam(required = false, defaultValue = "126.5312") double lon) {
        
        log.info("좌표 변환 요청 - 위치: ({}, {})", lat, lon);
        
        WeatherApiClient.GridCoordinate grid = weatherApiClient.convertToGrid(lat, lon);
        
        log.info("좌표 변환 완료: ({}, {}) -> ({}, {})", lat, lon, grid.getNx(), grid.getNy());
        
        return ResponseEntity.ok(grid);
    }
    
    /**
     * 제주 기본 날씨 (API 키 없이 테스트용)
     * GET /api/weather/jeju
     */
    @GetMapping("/jeju")
    public Mono<ResponseEntity<WeatherInfo>> getJejuWeather() {
        
        log.info("제주 기본 날씨 조회 요청");
        
        return weatherApiClient.getJejuWeatherForecast()
                .map(weather -> {
                    log.info("제주 날씨 조회 완료: {}", weather.getFormattedSummary());
                    return ResponseEntity.ok(weather);
                })
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }
    
    /**
     * 날씨 정보 요약 (간단 버전)
     * GET /api/weather/summary?lat=33.4996&lon=126.5312
     */
    @GetMapping("/summary")
    public Mono<ResponseEntity<String>> getWeatherSummary(
            @RequestParam(required = false, defaultValue = "33.4996") double lat,
            @RequestParam(required = false, defaultValue = "126.5312") double lon) {
        
        log.info("날씨 요약 조회 요청 - 위치: ({}, {})", lat, lon);
        
        return weatherApiClient.getWeatherByLocation(lat, lon)
                .map(weather -> {
                    String summary = String.format(
                            "📍 %s 날씨\n🌡️ 기온: %s°C (최고 %d°C, 최저 %d°C)\n💧 습도: %s%%\n☔ 강수확률: %d%%\n☀️ 하늘: %s\n💨 풍속: %sm/s",
                            weather.getRegion(),
                            weather.getTemperature(),
                            weather.getMaxTemperature(),
                            weather.getMinTemperature(),
                            weather.getHumidity(),
                            weather.getRainProbability(),
                            weather.getSkyCondition(),
                            weather.getWindSpeed()
                    );
                    log.info("날씨 요약 완료");
                    return ResponseEntity.ok(summary);
                })
                .onErrorReturn(ResponseEntity.internalServerError().body("날씨 정보를 가져올 수 없습니다."));
    }
    
    /**
     * API 상태 확인
     * GET /api/weather/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("날씨 API 상태 확인");
        return ResponseEntity.ok("Weather API is running! 🌤️");
    }
    
    /**
     * 5일 기상 예보 (신규 추가)
     * GET /api/weather/5days?lat=33.4996&lon=126.5312
     */
    @GetMapping("/5days")
    public Mono<ResponseEntity<WeatherApiClient.WeatherForecast5Days>> get5DaysWeather(
            @RequestParam(required = false, defaultValue = "33.4996") double lat,
            @RequestParam(required = false, defaultValue = "126.5312") double lon) {
        
        log.info("5일 날씨 예보 요청 - 위치: ({}, {})", lat, lon);
        
        return weatherApiClient.get5DaysForecast(lat, lon)
                .map(forecast -> {
                    log.info("5일 날씨 예보 완료: {}일 데이터, {}개 경보", 
                            forecast.getDailyForecasts().size(),
                            forecast.getAlerts().size());
                    return ResponseEntity.ok(forecast);
                })
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }
}
