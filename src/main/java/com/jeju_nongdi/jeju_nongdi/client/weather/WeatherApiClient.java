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
     * 4일간 기상 데이터 클래스 (내일부터)
     */
    @Data
    @AllArgsConstructor
    public static class WeatherForecast4Days {
        private List<DailyWeather> dailyForecasts; // 내일부터 4일간 예보
        private List<WeatherAlert> alerts; // 위험 기상 알림
    }

    /**
     * 일별 기상 정보 클래스
     */
    @Data
    @AllArgsConstructor
    public static class DailyWeather {
        private String date; // YYYYMMDD
        private String dayLabel; // "내일", "모레", "3일후", "4일후"
        private Double maxTemp;
        private Double minTemp;
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
        private String mainMessage; // 주요 메시지
        private List<WeatherAlert> alerts; // 기상 경보들
        private List<String> preparationActions; // 미리 준비할 일
        private String marketInfo; // 농산물 시세 정보 (추후 추가)
    }
    
    /**
     * 4일간 상세 기상 예보 조회 및 분석 (내일부터)
     */
    public Mono<WeatherForecast4Days> get4DaysForecast(double lat, double lon) {
        GridCoordinate grid = convertToGrid(lat, lon);
        return get4DaysForecast(String.valueOf(grid.getNx()), String.valueOf(grid.getNy()));
    }
    
    /**
     * AI 농업 팁 생성
     */
    public Mono<AiAgricultureTip> generateAgricultureTip(double lat, double lon) {
        return get4DaysForecast(lat, lon)
                .map(forecast -> {
                    List<String> preparationActions = new ArrayList<>();
                    
                    // 위험 기상에 따른 준비 사항
                    for (WeatherAlert alert : forecast.getAlerts()) {
                        preparationActions.addAll(alert.getActionItems());
                    }
                    
                    String mainMessage = generateMainMessage(forecast.getAlerts());
                    
                    return new AiAgricultureTip(
                        mainMessage,
                        forecast.getAlerts(),
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
     * 4일간 상세 기상 예보 조회 및 분석 (내일부터)
     */
    public Mono<WeatherForecast4Days> get4DaysForecast(String nx, String ny) {
        String baseDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = getCurrentBaseTime();

        WebClient webClient = webClientBuilder
                .baseUrl(weatherApiUrl)
                .build();

        log.info("4일 예보 조회 시작 (내일부터) - 위치: ({}, {}), 기준: {} {}", nx, ny, baseDate, baseTime);

        return webClient.get()
                .uri(uriBuilder -> {
                    var uri = uriBuilder
                            .queryParam("numOfRows", "1000") // 4일 * 24시간 * 12개 카테고리
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
                .map(this::parse4DaysWeatherResponse)
                .doOnError(error -> log.error("4일 예보 조회 실패: {}", error.getMessage(), error));
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
     * 4일간 기상 데이터 파싱 (내일부터)
     */
    private WeatherForecast4Days parse4DaysWeatherResponse(String response) {
        try {
            log.info("=== 4일 예보 데이터 파싱 시작 (내일부터) ===");
            
            JsonNode root = objectMapper.readTree(response);
            JsonNode responseNode = root.path("response");
            JsonNode header = responseNode.path("header");
            
            String resultCode = header.path("resultCode").asText();
            if (!"00".equals(resultCode)) {
                throw new RuntimeException("4일 예보 API 오류: " + header.path("resultMsg").asText());
            }
            
            JsonNode items = responseNode.path("body").path("items").path("item");
            if (!items.isArray() || items.isEmpty()) {
                throw new RuntimeException("4일 예보 응답에 데이터가 없습니다.");
            }
            
            // 날짜별 데이터 그룹핑
            Map<String, DailyWeatherBuilder> dailyData = new HashMap<>();
            
            for (JsonNode item : items) {
                String fcstDate = item.path("fcstDate").asText();
                String category = item.path("category").asText();
                String fcstValue = item.path("fcstValue").asText();
                
                // 내일부터 4일 범위 내 데이터만 처리
                LocalDate itemDate = LocalDate.parse(fcstDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
                LocalDate today = LocalDate.now();
                LocalDate tomorrow = today.plusDays(1);
                
                // 오늘 데이터는 제외, 내일부터 4일간만
                if (itemDate.isBefore(tomorrow) || itemDate.isAfter(tomorrow.plusDays(3))) continue;
                
                dailyData.computeIfAbsent(fcstDate, k -> new DailyWeatherBuilder(fcstDate));
                DailyWeatherBuilder builder = dailyData.get(fcstDate);
                
                switch (category) {
                    case "TMP" -> builder.addTemperature(Integer.parseInt(fcstValue));
                    case "TMX" -> builder.setMaxTemp(Double.parseDouble(fcstValue));
                    case "TMN" -> builder.setMinTemp(Double.parseDouble(fcstValue));
                    case "POP" -> builder.addRainProb(Integer.parseInt(fcstValue));
                    case "PCP" -> builder.addRainfall(parseRainfall(fcstValue));
                    case "SKY" -> builder.setSkyCondition(parseSkyCondition(fcstValue)); 
                    case "WSD" -> builder.addWindSpeed(Double.parseDouble(fcstValue));
                }
            }
            
            // 일별 예보 생성 (내일부터 4일간)
            List<DailyWeather> dailyForecasts = new ArrayList<>();
            LocalDate tomorrow = LocalDate.now().plusDays(1);

            for (int i = 0; i < 4; i++) {
                LocalDate targetDate = tomorrow.plusDays(i);
                String dateStr = targetDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String dayLabel = getDayLabel(i + 1); // +1로 "내일"부터 시작
                
                DailyWeatherBuilder builder = dailyData.get(dateStr);
                if (builder != null) {
                    DailyWeather weather = builder.build(dayLabel);
                    dailyForecasts.add(weather);
                }
            }
            
            // 위험 기상 패턴 분석
            List<WeatherAlert> alerts = analyzeWeatherPatterns(dailyForecasts);
            
            log.info("✅ 4일 예보 파싱 완료 (내일부터): {}일 데이터, {}개 경보", dailyForecasts.size(), alerts.size());
            return new WeatherForecast4Days(dailyForecasts, alerts);
            
        } catch (Exception e) {
            log.error("❌ 4일 예보 파싱 실패: {}", e.getMessage(), e);
            throw new RuntimeException("4일 예보 파싱 실패: " + e.getMessage());
        }
    }
    
    /**
     * 위험 기상 패턴 분석
     */
    private List<WeatherAlert> analyzeWeatherPatterns(List<DailyWeather> forecasts) {
        List<WeatherAlert> alerts = new ArrayList<>();
        
        // 1. 연속 폭염 감지 (3일 이상 30°C 초과)
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
            
            if (day.getMaxTemp() != null && day.getMaxTemp() >= 30) {
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
                            String.format("최고기온 %.1f°C 이상이 %d일간 지속됩니다",
                                    forecasts.get(startDay).getMaxTemp(), consecutiveHotDays),
                            forecasts.get(startDay).getDate(),
                            consecutiveHotDays,
                            actions
                    ));
                }
                consecutiveHotDays = 0;
            }
        }
        
        // 마지막까지 폭염이 계속되는 경우
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
                    String.format("최고기온 %.1f°C 이상이 %d일간 지속됩니다",
                            forecasts.get(startDay).getMaxTemp(), consecutiveHotDays),
                    forecasts.get(startDay).getDate(),
                    consecutiveHotDays,
                    actions
            ));
        }
        
        return alerts;
    }
    
    /**
     * 집중호우 패턴 감지
     */
    private List<WeatherAlert> detectHeavyRain(List<DailyWeather> forecasts) {
        List<WeatherAlert> alerts = new ArrayList<>();
        
        for (DailyWeather day : forecasts) {
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
        
        for (DailyWeather day : forecasts) {
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
            DailyWeather prevDay = forecasts.get(i-1);
            DailyWeather currentDay = forecasts.get(i);
            
            if (prevDay.getMaxTemp() != null && currentDay.getMaxTemp() != null) {
                double tempDiff = Math.abs(currentDay.getMaxTemp() - prevDay.getMaxTemp());
                
                if (tempDiff >= 15) {
                    List<String> actions = Arrays.asList(
                        "🌡️ 급격한 기온 변화 대비 작물 보호",
                        "🏠 하우스 온도 조절 시설 점검",
                        "🧥 작업복 준비 (기온 변화 대응)"
                    );
                    
                    alerts.add(new WeatherAlert(
                        "TEMP_CHANGE",
                        String.format("⚠️ %s 급격한 기온 변화!", currentDay.getDayLabel()),
                        String.format("기온이 %.1f°C → %.1f°C로 %.1f°C 변화",
                                prevDay.getMaxTemp(), currentDay.getMaxTemp(), tempDiff),
                        currentDay.getDate(),
                        1,
                        actions
                    ));
                }
            }
        }
        
        return alerts;
    }
    
    /**
     * 메인 메시지 생성
     */
    private String generateMainMessage(List<WeatherAlert> alerts) {
        if (alerts.isEmpty()) {
            return "🌱 향후 4일간 농업 작업에 좋은 날씨가 예상됩니다!";
        }
        
        WeatherAlert mostImportant = alerts.get(0); // 첫 번째 알림을 가장 중요하게
        return mostImportant.getTitle() + " " + mostImportant.getDescription();
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
            case 1 -> "내일";
            case 2 -> "모레";
            case 3 -> "3일 후";
            case 4 -> "4일 후";
            default -> daysFromToday + "일 후";
        };
    }

    /**
     * 일별 날씨 데이터 빌더 클래스
     */
    private static class DailyWeatherBuilder {
        private final String date;
        private Double maxTemp = null;
        private Double minTemp = null;
        private final List<Integer> temperatures = new ArrayList<>();
        private final List<Integer> rainProbs = new ArrayList<>();
        private final List<Integer> rainfalls = new ArrayList<>();
        private final List<Double> windSpeeds = new ArrayList<>();
        private String skyCondition = "맑음";
        
        public DailyWeatherBuilder(String date) {
            this.date = date;
        }
        
        public void addTemperature(int temp) { temperatures.add(temp); }
        public void setMaxTemp(double temp) { this.maxTemp = temp; }
        public void setMinTemp(double temp) { this.minTemp = temp; }
        public void addRainProb(int prob) { rainProbs.add(prob); }
        public void addRainfall(int rainfall) { rainfalls.add(rainfall); }
        public void addWindSpeed(double speed) { windSpeeds.add(speed); }
        public void setSkyCondition(String condition) { this.skyCondition = condition; }
        
        public DailyWeather build(String dayLabel) {
            Double finalMaxTemp = maxTemp != null ? maxTemp :
                temperatures.stream().max(Integer::compareTo).map(Integer::doubleValue).orElse(null);
            Double finalMinTemp = minTemp != null ? minTemp :
                temperatures.stream().min(Integer::compareTo).map(Integer::doubleValue).orElse(null);
            Integer maxRainProb = rainProbs.stream().max(Integer::compareTo).orElse(0);
            Integer totalRain = rainfalls.stream().mapToInt(Integer::intValue).sum();
            Double maxWind = windSpeeds.stream().max(Double::compareTo).orElse(0.0);
            
            return new DailyWeather(date, dayLabel, finalMaxTemp, finalMinTemp, 
                    maxRainProb, totalRain, skyCondition, maxWind);
        }
    }
}
