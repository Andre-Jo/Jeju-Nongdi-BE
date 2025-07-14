package com.jeju_nongdi.jeju_nongdi.client.price;

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
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PriceApiClient {
    
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    @Value("${external.api.price.url:http://www.kamis.or.kr/service/price}")
    private String priceApiUrl;
    
    @Value("${external.api.price.service-key:}")
    private String serviceKey;
    
    /**
     * 특정 작물의 현재 가격 정보 조회
     */
    public Mono<PriceInfo> getCropPrice(String cropName) {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        WebClient webClient = webClientBuilder
                .baseUrl(priceApiUrl)
                .build();
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/item.do")
                        .queryParam("action", "dailyPriceByCategoryList")
                        .queryParam("p_cert_key", serviceKey)
                        .queryParam("p_cert_id", "aT")
                        .queryParam("p_returntype", "json")
                        .queryParam("p_product_cls_code", "01") // 농산물
                        .queryParam("p_startday", currentDate)
                        .queryParam("p_endday", currentDate)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> parsePriceResponse(response, cropName))
                .doOnError(error -> log.error("가격 API 호출 실패: {}", error.getMessage()))
                .onErrorReturn(createDummyPriceInfo(cropName));
    }
    
    /**
     * 제주 특산물 가격 정보 조회
     */
    public Mono<List<PriceInfo>> getJejuSpecialtyPrices() {
        List<String> jejuSpecialties = List.of("감귤", "한라봉", "천혜향", "레드향", "당근");
        
        List<Mono<PriceInfo>> priceMono = jejuSpecialties.stream()
                .map(this::getCropPrice)
                .toList();
        
        return Mono.zip(priceMono, objects -> {
            List<PriceInfo> result = new ArrayList<>();
            for (Object obj : objects) {
                result.add((PriceInfo) obj);
            }
            return result;
        });
    }
    
    /**
     * 가격 동향 분석
     */
    public Mono<String> getPriceTrendAnalysis(String cropName) {
        return getCropPrice(cropName)
                .map(price -> {
                    if (price.getPriceChange() > 10) {
                        return String.format("%s 가격이 지난주 대비 %.1f%% 상승했습니다. 출하 시기를 조정해보세요.", 
                                cropName, price.getPriceChange());
                    } else if (price.getPriceChange() < -10) {
                        return String.format("%s 가격이 지난주 대비 %.1f%% 하락했습니다. 저장 후 출하를 고려해보세요.", 
                                cropName, Math.abs(price.getPriceChange()));
                    } else {
                        return String.format("%s 가격이 안정적입니다. 정상 출하 시기를 유지하세요.", cropName);
                    }
                });
    }
    
    /**
     * 수익성 분석
     */
    public Mono<String> getProfitabilityAnalysis(String cropName, double productionCost) {
        return getCropPrice(cropName)
                .map(price -> {
                    double profit = price.getCurrentPrice() - productionCost;
                    double profitRate = (profit / productionCost) * 100;
                    
                    if (profitRate > 30) {
                        return String.format("%s의 현재 수익률이 %.1f%%로 매우 좋습니다! 적극 출하하세요.", 
                                cropName, profitRate);
                    } else if (profitRate > 10) {
                        return String.format("%s의 현재 수익률이 %.1f%%로 양호합니다.", 
                                cropName, profitRate);
                    } else if (profitRate > 0) {
                        return String.format("%s의 현재 수익률이 %.1f%%로 저조합니다. 출하 시기 조정을 고려하세요.", 
                                cropName, profitRate);
                    } else {
                        return String.format("%s의 현재 가격으로는 손실이 예상됩니다. 저장 후 출하를 권장합니다.", cropName);
                    }
                });
    }
    
    private PriceInfo parsePriceResponse(String response, String cropName) {
        try {
            // 실제 KAMIS API 응답 파싱 로직
            // 현재는 더미 데이터로 구현
            return createDummyPriceInfo(cropName);
            
        } catch (Exception e) {
            log.error("가격 데이터 파싱 실패: {}", e.getMessage());
            return createDummyPriceInfo(cropName);
        }
    }
    
    private PriceInfo createDummyPriceInfo(String cropName) {
        // 더미 데이터 생성 (실제 API 연동 전까지 사용)
        double basePrice = switch (cropName) {
            case "감귤" -> 3500.0;
            case "한라봉" -> 8000.0;
            case "천혜향" -> 12000.0;
            case "당근" -> 2000.0;
            case "보리" -> 1500.0;
            case "감자" -> 2500.0;
            default -> 3000.0;
        };
        
        // 랜덤 변동 적용
        double variation = (Math.random() - 0.5) * 20; // -10% ~ +10% 변동
        double currentPrice = basePrice * (1 + variation / 100);
        
        return PriceInfo.builder()
                .cropName(cropName)
                .currentPrice(currentPrice)
                .previousPrice(basePrice)
                .priceChange(variation)
                .unit("kg")
                .marketType("도매")
                .region("제주")
                .lastUpdated(LocalDate.now().toString())
                .build();
    }
}
