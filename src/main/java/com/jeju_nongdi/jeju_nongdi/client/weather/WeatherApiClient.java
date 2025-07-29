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
import java.util.*;

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
     * 5일간 기상 데이터 클래스
     */
    @Data
    @AllArgsConstructor
    public static class WeatherForecast5Days {
        private List<DailyWeather> dailyForecasts; // 5일간 일별 예보
        private List<WeatherAlert> alerts; // 위험 기상 알림
    }

    /**
     * 일별 기상 정보 클래스
     */
    @Data
    @AllArgsConstructor
    public static class DailyWeather {
        private String date; // YYYYMMDD
        private String dayLabel; // "오늘", "내일", "3일후" 등
        private Integer maxTemp;
        private Integer minTemp;
        private Integer maxRainProb; // 하루 중 최대 강수확률
        private Integer totalRainfall; // 일 총 강수량 (mm)
        private String skyCondition;
        private Double maxWindSpeed;
    }

    /**
     * 기상 위험 알림 클래스
     */
    @Data
    @AllArgsConstructor
    public static class WeatherAlert {
        private String alertType; // "HEATWAVE", "HEAVY_RAIN", "HIGH_WIND" 등
        private String title; // "3일 후부터 연속 폭염 예상"
        private String description; // 상세 설명
        private String startDate; // 시작일 YYYYMMDD
        private Integer duration; // 지속 일수
        private List<String> actionItems; // 준비 사항들
    }

    /**
     * AI 농업 팁 클래스
     */
    @Data
    @AllArgsConstructor
    public static class AiAgricultureTip {
        private String tipType; // "MORNING", "EVENING"
        private String mainMessage; // 주요 메시지
        private List<WeatherAlert> alerts; // 기상 경보들
        private List<String> todayActions; // 오늘 해야 할 일
        private List<String> preparationActions; // 미리 준비할 일
        private String marketInfo; // 농산물 시세 정보 (추후 추가)
    }
    
    /**
     * 오늘/내일 날씨 비교 클래스 (기존 호환성)
     */
    @Data
    @AllArgsConstructor
    public static class TodayTomorrowWeather {
        private WeatherInfo today;
        private WeatherInfo tomorrow;
    }
    
    /**
     * 농업 작업 추천 클래스 (기존 호환성)
     */
    @Data
    @AllArgsConstructor
    public static class FarmWorkRecommendation {
        private List<String> morningTips;
        private List<String> eveningTips;
    }
    
    /**
     * 5일간 상세 기상 예보 조회 및 분석
     */
    public Mono<WeatherForecast5Days> get5DaysForecast(double lat, double lon) {
        GridCoordinate grid = convertToGrid(lat, lon);
        return get5DaysForecast(String.valueOf(grid.getNx()), String.valueOf(grid.getNy()));
    }
    
    /**
     * AI 농업 팁 생성 (아침용)
     */
    public Mono<AiAgricultureTip> generateMorningTip(double lat, double lon) {
        return get5DaysForecast(lat, lon)
                .map(forecast -> {
                    List<String> todayActions = new ArrayList<>();
                    List<String> preparationActions = new ArrayList<>();
                    
                    // 오늘 날씨 기반 즉시 액션
                    if (!forecast.getDailyForecasts().isEmpty()) {
                        DailyWeather today = forecast.getDailyForecasts().get(0);
                        todayActions.addAll(generateTodayActions(today));
                    }
                    
                    // 위험 기상에 따른 준비 사항
                    for (WeatherAlert alert : forecast.getAlerts()) {
                        preparationActions.addAll(alert.getActionItems());
                    }
                    
                    String mainMessage = generateMainMessage(forecast.getAlerts(), "MORNING");
                    
                    return new AiAgricultureTip(
                        "MORNING",
                        mainMessage,
                        forecast.getAlerts(),
                        todayActions,
                        preparationActions,
                        null // 농산물 시세는 추후 구현
                    );
                });
    }
    
    /**
     * AI 농업 팁 생성 (저녁용)
     */
    public Mono<AiAgricultureTip> generateEveningTip(double lat, double lon) {
        return get5DaysForecast(lat, lon)
                .map(forecast -> {
                    List<String> todayActions = new ArrayList<>();
                    List<String> preparationActions = new ArrayList<>();
                    
                    // 내일 이후 날씨 대비 준비사항
                    for (WeatherAlert alert : forecast.getAlerts()) {
                        preparationActions.addAll(alert.getActionItems());
                    }
                    
                    // 오늘 저녁에 해야 할 일
                    todayActions.addAll(generateEveningActions(forecast));
                    
                    String mainMessage = generateMainMessage(forecast.getAlerts(), "EVENING");
                    
                    return new AiAgricultureTip(
                        "EVENING",
                        mainMessage,
                        forecast.getAlerts(),
                        todayActions,
                        preparationActions,
                        null // 농산물 시세는 추후 구현
                    );
                });
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
     * 위치 기반 날씨 조회 (기존 호환성)
     */
    public Mono<WeatherInfo> getWeatherByLocation(double lat, double lon) {
        GridCoordinate grid = convertToGrid(lat, lon);
        return getWeatherForecast(String.valueOf(grid.getNx()), String.valueOf(grid.getNy()))
                .map(weather -> {
                    String region = getRegionName(lat, lon);
                    weather.setRegion(region);
                    return weather;
                });
    }
    
    /**
     * 제주 지역 단기예보 조회
     */
    public Mono<WeatherInfo> getJejuWeatherForecast() {
        return getWeatherForecast(JEJU_NX, JEJU_NY);
    }
    
    /**
     * 5일간 상세 기상 예보 조회 및 분석
     */
    public Mono<WeatherForecast5Days> get5DaysForecast(String nx, String ny) {
        String baseDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = getCurrentBaseTime();

        WebClient webClient = webClientBuilder
                .baseUrl(weatherApiUrl)
                .build();

        log.info("5일 예보 조회 시작 - 위치: ({}, {}), 기준: {} {}", nx, ny, baseDate, baseTime);

        return webClient.get()
                .uri(uriBuilder -> {
                    var uri = uriBuilder
                            .queryParam("numOfRows", "1000") // 5일 * 24시간 * 12개 카테고리
                            .queryParam("dataType", "JSON")
                            .queryParam("pageNo", "1")
                            .queryParam("base_date", baseDate)
                            .queryParam("base_time", baseTime)
                            .queryParam("nx", nx)
                            .queryParam("ny", ny)
                            .build(false);

                    String baseUri = uri.toString();
                    String fullUri = baseUri + "&serviceKey=" + serviceKey;
                    return java.net.URI.create(fullUri);
                })
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parse5DaysWeatherResponse)
                .doOnError(error -> log.error("5일 예보 조회 실패: {}", error.getMessage(), error));
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

        return webClient.get()
                .uri(uriBuilder -> {
                    var uri = uriBuilder
                            .queryParam("numOfRows", "300")
                            .queryParam("dataType", "JSON")
                            .queryParam("pageNo", "1")
                            .queryParam("base_date", baseDate)
                            .queryParam("base_time", baseTime)
                            .queryParam("nx", nx)
                            .queryParam("ny", ny)
                            .build(false);

                    String baseUri = uri.toString();
                    String fullUri = baseUri + "&serviceKey=" + serviceKey;
                    return java.net.URI.create(fullUri);
                })
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseWeatherResponse)
                .doOnError(error -> log.error("기상청 API 호출 실패: {}", error.getMessage(), error));
    }
    
    /**
     * 오늘/내일 날씨 비교 (기존 호환성)
     */
    public Mono<TodayTomorrowWeather> getTodayTomorrowWeather(double lat, double lon) {
        return get5DaysForecast(lat, lon)
                .map(forecast -> {
                    WeatherInfo today = null;
                    WeatherInfo tomorrow = null;
                    
                    if (forecast.getDailyForecasts().size() > 0) {
                        today = convertToWeatherInfo(forecast.getDailyForecasts().get(0));
                    }
                    if (forecast.getDailyForecasts().size() > 1) {
                        tomorrow = convertToWeatherInfo(forecast.getDailyForecasts().get(1));
                    }
                    
                    if (today == null) {
                        today = new WeatherInfo();
                        today.setRegion("오늘 (데이터 없음)");
                    }
                    if (tomorrow == null) {
                        tomorrow = new WeatherInfo();
                        tomorrow.setRegion("내일 (데이터 없음)");
                    }
                    
                    return new TodayTomorrowWeather(today, tomorrow);
                });
    }
    
    /**
     * 위치 기반 농업 작업 추천 (기존 호환성)
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
     * 현재 시간에 맞는 기상청 기준시간 계산 (단기예보용)
     */
    private String getCurrentBaseTime() {
        int currentHour = LocalDateTime.now().getHour();
        int currentMinute = LocalDateTime.now().getMinute();

        if (currentHour >= 23 && currentMinute >= 10) return "2300";
        else if (currentHour >= 20 && currentMinute >= 10) return "2000";
        else if (currentHour >= 17 && currentMinute >= 10) return "1700";
        else if (currentHour >= 14 && currentMinute >= 10) return "1400";
        else if (currentHour >= 11 && currentMinute >= 10) return "1100";
        else if (currentHour >= 8 && currentMinute >= 10) return "0800";
        else if (currentHour >= 5 && currentMinute >= 10) return "0500";
        else if (currentHour >= 2 && currentMinute >= 10) return "0200";
        else return "2300";
    }
    
    /**
     * 5일간 기상 데이터 파싱
     */
    private WeatherForecast5Days parse5DaysWeatherResponse(String response) {
        try {
            log.info("=== 5일 예보 데이터 파싱 시작 ===");
            
            JsonNode root = objectMapper.readTree(response);
            JsonNode responseNode = root.path("response");
            JsonNode header = responseNode.path("header");
            
            String resultCode = header.path("resultCode").asText();
            if (!"00".equals(resultCode)) {
                throw new RuntimeException("5일 예보 API 오류: " + header.path("resultMsg").asText());
            }
            
            JsonNode items = responseNode.path("body").path("items").path("item");
            if (!items.isArray() || items.isEmpty()) {
                throw new RuntimeException("5일 예보 응답에 데이터가 없습니다.");
            }
            
            // 날짜별 데이터 그룹핑
            Map<String, DailyWeatherBuilder> dailyData = new HashMap<>();
            
            for (JsonNode item : items) {
                String fcstDate = item.path("fcstDate").asText();
                String category = item.path("category").asText();
                String fcstValue = item.path("fcstValue").asText();
                
                // 5일 범위 내 데이터만 처리
                LocalDate itemDate = LocalDate.parse(fcstDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
                LocalDate today = LocalDate.now();
                if (itemDate.isAfter(today.plusDays(4))) continue;
                
                dailyData.computeIfAbsent(fcstDate, k -> new DailyWeatherBuilder(fcstDate));
                DailyWeatherBuilder builder = dailyData.get(fcstDate);
                
                switch (category) {
                    case "TMP" -> builder.addTemperature(Integer.parseInt(fcstValue));
                    case "TMX" -> builder.setMaxTemp(Integer.parseInt(fcstValue));
                    case "TMN" -> builder.setMinTemp(Integer.parseInt(fcstValue));
                    case "POP" -> builder.addRainProb(Integer.parseInt(fcstValue));
                    case "PCP" -> builder.addRainfall(parseRainfall(fcstValue));
                    case "SKY" -> builder.setSkyCondition(parseSkyCondition(fcstValue)); 
                    case "WSD" -> builder.addWindSpeed(Double.parseDouble(fcstValue));
                }
            }
            
            // 일별 예보 생성
            List<DailyWeather> dailyForecasts = new ArrayList<>();
            LocalDate today = LocalDate.now();
            
            for (int i = 0; i < 5; i++) {
                LocalDate targetDate = today.plusDays(i);
                String dateStr = targetDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String dayLabel = getDayLabel(i);
                
                DailyWeatherBuilder builder = dailyData.get(dateStr);
                if (builder != null) {
                    dailyForecasts.add(builder.build(dayLabel));
                }
            }
            
            // 위험 기상 패턴 분석
            List<WeatherAlert> alerts = analyzeWeatherPatterns(dailyForecasts);
            
            log.info("✅ 5일 예보 파싱 완료: {}일 데이터, {}개 경보", dailyForecasts.size(), alerts.size());
            return new WeatherForecast5Days(dailyForecasts, alerts);
            
        } catch (Exception e) {
            log.error("❌ 5일 예보 파싱 실패: {}", e.getMessage(), e);
            throw new RuntimeException("5일 예보 파싱 실패: " + e.getMessage());
        }
    }
    
    /**
     * 위험 기상 패턴 분석
     */
    private List<WeatherAlert> analyzeWeatherPatterns(List<DailyWeather> forecasts) {
        List<WeatherAlert> alerts = new ArrayList<>();
        
        // 1. 연속 폭염 감지 (3일 이상 35°C 초과)
        alerts.addAll(detectHeatWave(forecasts));
        
        // 2. 집중호우 감지 (강수확률 80% 이상 또는 강수량 30mm 이상)
        alerts.addAll(detectHeavyRain(forecasts));
        
        // 3. 강풍 감지 (풍속 10m/s 이상)
        alerts.addAll(detectHighWind(forecasts));
        
        // 4. 급격한 기온 변화 감지 (일교차 15°C 이상)
        alerts.addAll(detectTemperatureChange(forecasts));
        
        return alerts;
    }
    
    /**
     * 폭염 패턴 감지
     */
    private List<WeatherAlert> detectHeatWave(List<DailyWeather> forecasts) {
        List<WeatherAlert> alerts = new ArrayList<>();
        
        int consecutiveHotDays = 0;
        int startDay = -1;
        
        for (int i = 0; i < forecasts.size(); i++) {
            DailyWeather day = forecasts.get(i);
            
            if (day.getMaxTemp() != null && day.getMaxTemp() >= 35) {
                if (consecutiveHotDays == 0) {
                    startDay = i;
                }
                consecutiveHotDays++;
            } else {
                if (consecutiveHotDays >= 3) {
                    String dayLabel = forecasts.get(startDay).getDayLabel();
                    
                    List<String> actions = Arrays.asList(
                        "🌡️ 차광막 및 그늘막 설치 점검",
                        "💧 자동 급수 시설 정상 작동 확인", 
                        "⏰ 작업 시간을 오전 7시 이전, 오후 6시 이후로 조정",
                        "🧴 작업자 수분 보충용품 준비",
                        "🏠 실내 작업 위주로 계획 변경"
                    );
                    
                    alerts.add(new WeatherAlert(
                        "HEATWAVE",
                        String.format("🔥 %s부터 %d일간 연속 폭염 예상!", dayLabel, consecutiveHotDays),
                        String.format("최고기온 %d°C 이상이 %d일간 지속됩니다", 
                                forecasts.get(startDay).getMaxTemp(), consecutiveHotDays),
                        forecasts.get(startDay).getDate(),
                        consecutiveHotDays,
                        actions
                    ));
                }
                consecutiveHotDays = 0;
            }
        }
        
        return alerts;
    }
    
    /**
     * 집중호우 패턴 감지
     */
    private List<WeatherAlert> detectHeavyRain(List<DailyWeather> forecasts) {
        List<WeatherAlert> alerts = new ArrayList<>();
        
        for (int i = 1; i < forecasts.size(); i++) {
            DailyWeather day = forecasts.get(i);
            
            if ((day.getMaxRainProb() != null && day.getMaxRainProb() >= 80) ||
                (day.getTotalRainfall() != null && day.getTotalRainfall() >= 30)) {
                
                List<String> actions = Arrays.asList(
                    "🌾 수확 가능한 작물 미리 수확",
                    "💧 배수로 및 물빠짐 시설 점검",
                    "🏠 비닐하우스 보강 및 고정",
                    "🔧 농기구 실내 보관",
                    "📦 야외 보관 자재 실내 이동"
                );
                
                alerts.add(new WeatherAlert(
                    "HEAVY_RAIN",
                    String.format("🌧️ %s 집중호우 예상!", day.getDayLabel()),
                    String.format("강수확률 %d%%, 예상 강수량 %dmm", 
                            day.getMaxRainProb(), day.getTotalRainfall()),
                    day.getDate(),
                    1,
                    actions
                ));
            }
        }
        
        return alerts;
    }
    
    /**
     * 강풍 패턴 감지
     */
    private List<WeatherAlert> detectHighWind(List<DailyWeather> forecasts) {
        List<WeatherAlert> alerts = new ArrayList<>();
        
        for (int i = 1; i < forecasts.size(); i++) {
            DailyWeather day = forecasts.get(i);
            
            if (day.getMaxWindSpeed() != null && day.getMaxWindSpeed() >= 10.0) {
                List<String> actions = Arrays.asList(
                    "💨 비닐하우스 및 시설물 고정 점검",
                    "🌱 어린 작물 보호막 설치",
                    "📦 야외 경량 자재 실내 보관",
                    "🔧 농기구 고정 및 정리"
                );
                
                alerts.add(new WeatherAlert(
                    "HIGH_WIND",
                    String.format("💨 %s 강풍 주의!", day.getDayLabel()),
                    String.format("최대 풍속 %.1fm/s 예상", day.getMaxWindSpeed()),
                    day.getDate(),
                    1,
                    actions
                ));
            }
        }
        
        return alerts;
    }
    
    /**
     * 급격한 기온 변화 감지
     */
    private List<WeatherAlert> detectTemperatureChange(List<DailyWeather> forecasts) {
        List<WeatherAlert> alerts = new ArrayList<>();
        
        for (int i = 1; i < forecasts.size(); i++) {
            DailyWeather today = forecasts.get(i-1);
            DailyWeather tomorrow = forecasts.get(i);
            
            if (today.getMaxTemp() != null && tomorrow.getMaxTemp() != null) {
                int tempDiff = Math.abs(tomorrow.getMaxTemp() - today.getMaxTemp());
                
                if (tempDiff >= 15) {
                    List<String> actions = Arrays.asList(
                        "🌡️ 급격한 기온 변화 대비 작물 보호",
                        "🏠 하우스 온도 조절 시설 점검",
                        "🧥 작업복 준비 (기온 변화 대응)"
                    );
                    
                    alerts.add(new WeatherAlert(
                        "TEMP_CHANGE",
                        String.format("⚠️ %s 급격한 기온 변화!", tomorrow.getDayLabel()),
                        String.format("기온이 %d°C → %d°C로 %d°C 변화", 
                                today.getMaxTemp(), tomorrow.getMaxTemp(), tempDiff),
                        tomorrow.getDate(),
                        1,
                        actions
                    ));
                }
            }
        }
        
        return alerts;
    }
    
    /**
     * 오늘 해야 할 작업 생성
     */
    private List<String> generateTodayActions(DailyWeather today) {
        List<String> actions = new ArrayList<>();
        
        if (today.getMaxTemp() != null && today.getMaxTemp() > 30) {
            actions.add("🌡️ 오전 7시 전 물주기 완료");
            actions.add("☀️ 오후 2-4시 야외작업 금지");
        }
        
        if (today.getMaxRainProb() != null && today.getMaxRainProb() > 60) {
            actions.add("🌧️ 비닐하우스 내 작업 위주로 계획");
            actions.add("💧 배수로 점검");
        }
        
        if (today.getMaxWindSpeed() != null && today.getMaxWindSpeed() > 7.0) {
            actions.add("💨 시설물 고정 상태 점검");
        }
        
        if (actions.isEmpty()) {
            actions.add("🌱 농업 작업에 좋은 날씨입니다!");
        }
        
        return actions;
    }
    
    /**
     * 저녁에 해야 할 작업 생성
     */
    private List<String> generateEveningActions(WeatherForecast5Days forecast) {
        List<String> actions = new ArrayList<>();
        
        // 내일 이후 위험 기상에 대비한 작업
        for (WeatherAlert alert : forecast.getAlerts()) {
            if (alert.getAlertType().equals("HEATWAVE")) {
                actions.add("🌅 고온 대비 충분한 관수 실시");
                actions.add("🏠 차광막 설치 점검");
            } else if (alert.getAlertType().equals("HEAVY_RAIN")) {
                actions.add("🌾 수확 가능한 작물 오늘 수확");
                actions.add("🔧 농기구 실내 보관");
            }
        }
        
        if (actions.isEmpty()) {
            actions.add("🌙 내일도 좋은 농업 환경이 예상됩니다");
            actions.add("💤 충분한 휴식으로 내일 작업 준비하세요");
        }
        
        return actions;
    }
    
    /**
     * 메인 메시지 생성
     */
    private String generateMainMessage(List<WeatherAlert> alerts, String tipType) {
        if (alerts.isEmpty()) {
            return tipType.equals("MORNING") ? 
                "🌱 오늘은 농업 작업에 좋은 날씨입니다!" :
                "🌙 앞으로도 좋은 농업 환경이 예상됩니다!";
        }
        
        WeatherAlert mostImportant = alerts.get(0); // 첫 번째 알림을 가장 중요하게
        return mostImportant.getTitle() + " " + mostImportant.getDescription();
    }
    
    // 기존 호환성을 위한 메서드들
    private WeatherInfo parseWeatherResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode responseNode = root.path("response");
            JsonNode header = responseNode.path("header");
            
            String resultCode = header.path("resultCode").asText();
            if (!"00".equals(resultCode)) {
                throw new RuntimeException("기상청 API 오류: " + header.path("resultMsg").asText());
            }
            
            JsonNode items = responseNode.path("body").path("items").path("item");
            if (!items.isArray() || items.isEmpty()) {
                throw new RuntimeException("기상청 API 응답에 데이터가 없습니다.");
            }
            
            Integer maxTemp = null, minTemp = null;
            String temperature = null, humidity = null, skyCondition = null, windSpeed = null;
            Integer rainProbability = null;
            
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            
            for (JsonNode item : items) {
                String category = item.path("category").asText();
                String fcstValue = item.path("fcstValue").asText();
                String fcstDate = item.path("fcstDate").asText();
                
                if (!today.equals(fcstDate)) continue;
                
                switch (category) {
                    case "TMP" -> temperature = fcstValue;
                    case "TMX" -> maxTemp = Integer.parseInt(fcstValue);
                    case "TMN" -> minTemp = Integer.parseInt(fcstValue);
                    case "REH" -> humidity = fcstValue;
                    case "POP" -> rainProbability = Integer.parseInt(fcstValue);
                    case "SKY" -> skyCondition = parseSkyCondition(fcstValue);
                    case "WSD" -> windSpeed = fcstValue;
                }
            }
            
            if (temperature == null || humidity == null || rainProbability == null || skyCondition == null) {
                throw new RuntimeException("기상청 API에서 필수 데이터를 받지 못했습니다.");
            }
            
            return WeatherInfo.builder()
                    .temperature(temperature)
                    .maxTemperature(maxTemp != null ? maxTemp : Integer.parseInt(temperature))
                    .minTemperature(minTemp != null ? minTemp : Integer.parseInt(temperature))
                    .humidity(humidity)
                    .rainProbability(rainProbability)
                    .skyCondition(skyCondition)
                    .windSpeed(windSpeed != null ? windSpeed : "0.0")
                    .region("제주시")
                    .build();
            
        } catch (Exception e) {
            log.error("❌ 날씨 데이터 파싱 실패: {}", e.getMessage(), e);
            throw new RuntimeException("날씨 데이터 파싱 실패: " + e.getMessage());
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
    
    private Integer parseRainfall(String pcp) {
        if (pcp == null || pcp.equals("강수없음") || pcp.equals("0")) return 0;
        
        try {
            if (pcp.contains("mm 미만")) return 0;
            if (pcp.contains("~")) return 40;
            if (pcp.contains("mm 이상")) return 50;
            return (int) Double.parseDouble(pcp.replace("mm", ""));
        } catch (Exception e) {
            return 0;
        }
    }
    
    private String getDayLabel(int daysFromToday) {
        return switch (daysFromToday) {
            case 0 -> "오늘";
            case 1 -> "내일";
            case 2 -> "모레";
            default -> daysFromToday + "일 후";
        };
    }
    
    private String getRegionName(double lat, double lon) {
        if (lat >= 33.49 && lat <= 33.51 && lon >= 126.51 && lon <= 126.54) {
            return "제주시";
        } else if (lat >= 33.24 && lat <= 33.26 && lon >= 126.55 && lon <= 126.57) {
            return "서귀포시";
        } else {
            return "제주도";
        }
    }
    
    private WeatherInfo convertToWeatherInfo(DailyWeather daily) {
        return WeatherInfo.builder()
                .temperature(String.valueOf((daily.getMaxTemp() + daily.getMinTemp()) / 2))
                .maxTemperature(daily.getMaxTemp())
                .minTemperature(daily.getMinTemp())
                .humidity("75") // 기본값
                .rainProbability(daily.getMaxRainProb() != null ? daily.getMaxRainProb() : 0)
                .skyCondition(daily.getSkyCondition())
                .windSpeed(daily.getMaxWindSpeed() != null ? String.valueOf(daily.getMaxWindSpeed()) : "0.0")
                .region(daily.getDayLabel())
                .build();
    }
    
    // 기존 호환성을 위한 메서드들
    private List<String> generateMorningTips(WeatherInfo today) {
        List<String> tips = new ArrayList<>();
        
        if (today.isHighTemperature()) {
            tips.add("🌡️ 고온 주의! 오전 7시 전에 물주기 완료하세요");
            tips.add("☀️ 오후 2-4시 야외작업 금지, 실내 작업으로 전환");
        }
        
        if (today.isRainExpected()) {
            tips.add("🌧️ 강수 예상! 비닐하우스 내 작업 위주로 계획");
            tips.add("💧 배수로 점검 및 물빠짐 확인");
        }
        
        if (tips.isEmpty()) {
            tips.add("🌱 농업 작업에 좋은 날씨입니다!");
        }
        
        return tips;
    }
    
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
        
        if (tips.isEmpty()) {
            tips.add("🌙 내일도 좋은 농업 환경이 예상됩니다");
        }
        
        return tips;
    }
    
    public Mono<String> getCurrentWeatherSummary() {
        return getJejuWeatherForecast()
                .map(weather -> String.format("현재 제주 날씨: %s, 기온 %s°C, 습도 %s%%", 
                        weather.getSkyCondition(), weather.getTemperature(), weather.getHumidity()));
    }
    
    public Mono<String> getFarmWorkRecommendation() {
        return getJejuWeatherForecast()
                .map(weather -> {
                    double temp = Double.parseDouble(weather.getTemperature());
                    int humidity = Integer.parseInt(weather.getHumidity());
                    
                    if (temp > 30) return "고온 주의: 오전 7시 이전 또는 오후 6시 이후 작업 권장";
                    else if (temp < 5) return "저온 주의: 실내 작업 또는 방한 대비 필수";
                    else if (humidity > 80) return "고습 주의: 통풍이 잘 되는 곳에서 작업";
                    else if (weather.getRainProbability() > 60) return "강수 예상: 실내 작업 권장";
                    else return "농업 작업에 적합한 날씨입니다";
                });
    }
    
    /**
     * 일별 날씨 데이터 빌더 클래스
     */
    private static class DailyWeatherBuilder {
        private final String date;
        private Integer maxTemp = null;
        private Integer minTemp = null;
        private final List<Integer> temperatures = new ArrayList<>();
        private final List<Integer> rainProbs = new ArrayList<>();
        private final List<Integer> rainfalls = new ArrayList<>();
        private final List<Double> windSpeeds = new ArrayList<>();
        private String skyCondition = "맑음";
        
        public DailyWeatherBuilder(String date) {
            this.date = date;
        }
        
        public void addTemperature(int temp) { temperatures.add(temp); }
        public void setMaxTemp(int temp) { this.maxTemp = temp; }
        public void setMinTemp(int temp) { this.minTemp = temp; }
        public void addRainProb(int prob) { rainProbs.add(prob); }
        public void addRainfall(int rainfall) { rainfalls.add(rainfall); }
        public void addWindSpeed(double speed) { windSpeeds.add(speed); }
        public void setSkyCondition(String condition) { this.skyCondition = condition; }
        
        public DailyWeather build(String dayLabel) {
            Integer finalMaxTemp = maxTemp != null ? maxTemp : 
                temperatures.stream().max(Integer::compareTo).orElse(25);
            Integer finalMinTemp = minTemp != null ? minTemp : 
                temperatures.stream().min(Integer::compareTo).orElse(20);
            Integer maxRainProb = rainProbs.stream().max(Integer::compareTo).orElse(0);
            Integer totalRain = rainfalls.stream().mapToInt(Integer::intValue).sum();
            Double maxWind = windSpeeds.stream().max(Double::compareTo).orElse(0.0);
            
            return new DailyWeather(date, dayLabel, finalMaxTemp, finalMinTemp, 
                    maxRainProb, totalRain, skyCondition, maxWind);
        }
    }
}
