package com.jeju_nongdi.jeju_nongdi.controller;

import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherApiClient;
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
     * 4일 기상 예보 (내일부터) - 메인 API
     * GET /api/weather/4days?lat=33.4996&lon=126.5312
     */
    @GetMapping("/4days")
    public Mono<ResponseEntity<WeatherApiClient.WeatherForecast4Days>> get4DaysWeather(
            @RequestParam(required = false, defaultValue = "33.4996") double lat,
            @RequestParam(required = false, defaultValue = "126.5312") double lon) {

        log.info("4일 날씨 예보 요청 (내일부터) - 위치: ({}, {})", lat, lon);

        return weatherApiClient.get4DaysForecast(lat, lon)
                .map(forecast -> {
                    log.info("4일 날씨 예보 완료: {}일 데이터, {}개 경보",
                            forecast.getDailyForecasts().size(),
                            forecast.getAlerts().size());
                    return ResponseEntity.ok(forecast);
                })
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }

}
