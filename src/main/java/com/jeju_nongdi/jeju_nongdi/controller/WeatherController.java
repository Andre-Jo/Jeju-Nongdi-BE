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
