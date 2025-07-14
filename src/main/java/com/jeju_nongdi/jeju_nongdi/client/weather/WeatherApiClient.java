package com.jeju_nongdi.jeju_nongdi.client.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherApiClient {
    
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    @Value("${external.api.weather.url:http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0}")
    private String weatherApiUrl;
    
    @Value("${external.api.weather.service-key:}")
    private String serviceKey;
    
    private static final String JEJU_NX = "52"; // 제주시 격자 X
    private static final String JEJU_NY = "38"; // 제주시 격자 Y
    
    /**
     * 제주 지역 단기예보 조회
     */
    public Mono<WeatherInfo> getJejuWeatherForecast() {
        return getWeatherForecast(JEJU_NX, JEJU_NY);
    }
    
    /**
     * 지역별 단기예보 조회
     */
    public Mono<WeatherInfo> getWeatherForecast(String nx, String ny) {
        String baseDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = "0500"; // 기상청 발표 시간
        
        WebClient webClient = webClientBuilder
                .baseUrl(weatherApiUrl)
                .build();
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/getVilageFcst")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("numOfRows", "100")
                        .queryParam("pageNo", "1")
                        .queryParam("base_date", baseDate)
                        .queryParam("base_time", baseTime)
                        .queryParam("nx", nx)
                        .queryParam("ny", ny)
                        .queryParam("dataType", "JSON")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseWeatherResponse)
                .doOnError(error -> log.error("날씨 API 호출 실패: {}", error.getMessage()))
                .onErrorReturn(createErrorWeatherInfo());
    }
    
    /**
     * 현재 날씨 요약 정보 조회 (더미 구현)
     */
    public Mono<String> getCurrentWeatherSummary() {
        return getJejuWeatherForecast()
                .map(weather -> String.format("현재 제주 날씨: %s, 기온 %s°C, 습도 %s%%", 
                        weather.getSkyCondition(), 
                        weather.getTemperature(),
                        weather.getHumidity()));
    }
    
    /**
     * 농업 작업 적합성 판단 (더미 구현)
     */
    public Mono<String> getFarmWorkRecommendation() {
        return getJejuWeatherForecast()
                .map(weather -> {
                    double temp = Double.parseDouble(weather.getTemperature());
                    int humidity = Integer.parseInt(weather.getHumidity());
                    
                    if (temp > 30) {
                        return "고온 주의: 오전 7시 이전 또는 오후 6시 이후 작업 권장";
                    } else if (temp < 5) {
                        return "저온 주의: 실내 작업 또는 방한 대비 필수";
                    } else if (humidity > 80) {
                        return "고습 주의: 통풍이 잘 되는 곳에서 작업";
                    } else if (weather.getRainProbability() > 60) {
                        return "강수 예상: 실내 작업 권장";
                    } else {
                        return "농업 작업에 적합한 날씨입니다";
                    }
                });
    }
    
    private WeatherInfo parseWeatherResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("response").path("body").path("items").path("item");
            
            WeatherInfo.WeatherInfoBuilder builder = WeatherInfo.builder();
            
            if (items.isArray()) {
                for (JsonNode item : items) {
                    String category = item.path("category").asText();
                    String fcstValue = item.path("fcstValue").asText();
                    
                    switch (category) {
                        case "TMP" -> builder.temperature(fcstValue); // 기온
                        case "REH" -> builder.humidity(fcstValue); // 습도
                        case "POP" -> builder.rainProbability(Integer.parseInt(fcstValue)); // 강수확률
                        case "SKY" -> builder.skyCondition(parseSkyCondition(fcstValue)); // 하늘상태
                        case "WSD" -> builder.windSpeed(fcstValue); // 풍속
                    }
                }
            }
            
            return builder.build();
            
        } catch (Exception e) {
            log.error("날씨 데이터 파싱 실패: {}", e.getMessage());
            return createErrorWeatherInfo();
        }
    }
    
    private String parseSkyCondition(String skyCode) {
        return switch (skyCode) {
            case "1" -> "맑음";
            case "3" -> "구름많음";
            case "4" -> "흐림";
            default -> "알 수 없음";
        };
    }
    
    private WeatherInfo createErrorWeatherInfo() {
        return WeatherInfo.builder()
                .temperature("25")
                .humidity("60")
                .rainProbability(20)
                .skyCondition("알 수 없음")
                .windSpeed("2.0")
                .build();
    }
}
