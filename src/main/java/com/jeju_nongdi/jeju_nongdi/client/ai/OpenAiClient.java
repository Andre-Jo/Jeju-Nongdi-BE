package com.jeju_nongdi.jeju_nongdi.client.ai;

import com.jeju_nongdi.jeju_nongdi.client.price.PriceInfo;
import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherInfo;
import com.jeju_nongdi.jeju_nongdi.entity.UserPreference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAiClient {
    
    private final ChatClient chatClient;
    
    @Value("${spring.ai.openai.api-key:}")
    private String openAiApiKey;
    
    /**
     * 날씨 기반 농업 조언 생성
     */
    public String generateWeatherBasedAdvice(WeatherInfo weather, UserPreference userPreference) {
        try {
            String template = """
                제주 농업인을 위한 오늘의 날씨 기반 농업 조언을 생성해주세요.
                
                현재 날씨 정보:
                - 기온: {temperature}°C
                - 습도: {humidity}%
                - 강수확률: {rainProbability}%
                - 하늘상태: {skyCondition}
                - 풍속: {windSpeed}m/s
                
                농업인 정보:
                - 주요 작물: {primaryCrops}
                - 농업 경력: {experience}년
                - 농업 유형: {farmingType}
                
                다음 형식으로 답변해주세요:
                🌡️ 오늘의 날씨 요약
                ⚠️ 주의사항
                ✅ 권장 작업
                🕐 적합한 작업 시간
                """;
            
            PromptTemplate promptTemplate = new PromptTemplate(template);
            Map<String, Object> variables = Map.of(
                    "temperature", weather.getTemperature(),
                    "humidity", weather.getHumidity(),
                    "rainProbability", weather.getRainProbability(),
                    "skyCondition", weather.getSkyCondition(),
                    "windSpeed", weather.getWindSpeed(),
                    "primaryCrops", userPreference != null ? String.join(", ", userPreference.getPrimaryCropsList()) : "일반작물",
                    "experience", userPreference != null ? userPreference.getFarmingExperience() : 5,
                    "farmingType", userPreference != null && userPreference.getFarmingType() != null ? 
                            userPreference.getFarmingType().getDescription() : "전통농업"
            );
            
            Prompt prompt = promptTemplate.create(variables);
            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
            
            return response.getResult().getOutput().getContent();
            
        } catch (Exception e) {
            log.error("OpenAI API 호출 실패: {}", e.getMessage());
            return generateFallbackWeatherAdvice(weather);
        }
    }
    
    /**
     * 작물별 생육 단계 가이드 생성
     */
    public String generateCropGuide(String cropName, String currentSeason) {
        try {
            String template = """
                제주도에서 {cropName} 재배를 위한 {currentSeason} 시기의 상세 가이드를 작성해주세요.
                
                다음 내용을 포함해주세요:
                🌱 현재 시기 생육 상태
                💧 물 관리 방법
                🌿 비료 관리
                ✂️ 가지치기 및 관리 작업
                🐛 병해충 예방법
                📅 앞으로 2주간 주요 작업
                
                제주도의 기후 특성을 고려하여 실용적인 조언을 제공해주세요.
                """;
            
            PromptTemplate promptTemplate = new PromptTemplate(template);
            Map<String, Object> variables = Map.of(
                    "cropName", cropName,
                    "currentSeason", currentSeason
            );
            
            Prompt prompt = promptTemplate.create(variables);
            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
            
            return response.getResult().getOutput().getContent();
            
        } catch (Exception e) {
            log.error("작물 가이드 생성 실패: {}", e.getMessage());
            return generateFallbackCropGuide(cropName);
        }
    }
    
    /**
     * 수익성 분석 및 출하 시기 조언
     */
    public String generateProfitAnalysis(List<PriceInfo> priceInfos, UserPreference userPreference) {
        try {
            StringBuilder priceData = new StringBuilder();
            for (PriceInfo price : priceInfos) {
                priceData.append(String.format("- %s: %s (%s)\n", 
                        price.getCropName(), 
                        price.getFormattedPrice(), 
                        price.getPriceChangeDescription()));
            }
            
            String template = """
                제주 농업인을 위한 수익성 분석 및 출하 전략을 제안해주세요.
                
                현재 농산물 가격 정보:
                {priceData}
                
                농업인 정보:
                - 주요 작물: {primaryCrops}
                - 농업 경력: {experience}년
                - 농장 규모: {farmSize}㎡
                
                다음 형식으로 분석해주세요:
                📊 가격 동향 분석
                💰 수익성 평가
                📅 최적 출하 시기
                💡 수익 향상 전략
                ⚠️ 위험 요소 및 대응방안
                """;
            
            PromptTemplate promptTemplate = new PromptTemplate(template);
            Map<String, Object> variables = Map.of(
                    "priceData", priceData.toString(),
                    "primaryCrops", userPreference != null ? String.join(", ", userPreference.getPrimaryCropsList()) : "일반작물",
                    "experience", userPreference != null ? userPreference.getFarmingExperience() : 5,
                    "farmSize", userPreference != null ? userPreference.getFarmSize() : 1000.0
            );
            
            Prompt prompt = promptTemplate.create(variables);
            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
            
            return response.getResult().getOutput().getContent();
            
        } catch (Exception e) {
            log.error("수익성 분석 생성 실패: {}", e.getMessage());
            return generateFallbackProfitAnalysis(priceInfos);
        }
    }
    
    /**
     * 일반적인 농업 질의응답
     */
    public String generateFarmingAdvice(String question, UserPreference userPreference) {
        try {
            String template = """
                제주도 농업 전문가로서 다음 질문에 답변해주세요.
                
                질문: {question}
                
                농업인 정보:
                - 주요 작물: {primaryCrops}
                - 농업 경력: {experience}년
                - 농업 유형: {farmingType}
                - 농장 위치: {location}
                
                제주도의 기후와 토양 특성을 고려하여 실용적이고 구체적인 조언을 제공해주세요.
                """;
            
            PromptTemplate promptTemplate = new PromptTemplate(template);
            Map<String, Object> variables = Map.of(
                    "question", question,
                    "primaryCrops", userPreference != null ? String.join(", ", userPreference.getPrimaryCropsList()) : "일반작물",
                    "experience", userPreference != null ? userPreference.getFarmingExperience() : 5,
                    "farmingType", userPreference != null && userPreference.getFarmingType() != null ? 
                            userPreference.getFarmingType().getDescription() : "전통농업",
                    "location", userPreference != null ? userPreference.getFarmLocation() : "제주"
            );
            
            Prompt prompt = promptTemplate.create(variables);
            ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
            
            return response.getResult().getOutput().getContent();
            
        } catch (Exception e) {
            log.error("농업 조언 생성 실패: {}", e.getMessage());
            return "죄송합니다. 현재 AI 조언 서비스를 이용할 수 없습니다. 잠시 후 다시 시도해주세요.";
        }
    }
    
    // 폴백 메서드들 (AI API 실패 시 사용)
    private String generateFallbackWeatherAdvice(WeatherInfo weather) {
        StringBuilder advice = new StringBuilder();
        advice.append("🌡️ 오늘의 날씨 요약\n");
        advice.append(weather.getFormattedSummary()).append("\n\n");
        
        advice.append("⚠️ 주의사항\n");
        if (weather.isHighTemperature()) {
            advice.append("- 고온 주의: 한낮 야외 작업을 피하세요\n");
        }
        if (weather.isRainExpected()) {
            advice.append("- 강수 예상: 실내 작업을 계획하세요\n");
        }
        
        advice.append("\n✅ 권장 작업\n");
        if (weather.isGoodForFarmWork()) {
            advice.append("- 농업 작업에 적합한 날씨입니다\n");
            advice.append("- 일반적인 농장 관리 작업을 진행하세요\n");
        }
        
        return advice.toString();
    }
    
    private String generateFallbackCropGuide(String cropName) {
        return String.format("""
                🌱 %s 기본 관리 가이드
                
                💧 물 관리: 토양 상태를 확인하여 적절히 급수
                🌿 비료 관리: 생육 단계에 맞는 비료 시비
                ✂️ 관리 작업: 정기적인 점검 및 관리
                🐛 병해충 예방: 예방적 방제 실시
                📅 향후 작업: 계절에 맞는 관리 작업 계획
                
                자세한 사항은 농업기술센터에 문의하세요.
                """, cropName);
    }
    
    private String generateFallbackProfitAnalysis(List<PriceInfo> priceInfos) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("📊 가격 동향 분석\n");
        for (PriceInfo price : priceInfos) {
            analysis.append(String.format("- %s: %s\n", price.getCropName(), price.getTradeRecommendation()));
        }
        
        analysis.append("\n💰 수익성 평가\n");
        analysis.append("현재 시장 상황을 종합적으로 검토하여 출하 계획을 수립하세요.\n");
        
        return analysis.toString();
    }
}
