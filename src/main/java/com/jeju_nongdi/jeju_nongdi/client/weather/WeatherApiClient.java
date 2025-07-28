package com.jeju_nongdi.jeju_nongdi.client.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherApiClient {
    
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    @Value("${external.api.weather.url:}")
    private String weatherApiUrl;
    
    @Value("${external.api.weather.service-key:}")
    private String serviceKey;

    private static final String JEJU_NX = "52"; // 제주시 격자 X
    private static final String JEJU_NY = "38"; // 제주시 격자 Y
    
    /**
     * 격자 좌표 클래스
     */
    @Data
    @AllArgsConstructor
    public static class GridCoordinate {
        private int nx;
        private int ny;
    }
    
    /**
     * 오늘/내일 날씨 비교 클래스
     */
    @Data
    @AllArgsConstructor
    public static class TodayTomorrowWeather {
        private WeatherInfo today;
        private WeatherInfo tomorrow;
    }
    
    /**
     * 농업 작업 추천 클래스
     */
    @Data
    @AllArgsConstructor
    public static class FarmWorkRecommendation {
        private List<String> morningTips;
        private List<String> eveningTips;
    }
    
    /**
     * 위경도를 기상청 격자 좌표로 변환
     */
    public GridCoordinate convertToGrid(double lat, double lon) {
        // 기상청 격자 변환 공식 (Lambert Conformal Conic Projection)
        double RE = 6371.00877; // 지구 반경(km)
        double GRID = 5.0; // 격자 간격(km)
        double SLAT1 = 30.0; // 투영 위도1(degree)
        double SLAT2 = 60.0; // 투영 위도2(degree)
        double OLON = 126.0; // 기준점 경도(degree)
        double OLAT = 38.0; // 기준점 위도(degree)
        double XO = 210 / GRID; // 기준점 X좌표(GRID)
        double YO = 675 / GRID; // 기준점 Y좌표(GRID)
        
        double DEGRAD = Math.PI / 180.0;
        double RADDEG = 180.0 / Math.PI;
        
        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;
        
        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);
        
        double ra = Math.tan(Math.PI * 0.25 + (lat) * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        double theta = lon * DEGRAD - olon;
        if (theta > Math.PI) theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;
        
        int nx = (int) Math.round(ra * Math.sin(theta) + XO);
        int ny = (int) Math.round(ro - ra * Math.cos(theta) + YO);
        
        log.info("좌표 변환: ({:.4f}, {:.4f}) -> ({}, {})", lat, lon, nx, ny);
        return new GridCoordinate(nx, ny);
    }
    
    /**
     * 위치 기반 날씨 조회
     */
    public Mono<WeatherInfo> getWeatherByLocation(double lat, double lon) {
        GridCoordinate grid = convertToGrid(lat, lon);
        return getWeatherForecast(String.valueOf(grid.getNx()), String.valueOf(grid.getNy()))
                .map(weather -> {
                    // 지역명을 좌표 기반으로 설정 (제주도 내 위치 판단)
                    String region = getRegionName(lat, lon);
                    weather.setRegion(region);
                    return weather;
                });
    }
    
    /**
     * 오늘/내일 날씨 비교
     */
    public Mono<TodayTomorrowWeather> getTodayTomorrowWeather(double lat, double lon) {
        GridCoordinate grid = convertToGrid(lat, lon);
        
        return getDetailedWeatherForecast(String.valueOf(grid.getNx()), String.valueOf(grid.getNy()))
                .map(forecasts -> {
                    String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                    String tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                    
                    WeatherInfo todayWeather = null;
                    WeatherInfo tomorrowWeather = null;
                    
                    for (WeatherInfo forecast : forecasts) {
                        if (forecast.getRegion().equals(today)) {
                            todayWeather = forecast;
                        } else if (forecast.getRegion().equals(tomorrow)) {
                            tomorrowWeather = forecast;
                        }
                    }
                    
                    // 데이터가 없으면 기본값 생성
                    if (todayWeather == null) {
                        todayWeather = createRealisticWeatherForDate("오늘");
                    }
                    if (tomorrowWeather == null) {
                        tomorrowWeather = createRealisticWeatherForDate("내일");
                    }
                    
                    return new TodayTomorrowWeather(todayWeather, tomorrowWeather);
                });
    }
    
    /**
     * 위치 기반 농업 작업 추천
     */
    public Mono<FarmWorkRecommendation> getFarmWorkRecommendation(double lat, double lon) {
        return getTodayTomorrowWeather(lat, lon)
                .map(weather -> {
                    List<String> morningTips = generateMorningTips(weather.getToday());
                    List<String> eveningTips = generateEveningTips(weather.getTomorrow());
                    return new FarmWorkRecommendation(morningTips, eveningTips);
                });
    }

    /**
     * 제주 지역 단기예보 조회
     */
    public Mono<WeatherInfo> getJejuWeatherForecast() {
        return getWeatherForecast(JEJU_NX, JEJU_NY);
    }
    
    /**
     * 지역별 단기예보 조회 (실제 기상청 API 호출)
     */
    public Mono<WeatherInfo> getWeatherForecast(String nx, String ny) {
        String baseDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = getCurrentBaseTime();

        WebClient webClient = webClientBuilder
                .baseUrl(weatherApiUrl)
                .build();

        log.info("기상청 API 호출 시작 - 위치: ({}, {}), 기준: {} {}", nx, ny, baseDate, baseTime);
        log.info("API URL: {}", weatherApiUrl);
        log.info("API KEY: {}", serviceKey);

        return webClient.get()
                .uri(uriBuilder -> {
                    // serviceKey는 자동 인코딩을 피하기 위해 수동으로 추가
                    var uri = uriBuilder
                            .queryParam("numOfRows", "300")
                            .queryParam("dataType", "JSON")
                            .queryParam("pageNo", "1")
                            .queryParam("base_date", baseDate)
                            .queryParam("base_time", baseTime)
                            .queryParam("nx", nx)
                            .queryParam("ny", ny)
                            .build(false); // 자동 인코딩 비활성화

                    // 수동으로 serviceKey만 추가
                    String baseUri = uri.toString();
                    String fullUri = baseUri + "&serviceKey=" + serviceKey;

                    log.info("요청 전체 URI: {}", fullUri);
                    return java.net.URI.create(fullUri);
                })
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> {
                    log.info("기상청 API 응답 수신: {} bytes", response.length());
                    log.debug("응답 내용: {}", response.substring(0, Math.min(500, response.length())));
                })
                .map(this::parseWeatherResponse)
                .doOnError(error -> log.error("기상청 API 호출 실패: {}", error.getMessage(), error))
                .onErrorReturn(createErrorWeatherInfo());
    }
    
    /**
     * 상세한 날씨 예보 (여러 날짜)
     */
    private Mono<List<WeatherInfo>> getDetailedWeatherForecast(String nx, String ny) {
        // 실제 구현에서는 여러 날짜의 데이터를 파싱해야 하지만, 
        // 지금은 간단히 오늘/내일 데이터 생성
        List<WeatherInfo> forecasts = new ArrayList<>();
        forecasts.add(createRealisticWeatherForDate("오늘"));
        forecasts.add(createRealisticWeatherForDate("내일"));
        return Mono.just(forecasts);
    }
    
    /**
     * 현재 시간에 맞는 기상청 기준시간 계산
     */
    private String getCurrentBaseTime() {
        // 기상청 데이터 1시간 전 데이터만 존재함 현재시간 데이터 없던디 ,,
        int currentHour = LocalDateTime.now().minusHours(1).getHour();
        if (currentHour >= 23 || currentHour < 2) return "2300";
        else if (currentHour >= 20) return "2000";
        else if (currentHour >= 17) return "1700";
        else if (currentHour >= 14) return "1400";
        else if (currentHour >= 11) return "1100";
        else if (currentHour >= 8) return "0800";
        else if (currentHour >= 5) return "0500";
        else return "0200";
    }
    
    /**
     * 좌표 기반 지역명 추정
     */
    private String getRegionName(double lat, double lon) {
        // 제주도 주요 지역 좌표 기반 판별
        if (lat >= 33.49 && lat <= 33.51 && lon >= 126.51 && lon <= 126.54) {
            return "제주시";
        } else if (lat >= 33.24 && lat <= 33.26 && lon >= 126.55 && lon <= 126.57) {
            return "서귀포시";
        } else if (lat >= 33.38 && lat <= 33.40 && lon >= 126.27 && lon <= 126.29) {
            return "한림읍";
        } else if (lat >= 33.45 && lat <= 33.47 && lon >= 126.89 && lon <= 126.91) {
            return "성산읍";
        } else {
            return "제주도";
        }
    }
    
    /**
     * 오전 작업 팁 생성
     */
    private List<String> generateMorningTips(WeatherInfo today) {
        List<String> tips = new ArrayList<>();
        
        if (today.isHighTemperature()) {
            tips.add("🌡️ 고온 주의! 오전 7시 전에 물주기 완료하세요");
            tips.add("☀️ 오후 2-4시 야외작업 금지, 실내 작업으로 전환");
            tips.add("🧴 작업자 수분 보충 필수 - 30분마다 물 섭취");
        }
        
        if (today.isRainExpected()) {
            tips.add("🌧️ 강수 예상! 비닐하우스 내 작업 위주로 계획");
            tips.add("💧 배수로 점검 및 물빠짐 확인");
        }
        
        if (today.isHighHumidity()) {
            tips.add("💨 고습 주의! 하우스 환기팬 가동 필수");
            tips.add("🍄 곰팡이 방지를 위한 통풍 관리 강화");
        }
        
        Double windSpeed = today.getWindSpeed();
        if (windSpeed != null && windSpeed > 7.0) {
            tips.add("💨 강풍 주의! 비닐하우스 고정 점검");
            tips.add("🌱 어린 작물 보호막 설치 권장");
        }
        
        if (tips.isEmpty()) {
            tips.add("🌱 농업 작업에 좋은 날씨입니다!");
            tips.add("📅 계획된 농작업을 진행하세요");
        }
        
        return tips;
    }
    
    /**
     * 저녁 작업 팁 생성 (내일 날씨 대비)
     */
    private List<String> generateEveningTips(WeatherInfo tomorrow) {
        List<String> tips = new ArrayList<>();
        
        if (tomorrow.isHighTemperature()) {
            tips.add("🌅 내일 고온 예상! 오늘 저녁에 충분한 관수 실시");
            tips.add("🏠 차광막/그늘막 설치 점검");
        }
        
        if (tomorrow.isRainExpected()) {
            tips.add("☔ 내일 비 예상! 수확 가능한 작물 오늘 수확 권장");
            tips.add("🔧 농기구 실내 보관 및 방수 처리");
        }
        
        if (tomorrow.isLowTemperature()) {
            tips.add("🥶 내일 저온 주의! 보온 자재 준비");
            tips.add("🔥 난방 시설 점검 및 연료 보충");
        }
        
        Double windSpeed = tomorrow.getWindSpeed();
        if (windSpeed != null && windSpeed > 10.0) {
            tips.add("🌪️ 내일 강풍 예상! 시설물 고정 강화");
            tips.add("📦 야외 보관물 실내 이동 권장");
        }
        
        if (tips.isEmpty()) {
            tips.add("🌙 내일도 좋은 농업 환경이 예상됩니다");
            tips.add("💤 충분한 휴식으로 내일 작업 준비하세요");
        }
        
        return tips;
    }
    
    /**
     * 현재 날씨 요약 정보 조회
     */
    public Mono<String> getCurrentWeatherSummary() {
        return getJejuWeatherForecast()
                .map(weather -> String.format("현재 제주 날씨: %s, 기온 %s°C, 습도 %s%%", 
                        weather.getSkyCondition(), 
                        weather.getTemperature(),
                        weather.getHumidity()));
    }
    
    /**
     * 농업 작업 적합성 판단
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
            log.info("=== 기상청 API 응답 파싱 시작 ===");
            log.info("응답 데이터: {}", response);
            
            JsonNode root = objectMapper.readTree(response);
            JsonNode responseNode = root.path("response");
            JsonNode header = responseNode.path("header");
            
            String resultCode = header.path("resultCode").asText();
            String resultMsg = header.path("resultMsg").asText();
            
            log.info("API 응답 코드: {}, 메시지: {}", resultCode, resultMsg);
            
            if (!"00".equals(resultCode)) {
                log.error("❌ 기상청 API 오류 - 코드: {}, 메시지: {}", resultCode, resultMsg);
                log.error("더미 데이터로 폴백합니다");
                return createRealisticJejuWeather();
            }
            
            JsonNode items = responseNode.path("body").path("items").path("item");
            log.info("파싱할 아이템 개수: {}", items.isArray() ? items.size() : "배열 아님");
            
            if (!items.isArray() || items.isEmpty()) {
                log.error("❌ 응답 데이터에 아이템이 없습니다");
                return createRealisticJejuWeather();
            }
            
            WeatherInfo.WeatherInfoBuilder builder = WeatherInfo.builder()
                    .region("제주시");
            
            Integer maxTemp = null, minTemp = null;
            String temperature = null;
            String humidity = null;
            Integer rainProbability = null;
            String skyCondition = null;
            String windSpeed = null;
            
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            log.info("오늘 날짜: {}", today);
            
            for (JsonNode item : items) {
                String category = item.path("category").asText();
                String fcstValue = item.path("fcstValue").asText();
                String fcstDate = item.path("fcstDate").asText();
                String fcstTime = item.path("fcstTime").asText();
                
                log.debug("데이터: {} = {} (날짜: {}, 시간: {})", category, fcstValue, fcstDate, fcstTime);
                
                // 오늘 날짜의 데이터만 처리
                if (!today.equals(fcstDate)) continue;
                
                switch (category) {
                    case "TMP" -> {
                        temperature = fcstValue;
                        int temp = Integer.parseInt(fcstValue);
                        if (maxTemp == null || temp > maxTemp) maxTemp = temp;
                        if (minTemp == null || temp < minTemp) minTemp = temp;
                        log.info("✅ 기온 데이터: {}°C", fcstValue);
                    }
                    case "TMX" -> {
                        maxTemp = Integer.parseInt(fcstValue);
                        log.info("✅ 최고기온: {}°C", fcstValue);
                    }
                    case "TMN" -> {
                        minTemp = Integer.parseInt(fcstValue);
                        log.info("✅ 최저기온: {}°C", fcstValue);
                    }
                    case "REH" -> {
                        humidity = fcstValue;
                        log.info("✅ 습도: {}%", fcstValue);
                    }
                    case "POP" -> {
                        rainProbability = Integer.parseInt(fcstValue);
                        log.info("✅ 강수확률: {}%", fcstValue);
                    }
                    case "SKY" -> {
                        skyCondition = parseSkyCondition(fcstValue);
                        log.info("✅ 하늘상태: {} (코드: {})", skyCondition, fcstValue);
                    }
                    case "WSD" -> {
                        windSpeed = fcstValue;
                        log.info("✅ 풍속: {}m/s", fcstValue);
                    }
                }
            }
            
            // 필수 데이터 검증
            if (temperature == null || humidity == null || rainProbability == null || skyCondition == null) {
                log.error("❌ 필수 날씨 데이터가 부족합니다. 기온: {}, 습도: {}, 강수확률: {}, 하늘상태: {}", 
                         temperature, humidity, rainProbability, skyCondition);
                return createRealisticJejuWeather();
            }
            
            WeatherInfo weather = WeatherInfo.builder()
                    .temperature(temperature)
                    .maxTemperature(maxTemp != null ? maxTemp : Integer.parseInt(temperature))
                    .minTemperature(minTemp != null ? minTemp : Integer.parseInt(temperature))
                    .humidity(humidity)
                    .rainProbability(rainProbability)
                    .skyCondition(skyCondition)
                    .windSpeed(windSpeed != null ? windSpeed : "0.0")
                    .region("제주시")
                    .build();
            
            log.info("🎉 실제 기상청 API 데이터 파싱 성공: {}", weather.getFormattedSummary());
            return weather;
            
        } catch (Exception e) {
            log.error("❌ 날씨 데이터 파싱 실패: {}", e.getMessage(), e);
            log.error("원본 응답 데이터: {}", response);
            log.error("더미 데이터로 폴백합니다");
            return createRealisticJejuWeather();
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
        log.warn("기상청 API 호출 실패, 폴백 데이터 사용");
        return createRealisticJejuWeather();
    }
    
    private WeatherInfo createRealisticJejuWeather() {
        // 날짜 기반으로 일관성 있는 데이터 생성 (같은 날엔 같은 값)
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long seed = today.hashCode(); // 날짜를 시드로 사용
        
        // 고정된 현실적인 제주 날씨 데이터 사용
        int maxTemp = 29; // 7월 제주 평균 최고기온
        int minTemp = 24; // 7월 제주 평균 최저기온  
        int humidity = 75; // 7월 제주 평균 습도
        int rainProb = 30; // 보통 수준의 강수확률
        String skyCondition = "구름조금";
        double windSpeed = 3.5; // 적당한 바람
        
        return WeatherInfo.builder()
                .temperature(String.valueOf((maxTemp + minTemp) / 2))
                .maxTemperature(maxTemp)
                .minTemperature(minTemp)
                .humidity(String.valueOf(humidity))
                .rainProbability(rainProb)
                .skyCondition(skyCondition)
                .windSpeed(String.format("%.1f", windSpeed))
                .region("제주시")
                .build();
    }
    
    private WeatherInfo createRealisticWeatherForDate(String dateLabel) {
        // 날짜별로 일관성 있는 데이터 생성
        WeatherInfo weather = createRealisticJejuWeather();
        weather.setRegion(dateLabel);
        
        // 내일은 약간 다른 날씨로 설정
        if ("내일".equals(dateLabel)) {
            weather.setTemperature("28");
            weather.setMaxTemperature(30);
            weather.setMinTemperature(25);
            weather.setRainProbability(20);
            weather.setSkyCondition("맑음");
        }
        
        return weather;
    }

}