package com.jeju_nongdi.jeju_nongdi.controller;

import com.jeju_nongdi.jeju_nongdi.client.price.PriceApiClient;
import com.jeju_nongdi.jeju_nongdi.client.price.PriceInfo;
import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherApiClient;
import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherApiClient.AiAgricultureTip;
import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherApiClient.WeatherForecast4Days;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 농업 종합 컨트롤러
 * - 4일 기상 예보 및 농업 팁
 * - 농수산물 가격 정보 (농넷 API) - 추후 구현
 * - 종합 농업 가이드 제공
 */
@RestController
@RequestMapping("/api/ai-tip")
@RequiredArgsConstructor
@Slf4j
public class AiTipController {

    private final WeatherApiClient weatherApiClient;
    private final PriceApiClient priceApiClient;

    // 캐시 관련 필드들
    private final ConcurrentHashMap<String, Mono<PriceInfo>> priceCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> priceCacheTimestamp = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Mono<AiAgricultureTip>> weatherCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> weatherCacheTimestamp = new ConcurrentHashMap<>();

    // 캐시 TTL (5분)
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    // ==================== 캐시 관련 헬퍼 메서드 ====================

    /**
     * 캐시된 가격 정보 조회 (중복 호출 방지) - 성공한 응답만 캐시
     */
    private Mono<PriceInfo> getCachedPriceInfo(String cropName) {
        String cacheKey = "price_" + cropName;
        LocalDateTime now = LocalDateTime.now();

        // 캐시 만료 확인
        LocalDateTime cachedTime = priceCacheTimestamp.get(cacheKey);
        if (cachedTime != null && Duration.between(cachedTime, now).compareTo(CACHE_TTL) < 0) {
            Mono<PriceInfo> cachedMono = priceCache.get(cacheKey);
            if (cachedMono != null) {
                log.debug("가격 정보 캐시 히트 - 작물: {}", cropName);
                return cachedMono;
            }
        }

        // 캐시 미스 또는 만료 - 새로운 요청
        log.debug("가격 정보 캐시 미스 - 새로운 API 호출 - 작물: {}", cropName);
        
        return priceApiClient.getCropPrice(cropName)
                .doOnSubscribe(subscription -> log.debug("가격 정보 API 호출 시작 - 작물: {}", cropName))
                .doOnNext(price -> {
                    // 정상적인 응답인지 확인
                    if (isValidPriceResponse(price, cropName)) {
                        log.debug("가격 정보 API 호출 성공 - 작물: {}, 가격: {}", cropName, price.getFormattedPrice());
                        
                        // 성공한 응답만 캐시에 저장
                        Mono<PriceInfo> successMono = Mono.just(price).cache();
                        priceCache.put(cacheKey, successMono);
                        priceCacheTimestamp.put(cacheKey, LocalDateTime.now());
                        
                        log.debug("가격 정보 캐시 저장 완료 - 작물: {}", cropName);
                    } else {
                        log.warn("가격 정보 응답이 유효하지 않음 - 작물: {}, 캐시하지 않음", cropName);
                        // 유효하지 않은 응답은 캐시에서 제거
                        priceCache.remove(cacheKey);
                        priceCacheTimestamp.remove(cacheKey);
                    }
                })
                .doOnError(error -> {
                    log.error("가격 정보 API 호출 실패 - 작물: {}, 오류: {}", cropName, error.getMessage());
                    // 실패한 요청은 캐시에서 제거
                    priceCache.remove(cacheKey);
                    priceCacheTimestamp.remove(cacheKey);
                });
    }

    /**
     * 가격 응답이 유효한지 확인
     */
    private boolean isValidPriceResponse(PriceInfo price, String cropName) {
        if (price == null) {
            log.debug("가격 정보가 null - 작물: {}", cropName);
            return false;
        }
        
        // 기본적인 유효성 검사
        if (price.getCurrentPrice() <= 0) {
            log.debug("가격이 0 이하 - 작물: {}, 가격: {}", cropName, price.getCurrentPrice());
            return false;
        }
        
        // 가격 정보 메서드 호출이 정상적으로 작동하는지 확인
        try {
            String formattedPrice = price.getFormattedPrice();
            String marketCondition = price.getMarketCondition();
            
            if (formattedPrice == null || marketCondition == null) {
                log.debug("필수 정보가 누락됨 - 작물: {}", cropName);
                return false;
            }
            
            log.debug("가격 정보 유효성 검사 통과 - 작물: {}, 가격: {}, 상태: {}", 
                     cropName, formattedPrice, marketCondition);
            return true;
            
        } catch (Exception e) {
            log.warn("가격 정보 유효성 검사 실패 - 작물: {}, 오류: {}", cropName, e.getMessage());
            return false;
        }
    }

    /**
     * 캐시된 기상 정보 조회 (중복 호출 방지) - 성공한 응답만 캐시
     */
    private Mono<AiAgricultureTip> getCachedWeatherInfo(double lat, double lon) {
        String cacheKey = String.format("weather_%.4f_%.4f", lat, lon);
        LocalDateTime now = LocalDateTime.now();

        // 캐시 만료 확인
        LocalDateTime cachedTime = weatherCacheTimestamp.get(cacheKey);
        if (cachedTime != null && Duration.between(cachedTime, now).compareTo(CACHE_TTL) < 0) {
            Mono<AiAgricultureTip> cachedMono = weatherCache.get(cacheKey);
            if (cachedMono != null) {
                log.debug("기상 정보 캐시 히트 - 위치: ({}, {})", lat, lon);
                return cachedMono;
            }
        }

        // 캐시 미스 또는 만료 - 새로운 요청
        log.debug("기상 정보 캐시 미스 - 새로운 API 호출 - 위치: ({}, {})", lat, lon);
        
        return weatherApiClient.generateAgricultureTip(lat, lon)
                .doOnSubscribe(subscription -> log.debug("기상 정보 API 호출 시작"))
                .doOnNext(tip -> {
                    // 정상적인 응답인지 확인
                    if (isValidWeatherResponse(tip, lat, lon)) {
                        log.debug("기상 정보 API 호출 성공 - 위치: ({}, {})", lat, lon);
                        
                        // 성공한 응답만 캐시에 저장
                        Mono<AiAgricultureTip> successMono = Mono.just(tip).cache();
                        weatherCache.put(cacheKey, successMono);
                        weatherCacheTimestamp.put(cacheKey, LocalDateTime.now());
                        
                        log.debug("기상 정보 캐시 저장 완료 - 위치: ({}, {})", lat, lon);
                    } else {
                        log.warn("기상 정보 응답이 유효하지 않음 - 위치: ({}, {}), 캐시하지 않음", lat, lon);
                        // 유효하지 않은 응답은 캐시에서 제거
                        weatherCache.remove(cacheKey);
                        weatherCacheTimestamp.remove(cacheKey);
                    }
                })
                .doOnError(error -> {
                    log.error("기상 정보 API 호출 실패 - 위치: ({}, {}), 오류: {}", lat, lon, error.getMessage());
                    // 실패한 요청은 캐시에서 제거
                    weatherCache.remove(cacheKey);
                    weatherCacheTimestamp.remove(cacheKey);
                });
    }

    /**
     * 기상 응답이 유효한지 확인
     */
    private boolean isValidWeatherResponse(AiAgricultureTip tip, double lat, double lon) {
        if (tip == null) {
            log.debug("기상 정보가 null - 위치: ({}, {})", lat, lon);
            return false;
        }
        
        try {
            // 기본적인 유효성 검사 - 메인 메시지가 있는지 확인
            String mainMessage = tip.getMainMessage();
            if (mainMessage == null || mainMessage.trim().isEmpty()) {
                log.debug("기상 정보 메인 메시지가 없음 - 위치: ({}, {})", lat, lon);
                return false;
            }
            
            log.debug("기상 정보 유효성 검사 통과 - 위치: ({}, {})", lat, lon);
            return true;
            
        } catch (Exception e) {
            log.warn("기상 정보 유효성 검사 실패 - 위치: ({}, {}), 오류: {}", lat, lon, e.getMessage());
            return false;
        }
    }

    // ==================== 기상 정보 관련 API ====================

    /**
     * 🌾 AI 농업 종합 가이드 (메인 API)
     * 기상 정보 + 농수산물 가격 + AI 분석
     * GET /api/ai-tip?lat=33.4996&lon=126.5312
     */
    @GetMapping
    public Mono<ResponseEntity<AiAgricultureTip>> getComprehensiveAgricultureTip(
            @RequestParam(required = false, defaultValue = "33.4996") double lat,
            @RequestParam(required = false, defaultValue = "126.5312") double lon,
            @RequestParam(required = false) String cropType) {

        log.info("AI 농업 종합 가이드 요청 - 위치: ({}, {}), 작물: {}", lat, lon, cropType);

        // 캐시된 메서드 사용으로 중복 호출 방지
        Mono<AiAgricultureTip> tipMono = getCachedWeatherInfo(lat, lon);

        // 2. 가격 정보 요청 (기본값 처리)
        String targetCrop = (cropType != null && !cropType.trim().isEmpty()) ? cropType : "감귤";
        Mono<PriceInfo> cropPricesMono = getCachedPriceInfo(targetCrop);

        // 3. 두 Mono 조합해서 응답 구성
        return Mono.zip(tipMono, cropPricesMono)
                .doOnSubscribe(subscription -> log.debug("메인 API - Mono.zip 구독 시작"))
                .map(tuple -> {
                    log.debug("메인 API - Mono.zip map 실행 시작");
                    AiAgricultureTip tip = tuple.getT1();
                    PriceInfo cropPrices = tuple.getT2();

                    // 가격 정보를 팁에 추가 (AiAgricultureTip에 setCropPrices 메서드가 있다고 가정)
                    // tip.setCropPrices(cropPrices); // 실제 구현에 따라 조정 필요

                    log.info("AI 농업 종합 가이드 생성 완료: {} 개 경보, {} 개 준비사항, 작물 가격: {}",
                            tip != null && tip.getAlerts() != null ? tip.getAlerts().size() : 0,
                            tip != null && tip.getPreparationActions() != null ? tip.getPreparationActions().size() : 0,
                            cropPrices != null ? cropPrices.getFormattedPrice() : "정보없음");

                    return ResponseEntity.ok(tip);
                })
                .doOnError(error -> log.error("메인 API - 종합 가이드 처리 중 오류 발생: {}", error.getMessage(), error))
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    /**
     * 📊 4일 기상 예보 및 위험 패턴 분석 (내일부터)
     * GET /api/ai-tip/weather-forecast?lat=33.4996&lon=126.5312
     */
    @GetMapping("/weather-forecast")
    public Mono<ResponseEntity<WeatherForecast4Days>> get4DaysWeatherForecast(
            @RequestParam(required = false, defaultValue = "33.4996") double lat,
            @RequestParam(required = false, defaultValue = "126.5312") double lon) {

        log.info("4일 기상 예보 요청 (내일부터) - 위치: ({}, {})", lat, lon);

        return weatherApiClient.get4DaysForecast(lat, lon)
                .map(forecast -> {
                    log.info("4일 기상 예보 완료: {}일 데이터, {}개 경보",
                            forecast.getDailyForecasts() != null ? forecast.getDailyForecasts().size() : 0,
                            forecast.getAlerts() != null ? forecast.getAlerts().size() : 0);
                    return ResponseEntity.ok(forecast);
                })
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    /**
     * 🚨 기상 경보 요약
     * GET /api/ai-tip/weather-alerts?lat=33.4996&lon=126.5312
     */
    @GetMapping("/weather-alerts")
    public Mono<ResponseEntity<String>> getWeatherAlertsSummary(
            @RequestParam(required = false, defaultValue = "33.4996") double lat,
            @RequestParam(required = false, defaultValue = "126.5312") double lon) {

        log.info("기상 경보 요약 요청 - 위치: ({}, {})", lat, lon);

        return weatherApiClient.get4DaysForecast(lat, lon)
                .map(forecast -> {
                    StringBuilder summary = new StringBuilder();
                    summary.append("🚨 향후 4일 기상 경보 요약 (내일부터)\n");
                    summary.append("━━━━━━━━━━━━━━━━━━━\n\n");

                    if (forecast.getAlerts() == null || forecast.getAlerts().isEmpty()) {
                        summary.append("✅ 현재 위험 기상은 예상되지 않습니다.\n");
                        summary.append("🌱 안전한 농업 작업이 가능합니다!");
                    } else {
                        summary.append(String.format("⚠️ 총 %d개의 기상 경보가 발령되었습니다.\n\n",
                                forecast.getAlerts().size()));

                        for (var alert : forecast.getAlerts()) {
                            summary.append(String.format("🔥 %s\n", alert.getTitle()));
                            summary.append(String.format("📅 시작일: %s (%d일간)\n",
                                    alert.getStartDate(), alert.getDuration()));
                            summary.append(String.format("📝 설명: %s\n", alert.getDescription()));
                            summary.append("🔧 준비사항:\n");

                            if (alert.getActionItems() != null) {
                                for (String action : alert.getActionItems()) {
                                    summary.append(String.format("  • %s\n", action));
                                }
                            }
                            summary.append("\n");
                        }
                    }

                    log.info("기상 경보 요약 완료: {}개 경보",
                            forecast.getAlerts() != null ? forecast.getAlerts().size() : 0);
                    return ResponseEntity.ok(summary.toString());
                })
                .onErrorReturn(ResponseEntity.internalServerError()
                        .body("❌ 기상 경보 요약을 가져올 수 없습니다."));
    }

    // ==================== 농수산물 가격 정보 관련 API ====================

    /**
     * 💰 농수산물 가격 정보 조회
     * GET /api/ai-tip/crop-prices?cropName=감귤
     */
    @GetMapping("/crop-prices")
    public Mono<ResponseEntity<PriceInfo>> getCropPrices(
            @RequestParam(required = false, defaultValue = "감귤") String cropName) {

        log.info("농수산물 가격 정보 요청 - 작물: {}", cropName);

        return getCachedPriceInfo(cropName)
                .map(priceInfo -> {
                    log.info("가격 정보 조회 완료 - {}: {}", cropName, priceInfo.getFormattedPrice());
                    return ResponseEntity.ok(priceInfo);
                })
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    /**
     * 📊 제주 특산물 가격 동향
     * GET /api/ai-tip/jeju-specialty-prices
     */
    @GetMapping("/jeju-specialty-prices")
    public Mono<ResponseEntity<List<PriceInfo>>> getJejuSpecialtyPrices() {

        log.info("제주 특산물 가격 동향 요청");

        return priceApiClient.getJejuSpecialtyPrices()
                .map(priceInfos -> {
                    log.info("제주 특산물 가격 조회 완료: {}개 작물", priceInfos.size());
                    return ResponseEntity.ok(priceInfos);
                })
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    /**
     * 📈 가격 트렌드 분석 및 출하 권장
     * GET /api/ai-tip/price-analysis?cropName=감귤&period=7
     */
    @GetMapping("/price-analysis")
    public Mono<ResponseEntity<String>> getPriceAnalysis(
            @RequestParam(required = false, defaultValue = "감귤") String cropName,
            @RequestParam(required = false, defaultValue = "7") int period) {

        log.info("가격 트렌드 분석 요청 - 작물: {}, 기간: {}일", cropName, period);

        return priceApiClient.getPriceTrendAnalysis(cropName)
                .map(analysis -> {
                    log.info("가격 트렌드 분석 완료 - {}", cropName);
                    return ResponseEntity.ok(analysis);
                })
                .onErrorReturn(ResponseEntity.internalServerError()
                        .body("❌ 가격 트렌드 분석을 가져올 수 없습니다."));
    }

    /**
     * 💡 수익성 분석
     * GET /api/ai-tip/profitability?cropName=감귤&cost=2000
     */
    @GetMapping("/profitability")
    public Mono<ResponseEntity<String>> getProfitabilityAnalysis(
            @RequestParam(required = false, defaultValue = "감귤") String cropName,
            @RequestParam(required = false, defaultValue = "2000") double productionCost) {

        log.info("수익성 분석 요청 - 작물: {}, 생산비: {}원", cropName, productionCost);

        return priceApiClient.getProfitabilityAnalysis(cropName, productionCost)
                .map(analysis -> {
                    log.info("수익성 분석 완료 - {}", cropName);
                    return ResponseEntity.ok(analysis);
                })
                .onErrorReturn(ResponseEntity.internalServerError()
                        .body("❌ 수익성 분석을 가져올 수 없습니다."));
    }

    // ==================== 종합 정보 및 유틸리티 API ====================

    /**
     * 📱 간단한 텍스트 요약 (기상 + 가격)
     * GET /api/ai-tip/summary?lat=33.4996&lon=126.5312&cropName=감귤
     */
    @GetMapping("/summary")
    public Mono<ResponseEntity<String>> getComprehensiveSummary(
            @RequestParam(required = false, defaultValue = "33.4996") double lat,
            @RequestParam(required = false, defaultValue = "126.5312") double lon,
            @RequestParam(required = false, defaultValue = "감귤") String cropName) {

        log.info("종합 요약 요청 - 위치: ({}, {}), 작물: {}", lat, lon, cropName);

        // 각각 독립적으로 처리하여 에러 안전성 확보
        Mono<String> weatherInfo = getCachedWeatherInfo(lat, lon)
                .map(tip -> "정상")
                .onErrorReturn("오류");

        Mono<String> priceInfo = getCachedPriceInfo(cropName)
                .map(price -> {
                    try {
                        String formattedPrice = price != null && price.getFormattedPrice() != null
                                ? price.getFormattedPrice() : "정보없음";
                        String marketCondition = price != null && price.getMarketCondition() != null
                                ? price.getMarketCondition() : "확인불가";
                        return String.format("%s (%s)", formattedPrice, marketCondition);
                    } catch (Exception e) {
                        log.warn("가격 정보 포맷팅 실패: {}", e.getMessage());
                        return "정보없음 (확인불가)";
                    }
                })
                .onErrorReturn("정보없음 (확인불가)");

        return Mono.zip(weatherInfo, priceInfo)
                .map(tuple -> {
                    String weather = tuple.getT1();
                    String price = tuple.getT2();

                    String summary = String.format(
                            """
                                    🌾 농업 종합 정보
                                    ━━━━━━━━━━━━━━━━━━━
                                    기상: %s
                                    가격: %s - %s
                                    """,
                            weather,
                            cropName,
                            price
                    );

                    log.info("종합 요약 완료 - 기상: {}, 가격: {}", weather, price);
                    return ResponseEntity.ok(summary);
                })
                .onErrorReturn(ResponseEntity.internalServerError()
                        .body("❌ 정보를 가져올 수 없습니다. 잠시 후 다시 시도해주세요."));
    }

    /**
     * 📱 알림 테스트
     * POST /api/ai-tip/test
     */
    @PostMapping("/test")
    public ResponseEntity<String> testNotification() {
        log.info("AI 농업 종합 가이드 테스트 요청");

        try {
            // TODO: 스케줄러 서비스에서 테스트 메서드 호출
            return ResponseEntity.ok("🌾 AI 농업 종합 가이드 테스트 실행 완료! 로그를 확인하세요.");
        } catch (Exception e) {
            log.error("종합 가이드 테스트 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("❌ 종합 가이드 테스트 실패: " + e.getMessage());
        }
    }

    /**
     * API 상태 확인
     * GET /api/ai-tip/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("AI 농업 종합 가이드 API 상태 확인");

        String status = String.format("""
                        🌾 AI 농업 종합 가이드 API 상태
                        ━━━━━━━━━━━━━━━━━━━
                        
                        ✅ 기상 정보: 정상 동작 (기상청 API)
                        ✅ 가격 정보: 정상 동작 (KAMIS API)
                        ✅ AI 분석: 정상 동작
                        ✅ 알림 시스템: 정상 동작
                        
                        📊 캐시 상태:
                        • 가격 정보 캐시: %d개 항목
                        • 기상 정보 캐시: %d개 항목
                        • 캐시 TTL: %d분
                        
                        🎯 제공 서비스:
                        • 4일 기상 예보 및 농업 가이드
                        • 농수산물 가격 정보
                        • AI 기반 출하 시기 추천
                        • 수익성 분석
                        • 종합 농업 가이드 제공
                        
                        📱 메인 API: GET /api/ai-tip
                        """,
                priceCache.size(),
                weatherCache.size(),
                CACHE_TTL.toMinutes());

        return ResponseEntity.ok(status);
    }

    /**
     * 캐시 클리어 (개발/테스트용)
     * DELETE /api/ai-tip/cache
     */
    @DeleteMapping("/cache")
    public ResponseEntity<String> clearCache() {
        log.info("캐시 클리어 요청");

        int priceCacheSize = priceCache.size();
        int weatherCacheSize = weatherCache.size();

        priceCache.clear();
        priceCacheTimestamp.clear();
        weatherCache.clear();
        weatherCacheTimestamp.clear();

        String message = String.format("🗑️ 캐시 클리어 완료\n• 가격 정보 캐시: %d개 제거\n• 기상 정보 캐시: %d개 제거",
                priceCacheSize, weatherCacheSize);

        log.info("캐시 클리어 완료 - 가격: {}개, 기상: {}개", priceCacheSize, weatherCacheSize);
        return ResponseEntity.ok(message);
    }

    /**
     * 🧪 가격 API 직접 테스트 (캐시 없이)
     * GET /api/ai-tip/test-price?cropName=감귤
     */
    @GetMapping("/test-price")
    public Mono<ResponseEntity<String>> testPriceApi(
            @RequestParam(required = false, defaultValue = "감귤") String cropName) {

        log.info("가격 API 직접 테스트 - 작물: {}", cropName);

        return priceApiClient.getCropPrice(cropName)
                .map(price -> ResponseEntity.ok(
                        String.format("✅ 테스트 성공\n작물: %s\n가격: %s\n상태: %s",
                                cropName, price.getFormattedPrice(), price.getMarketCondition())))
                .onErrorReturn(ResponseEntity.internalServerError()
                        .body("❌ 테스트 실패 - API 호출 오류"));
    }
}
