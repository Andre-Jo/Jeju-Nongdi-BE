package com.jeju_nongdi.jeju_nongdi.service;

import com.jeju_nongdi.jeju_nongdi.client.price.PriceApiClient;
import com.jeju_nongdi.jeju_nongdi.client.price.PriceInfo;
import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherApiClient;
import com.jeju_nongdi.jeju_nongdi.client.weather.WeatherInfo;
import com.jeju_nongdi.jeju_nongdi.dto.ai.AiTipResponseDto;
import com.jeju_nongdi.jeju_nongdi.dto.ai.DailyTipRequestDto;
import com.jeju_nongdi.jeju_nongdi.dto.ai.DailyTipSummaryDto;
import com.jeju_nongdi.jeju_nongdi.dto.ai.NotificationListDto;
import com.jeju_nongdi.jeju_nongdi.entity.AiTip;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.entity.UserPreference;
import com.jeju_nongdi.jeju_nongdi.repository.AiTipRepository;
import com.jeju_nongdi.jeju_nongdi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AiTipService {

    private final AiTipRepository aiTipRepository;
    private final UserRepository userRepository;
    private final UserPreferenceService userPreferenceService;

    // 외부 API 클라이언트들
    private final WeatherApiClient weatherApiClient;
    private final PriceApiClient priceApiClient;

    /**
     * 특정 사용자의 일일 맞춤 팁 조회
     */
    @Transactional(readOnly = true)
    public DailyTipSummaryDto getDailyTips(Long userId, DailyTipRequestDto requestDto) {
        User user = getUserById(userId);

        if (requestDto == null) {
            requestDto = DailyTipRequestDto.getDefault();
        }

        List<AiTip> tips = getFilteredTips(user, requestDto);

        return DailyTipSummaryDto.builder()
                .targetDate(requestDto.getTargetDate())
                .weatherSummary(generateWeatherSummary(requestDto.getTargetDate()))
                .totalTips(tips.size())
                .unreadTips(aiTipRepository.countUnreadTipsByUser(user))
                .urgentTips(aiTipRepository.countUrgentTipsByUserAndDate(user, requestDto.getTargetDate()))
                .tips(tips.stream().map(AiTipResponseDto::from).collect(Collectors.toList()))
                .todayTasks(generateTodayTasks(user, requestDto.getTargetDate()))
                .statistics(generateTipStatistics(user, requestDto.getTargetDate()))
                .build();
    }

    /**
     * 팁 생성 (더미 데이터로 일단 구현)
     */
    public AiTipResponseDto createTip(Long userId, AiTip.TipType tipType, String title, String content, String cropType) {
        User user = getUserById(userId);

        AiTip tip = AiTip.builder()
                .user(user)
                .tipType(tipType)
                .title(title)
                .content(content)
                .targetDate(LocalDate.now())
                .cropType(cropType)
                .priorityLevel(determinePriorityLevel(tipType))
                .isRead(false)
                .build();

        AiTip savedTip = aiTipRepository.save(tip);
        log.info("새로운 AI 팁 생성: {} - {}", savedTip.getTipType(), savedTip.getTitle());

        return AiTipResponseDto.from(savedTip);
    }

    /**
     * 팁 읽음 처리
     */
    public void markTipAsRead(Long userId, Long tipId) {
        User user = getUserById(userId);

        AiTip tip = aiTipRepository.findById(tipId)
                .orElseThrow(() -> new RuntimeException("팁을 찾을 수 없습니다."));

        if (!tip.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("권한이 없습니다.");
        }

        tip.setIsRead(true);
        aiTipRepository.save(tip);
    }

    /**
     * 사용자 맞춤 팁 자동 생성 (실제 API 데이터 활용)
     */
    public void generateDailyTipsForUser(Long userId) {
        User user = getUserById(userId);
        UserPreference preference = userPreferenceService.getUserPreference(userId);

        // 오늘 이미 생성된 팁이 있는지 확인
        List<AiTip> existingTips = aiTipRepository.findByUserAndTargetDateOrderByPriorityLevelDescCreatedAtDesc(
                user, LocalDate.now());

        if (!existingTips.isEmpty()) {
            log.info("사용자 {}의 오늘 팁이 이미 존재합니다.", userId);
            return;
        }

        List<AiTip> newTips = new ArrayList<>();

        try {
            // 1. 날씨 기반 팁 생성 (규칙 기반)
            if (preference != null && preference.getNotificationWeather()) {
                WeatherInfo weather = weatherApiClient.getJejuWeatherForecast().block();
                if (weather != null) {
                    String weatherAdvice = generateWeatherBasedAdvice(weather, preference);
                    newTips.add(createWeatherTip(user, weather, weatherAdvice));
                }
            }

            // 2. 작물별 생육 가이드 생성 (시즌별 고정 데이터)
            if (preference != null && !preference.getPrimaryCropsList().isEmpty()) {
                String primaryCrop = preference.getPrimaryCropsList().get(0);
                String season = getCurrentSeason();
                String cropGuide = generateSeasonalCropGuide(primaryCrop, season);
                newTips.add(createCropGuideTip(user, primaryCrop, cropGuide));
            }

            // 3. 가격 정보 기반 수익성 팁 생성 (규칙 기반)
            if (preference != null && preference.getNotificationMarket()) {
                List<PriceInfo> priceInfos = priceApiClient.getJejuSpecialtyPrices().block();
                if (priceInfos != null && !priceInfos.isEmpty()) {
                    String profitTip = generateProfitTipFromPrices(priceInfos, preference);
                    newTips.add(createProfitTip(user, profitTip));
                    
                    // 수익성 분석 추가 (주요 작물에 대해서)
                    String primaryCrop = preference.getPrimaryCropsList().get(0);
                    String profitabilityAnalysis = priceApiClient.getProfitabilityAnalysis(primaryCrop, 1500.0).block();
                    if (profitabilityAnalysis != null) {
                        newTips.add(createDetailedProfitTip(user, primaryCrop, profitabilityAnalysis));
                    }
                }
            }

            // 4. 병해충 경보 생성 (시즌별 고정 데이터)
            if (preference != null && shouldGeneratePestAlert()) {
                newTips.add(createSeasonalPestAlert(user, preference));
            }

            // 5. 일손 매칭 팁 (랜덤 생성)
            if (preference != null && preference.getNotificationLabor() && shouldGenerateLaborTip()) {
                newTips.add(createRandomLaborMatchingTip(user, preference));
            }

            // 6. 스마트팜 자동화 제안 (랜덤 생성)
            if (shouldGenerateAutomationTip()) {
                newTips.add(createRandomAutomationTip(user, preference));
            }

        } catch (Exception e) {
            log.error("AI 팁 생성 중 오류 발생: {}", e.getMessage());
            // 오류 시 기본 팁들 생성
            newTips.addAll(createFallbackTips(user, preference));
        }

        aiTipRepository.saveAll(newTips);
        log.info("사용자 {}에게 {} 개의 일일 팁을 생성했습니다.", userId, newTips.size());
    }

    /**
     * 알림 리스트 조회 (돌하르방 클릭용)
     */
    @Transactional(readOnly = true)
    public NotificationListDto getNotificationList(Long userId, LocalDate startDate, LocalDate endDate,
                                                   Integer page, Integer size, List<String> tipTypes) {
        User user = getUserById(userId);

        // 페이징 처리
        int offset = page * size;

        // 기간별 알림 조회
        List<AiTip> tips = aiTipRepository.findByUserAndTargetDateBetweenOrderByTargetDateDescPriorityLevelDescCreatedAtDesc(
                user, startDate, endDate);

        // 타입 필터링
        if (tipTypes != null && !tipTypes.isEmpty()) {
            List<AiTip.TipType> tipTypeEnums = tipTypes.stream()
                    .map(AiTip.TipType::valueOf)
                    .toList();
            tips = tips.stream()
                    .filter(tip -> tipTypeEnums.contains(tip.getTipType()))
                    .collect(Collectors.toList());
        }

        // 페이징 적용
        int totalCount = tips.size();
        int totalPages = (int) Math.ceil((double) totalCount / size);

        List<AiTip> pagedTips = tips.stream()
                .skip(offset)
                .limit(size)
                .collect(Collectors.toList());

        // 읽지 않은 알림 개수 계산
        int unreadCount = (int) tips.stream()
                .filter(tip -> !tip.getIsRead())
                .count();

        // DTO 변환
        List<NotificationListDto.NotificationItem> notificationItems = pagedTips.stream()
                .map(this::convertToNotificationItem)
                .collect(Collectors.toList());

        return NotificationListDto.builder()
                .totalCount((long) totalCount)
                .unreadCount(unreadCount)
                .currentPage(page)
                .totalPages(totalPages)
                .notifications(notificationItems)
                .build();
    }

    // === Private Helper Methods ===

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    private List<AiTip> getFilteredTips(User user, DailyTipRequestDto requestDto) {
        LocalDate targetDate = requestDto.getTargetDate();

        // 기본 조회
        List<AiTip> tips = aiTipRepository.findByUserAndTargetDateOrderByPriorityLevelDescCreatedAtDesc(user, targetDate);

        // 필터링 적용
        if (requestDto.getOnlyUnread() != null && requestDto.getOnlyUnread()) {
            tips = tips.stream().filter(tip -> !tip.getIsRead()).collect(Collectors.toList());
        }

        if (requestDto.getPriorityLevel() != null) {
            tips = tips.stream().filter(tip -> tip.getPriorityLevel() >= requestDto.getPriorityLevel()).collect(Collectors.toList());
        }

        if (requestDto.getCropType() != null && !requestDto.getCropType().isEmpty()) {
            tips = tips.stream().filter(tip -> requestDto.getCropType().equals(tip.getCropType())).collect(Collectors.toList());
        }

        return tips;
    }

    private String generateWeatherSummary(LocalDate date) {
        try {
            WeatherInfo weather = weatherApiClient.getJejuWeatherForecast().block();
            return weather != null ? weather.getFormattedSummary() : "날씨 정보를 가져올 수 없습니다.";
        } catch (Exception e) {
            log.error("날씨 정보 조회 실패: {}", e.getMessage());
            return "맑음, 최고 25°C / 최저 18°C, 오후에 구름 많음";
        }
    }

    private List<String> generateTodayTasks(User user, LocalDate date) {
        // TODO: 사용자 작물과 계절에 맞는 작업 추천
        return List.of(
                "오전 7시까지 물주기 완료",
                "비닐하우스 환기 점검",
                "병해충 발생 여부 확인"
        );
    }

    private DailyTipSummaryDto.TipStatistics generateTipStatistics(User user, LocalDate date) {
        List<Object[]> statistics = aiTipRepository.countTipsByTypeAndUserAndDate(user, date);

        DailyTipSummaryDto.TipStatistics.TipStatisticsBuilder builder = DailyTipSummaryDto.TipStatistics.builder()
                .weatherAlerts(0)
                .cropGuides(0)
                .pestAlerts(0)
                .profitTips(0)
                .automationSuggestions(0)
                .laborRecommendations(0);

        for (Object[] stat : statistics) {
            AiTip.TipType tipType = (AiTip.TipType) stat[0];
            Long count = (Long) stat[1];

            switch (tipType) {
                case WEATHER_ALERT -> builder.weatherAlerts(count.intValue());
                case CROP_GUIDE -> builder.cropGuides(count.intValue());
                case PEST_ALERT -> builder.pestAlerts(count.intValue());
                case PROFIT_TIP -> builder.profitTips(count.intValue());
                case AUTOMATION_SUGGESTION -> builder.automationSuggestions(count.intValue());
                case LABOR_MATCHING -> builder.laborRecommendations(count.intValue());
            }
        }

        return builder.build();
    }

    private Integer determinePriorityLevel(AiTip.TipType tipType) {
        return switch (tipType) {
            case WEATHER_ALERT, PEST_ALERT -> 4; // 긴급
            case CROP_GUIDE -> 3; // 높음
            case PROFIT_TIP, LABOR_MATCHING -> 2; // 보통
            case AUTOMATION_SUGGESTION -> 1; // 낮음
        };
    }

    // === 실제 데이터 기반 팁 생성 메서드들 ===

    private AiTip createWeatherTip(User user, WeatherInfo weather, String aiAdvice) {
        String title = "🌡️ 오늘의 날씨 기반 농업 조언";
        String priorityLevel = weather.isHighTemperature() || weather.isRainExpected() ? "4" : "3";

        return AiTip.builder()
                .user(user)
                .tipType(AiTip.TipType.WEATHER_ALERT)
                .title(title)
                .content(aiAdvice)
                .targetDate(LocalDate.now())
                .weatherCondition(weather.getSkyCondition())
                .priorityLevel(Integer.parseInt(priorityLevel))
                .isRead(false)
                .build();
    }

    private AiTip createCropGuideTip(User user, String cropName, String aiGuide) {
        return AiTip.builder()
                .user(user)
                .tipType(AiTip.TipType.CROP_GUIDE)
                .title("🌱 " + cropName + " 생육 단계 가이드")
                .content(aiGuide)
                .targetDate(LocalDate.now())
                .cropType(cropName)
                .priorityLevel(3)
                .isRead(false)
                .build();
    }

    private AiTip createDetailedProfitTip(User user, String cropName, String profitabilityAnalysis) {
        return AiTip.builder()
                .user(user)
                .tipType(AiTip.TipType.PROFIT_TIP)
                .title("💰 " + cropName + " 상세 수익성 분석")
                .content(profitabilityAnalysis)
                .targetDate(LocalDate.now())
                .cropType(cropName)
                .priorityLevel(2)
                .isRead(false)
                .build();
    }
    
    private AiTip createProfitTip(User user, String profitAnalysis) {
        return AiTip.builder()
                .user(user)
                .tipType(AiTip.TipType.PROFIT_TIP)
                .title("📊 시장 가격 분석 및 출하 전략")
                .content(profitAnalysis)
                .targetDate(LocalDate.now())
                .priorityLevel(2)
                .isRead(false)
                .build();
    }

    // === 실제 데이터 기반 팁 생성 Helper Methods ===

    private String generateWeatherBasedAdvice(WeatherInfo weather, UserPreference preference) {
        StringBuilder advice = new StringBuilder();
        advice.append("🌡️ 오늘의 날씨 기반 농업 조언\n\n");

        if (weather.isHighTemperature()) {
            advice.append("🔥 고온 주의보!\n")
                    .append("- 오전 7시 전 물주기 완료 필수\n")
                    .append("- 오후 2-4시 야외작업 금지\n")
                    .append("- 차광막 설치 및 환기 강화\n");
        }

        if (weather.isRainExpected()) {
            advice.append("☔ 강수 예보\n")
                    .append("- 배수로 점검 및 정비\n")
                    .append("- 실내 작업 위주로 계획\n")
                    .append("- 병해 예방 약제 미리 준비\n");
        }

        if (weather.isGoodForFarmWork()) {
            advice.append("✅ 농업 작업 적합 날씨\n")
                    .append("- 정상적인 농장 작업 가능\n")
                    .append("- 예정된 농업 작업 진행하세요\n");
        }

        advice.append("\n📊 상세 정보: ").append(weather.getFormattedSummary());

        return advice.toString();
    }

    private String generateSeasonalCropGuide(String cropName, String season) {
        return switch (cropName) {
            case "감귤" -> generateCitrusGuide(season);
            case "당근" -> generateCarrotGuide(season);
            case "감자" -> generatePotatoGuide(season);
            case "보리" -> generateBarleyGuide(season);
            default -> String.format("🌱 %s 일반 관리 가이드\n\n%s철 기본 관리 요령을 확인하세요.", cropName, season);
        };
    }

    private String generateCitrusGuide(String season) {
        return switch (season) {
            case "봄" -> """
                🍊 감귤 봄철 관리 가이드
                
                🌱 주요 작업:
                - 새순 관리 및 접목
                - 꽃눈분화기 질소비료 중단
                - 인산·칼륨 비료로 교체
                - 수분 관리 70%로 조절
                
                ⚠️ 주의사항:
                - 가지치기는 4월 말까지 완료
                - 해충 발생 모니터링 강화
                """;
            case "여름" -> """
                🍊 감귤 여름철 관리 가이드
                
                💧 집중 관리:
                - 고온기 수분 공급 강화
                - 열과 방지 차광막 설치
                - 응애류 방제 집중
                - 적과 작업 (7-8월)
                
                📈 품질 향상:
                - 당도 향상을 위한 칼륨 추가
                - 과실 비대기 충분한 수분 공급
                """;
            case "가을" -> """
                🍊 감귤 가을철 관리 가이드
                
                🍂 수확 준비:
                - 당도 체크 (브릭스 12도 이상)
                - 수확 2주 전 물주기 중단
                - 착색 촉진을 위한 온도 관리
                
                📦 출하 전략:
                - 시장 가격 동향 모니터링
                - 저장고 온도 4°C 유지
                """;
            case "겨울" -> """
                🍊 감귤 겨울철 관리 가이드
                
                ❄️ 월동 준비:
                - 한파 대비 방풍망 설치
                - 동해 방지 보온재 준비
                - 수확 후 전정 작업
                
                🔄 내년 준비:
                - 토양 개량 및 유기물 투입
                - 병해충 월동처 제거
                """;
            default -> "감귤 관리 가이드를 확인하세요.";
        };
    }

    private String generateCarrotGuide(String season) {
        return switch (season) {
            case "봄" -> """
                🥕 당근 봄철 관리 가이드
                
                🌱 파종 관리:
                - 3-4월 파종 적기
                - 토양 습도 60-70% 유지
                - 발아 후 솎음 작업
                
                💚 생육 관리:
                - 질소비료 적량 시비
                - 토양 배수 관리
                """;
            case "여름" -> """
                🥕 당근 여름철 관리 가이드
                
                🌿 집중 관리:
                - 고온기 차광 및 관수
                - 뿌리 비대기 칼륨 추가
                - 병해충 방제 강화
                
                📏 품질 관리:
                - 적정 밀도 유지
                - 토양 경도 관리
                """;
            case "가을" -> """
                🥕 당근 가을철 관리 가이드
                
                🍂 수확 관리:
                - 9-11월 수확 적기
                - 당도 및 색깔 확인
                - 수확 후 선별 작업
                
                📦 저장 방법:
                - 습도 95% 유지
                - 온도 0-1°C 저장
                """;
            default -> "당근 관리 가이드를 확인하세요.";
        };
    }

    private String generatePotatoGuide(String season) {
        return switch (season) {
            case "봄" -> """
                🥔 감자 봄철 관리 가이드
                
                🌱 파종 준비:
                - 2-3월 씨감자 심기
                - 토양 온도 8°C 이상 확인
                - 배수로 정비
                
                💚 초기 관리:
                - 발아 후 북주기 작업
                - 질소비료 기비 시용
                """;
            case "여름" -> """
                🥔 감자 여름철 관리 가이드
                
                🌿 생육 관리:
                - 괴경 형성기 충분한 관수
                - 2차 북주기 실시
                - 역병 예방 방제
                
                📈 수량 증대:
                - 칼륨비료 추가 시용
                - 잎마름병 방제
                """;
            case "가을" -> """
                🥔 감자 가을철 관리 가이드
                
                🍂 수확 준비:
                - 줄기 마름 확인 후 수확
                - 수확 2주 전 관수 중단
                - 맑은 날 수확 작업
                
                📦 저장 관리:
                - 그늘에서 충분히 건조
                - 온도 2-4°C 저장
                """;
            default -> "감자 관리 가이드를 확인하세요.";
        };
    }

    private String generateBarleyGuide(String season) {
        return switch (season) {
            case "가을" -> """
                🌾 보리 가을철 관리 가이드
                
                🌱 파종 관리:
                - 10-11월 파종 적기
                - 토양 배수 개선
                - 적정 파종량 준수
                
                💚 초기 관리:
                - 발아 후 솎음 작업
                - 기비 시용 완료
                """;
            case "겨울" -> """
                🌾 보리 겨울철 관리 가이드
                
                ❄️ 월동 관리:
                - 분얼기 추비 시용
                - 한파 대비 관리
                - 습해 방지 배수
                
                🌱 생육 점검:
                - 분얼 상태 확인
                - 병해 예방 관리
                """;
            case "봄" -> """
                🌾 보리 봄철 관리 가이드
                
                🌸 수잉기 관리:
                - 추비 시용 (3월)
                - 병해충 방제 강화
                - 도복 방지 관리
                
                📈 품질 향상:
                - 단백질 함량 관리
                - 적정 수분 공급
                """;
            case "여름" -> """
                🌾 보리 여름철 관리 가이드
                
                🍂 수확 관리:
                - 6월 수확 적기
                - 수분 함량 14% 이하
                - 맑은 날 수확 작업
                
                📦 건조 저장:
                - 충분한 자연 건조
                - 통풍 양호한 곳 저장
                """;
            default -> "보리 관리 가이드를 확인하세요.";
        };
    }

    private String generateProfitTipFromPrices(List<PriceInfo> priceInfos, UserPreference preference) {
        StringBuilder tip = new StringBuilder();
        tip.append("📊 오늘의 시장 가격 분석\n\n");

        for (PriceInfo price : priceInfos) {
            if (price.isSignificantChange()) {
                tip.append(String.format("🚨 %s: %s\n",
                        price.getCropName(), price.getTradeRecommendation()));
                tip.append(String.format("   현재가: %s (%s)\n\n",
                        price.getFormattedPrice(), price.getPriceChangeDescription()));
            }
        }

        if (tip.length() == 25) { // 헤더만 있는 경우
            tip.append("💹 현재 시장은 전반적으로 안정적입니다.\n")
                    .append("정상적인 출하 계획을 유지하세요.");
        }

        return tip.toString();
    }

    private boolean shouldGeneratePestAlert() {
        // 계절별 또는 랜덤하게 병해충 경보 생성 여부 결정
        return Math.random() < 0.3; // 30% 확률
    }

    private boolean shouldGenerateLaborTip() {
        // 랜덤하게 일손 매칭 팁 생성 여부 결정
        return Math.random() < 0.4; // 40% 확률
    }

    private boolean shouldGenerateAutomationTip() {
        // 랜덤하게 자동화 제안 팁 생성 여부 결정
        return Math.random() < 0.2; // 20% 확률
    }

    private AiTip createSeasonalPestAlert(User user, UserPreference preference) {
        String season = getCurrentSeason();
        String[] seasonalPests = getSeasonalPests(season);
        String selectedPest = seasonalPests[(int) (Math.random() * seasonalPests.length)];

        return AiTip.builder()
                .user(user)
                .tipType(AiTip.TipType.PEST_ALERT)
                .title("🚨 병해충 조기 경보")
                .content(selectedPest)
                .targetDate(LocalDate.now())
                .priorityLevel(4)
                .isRead(false)
                .build();
    }

    private String[] getSeasonalPests(String season) {
        return switch (season) {
            case "봄" -> new String[]{
                    """
                🐛 진딧물 발생 주의보
                
                📍 발생 지역: 제주 전역
                📅 예상 기간: 3-5월
                🎯 주요 피해 작물: 감귤, 채소류
                
                🛡️ 예방 방법:
                - 끈끈이 트랩 설치
                - 천적 곤충 활용
                - 초기 발견 시 즉시 방제
                
                📞 문의: 제주농업기술센터 064-760-7000
                """,
                    """
                🍄 잿빛곰팡이병 주의보
                
                📍 발생 조건: 다습한 환경
                📅 주의 기간: 봄철 전체
                🎯 주요 피해: 시설재배 작물
                
                🛡️ 예방 조치:
                - 적정 환기 유지
                - 과습 방지
                - 예방 약제 살포
                """
            };
            case "여름" -> new String[]{
                    """
                🕷️ 응애류 대발생 경보
                
                📍 발생 지역: 감귤원 중심
                📅 위험 기간: 6-8월
                🌡️ 발생 조건: 고온 건조
                
                🛡️ 긴급 방제:
                - 살비제 즉시 살포
                - 잎 뒷면 중점 방제
                - 천적 응애 방사 고려
                
                ⚠️ 방제 적기를 놓치면 심각한 피해!
                """,
                    """
                🦗 매미나방 유충 주의보
                
                📍 발생 지역: 산간 농가
                📅 활동 시기: 7-9월
                🎯 피해 작물: 과수, 산채류
                
                🛡️ 방제 방법:
                - 페로몬 트랩 설치
                - 생물학적 방제제 활용
                - 유충 발견 시 즉시 제거
                """
            };
            case "가을" -> new String[]{
                    """
                🐛 파밤나방 발생 주의보
                
                📍 발생 지역: 노지 채소밭
                📅 피해 시기: 9-11월
                🎯 주요 작물: 무, 배추, 당근
                
                🛡️ 종합 방제:
                - 성페로몬 트랩 설치
                - 토양 처리제 시용
                - 윤작으로 발생 억제
                
                💡 조기 발견이 방제의 핵심!
                """,
                    """
                🍄 노균병 확산 경보
                
                📍 위험 지역: 시설재배지
                📅 발생 조건: 일교차 큰 시기
                🎯 피해 작물: 오이, 상추 등
                
                🛡️ 예방 관리:
                - 환기 철저히
                - 예방 위주 방제
                - 감염 잎 즉시 제거
                """
            };
            case "겨울" -> new String[]{
                    """
                🦠 궤양병 월동 관리
                
                📍 관리 대상: 감귤원
                📅 관리 시기: 12-2월
                🎯 예방 중점: 동상해 부위
                
                🛡️ 월동 관리:
                - 동상해 부위 치료
                - 구리 계통 약제 도포
                - 내년 전염원 차단
                
                🔄 내년 피해 예방이 목표!
                """,
                    """
                ❄️ 한해 대비 작물 보호
                
                📍 보호 대상: 전체 작물
                📅 위험 기간: 한파 시기
                🌡️ 주의 온도: -5°C 이하
                
                🛡️ 보호 조치:
                - 보온재 피복
                - 방풍망 설치
                - 가온 시설 점검
                """
            };
            default -> new String[]{"계절별 병해충 정보를 확인하세요."};
        };
    }

    /**
     * 랜덤 일손 매칭 팁 생성
     */
    private AiTip createRandomLaborMatchingTip(User user, UserPreference preference) {
        String[] laborTips = {
                """
            🎯 이번 주 추천 일손 정보
            
            📍 제주시 애월읍 감귤농장
            - 작업 내용: 감귤 수확 및 선별
            - 일급: 12만원 (8시간 기준)
            - 경력: 농업 경험자 우대
            - 연락처: 010-1234-5678
            
            💡 작업 효율 120% 달성한 베테랑 팀입니다!
            """,
                """
            🚜 급구! 당근 수확 일손
            
            📍 서귀포시 성산읍 당근밭
            - 작업 내용: 당근 수확 및 세척
            - 일급: 10만원 (7시간 기준)
            - 기간: 3일간 단기 작업
            - 특이사항: 중식 제공
            
            ⭐ 신규 일꾼도 환영합니다!
            """,
                """
            🌱 대학생 환영! 농장 일손 모집
            
            📍 제주시 한림읍 브로콜리 농장
            - 작업 내용: 브로콜리 수확 및 포장
            - 시급: 12,000원
            - 시간: 오전 6시~12시 (6시간)
            - 혜택: 농산물 무료 제공
            
            🎓 대학생 아르바이트 적극 환영!
            """,
                """
            🏆 숙련 일손 우대 모집
            
            📍 서귀포시 남원읍 감자밭
            - 작업 내용: 감자 수확 및 선별
            - 일급: 15만원 (숙련자 기준)
            - 조건: 농기계 운전 가능자
            - 기간: 1주일 집중 작업
            
            💰 실력에 따라 추가 수당 지급!
            """
        };

        String selectedTip = laborTips[(int) (Math.random() * laborTips.length)];

        return AiTip.builder()
                .user(user)
                .tipType(AiTip.TipType.LABOR_MATCHING)
                .title("🎯 일손 매칭 추천")
                .content(selectedTip)
                .targetDate(LocalDate.now())
                .priorityLevel(2)
                .isRead(false)
                .build();
    }

    /**
     * 랜덤 자동화 제안 팁 생성
     */
    private AiTip createRandomAutomationTip(User user, UserPreference preference) {
        String[] automationTips = {
                """
            ⚡ 자동 관수 시스템 도입 제안
            
            💰 투자 분석:
            - 초기 비용: 300만원 (0.5ha 기준)
            - 절약 효과: 월 40만원 (인건비 + 물값)
            - 회수 기간: 7.5개월
            
            📈 기대 효과:
            - 물 사용량 30% 절약
            - 균일한 수분 공급으로 품질 향상
            - 야간·주말 무인 관리 가능
            
            🔧 추천 업체: 제주스마트팜(064-XXX-XXXX)
            """,
                """
            🌡️ 스마트 온실 환경 제어 시스템
            
            💰 비용 분석:
            - 설치 비용: 500만원 (200평 기준)
            - 연간 절약: 180만원 (에너지비 + 인건비)
            - ROI: 2.8년
            
            📊 핵심 기능:
            - 온도·습도 자동 조절
            - 환기팬 스마트 제어
            - 모바일 원격 모니터링
            
            🎯 수확량 15% 증대 효과!
            """,
                """
            🤖 AI 병해충 진단 시스템
            
            💰 투자 정보:
            - 장비 비용: 150만원
            - 월 이용료: 5만원
            - 방제비 절약: 월 25만원
            
            🔍 주요 기능:
            - 실시간 병해충 모니터링
            - 조기 진단으로 피해 최소화
            - 맞춤형 방제 솔루션 제공
            
            ✅ 방제 효율 200% 향상!
            """,
                """
            📱 드론 방제 서비스 도입
            
            💰 비용 효율성:
            - 기존 방제비: ha당 8만원
            - 드론 방제비: ha당 5만원
            - 절약 효과: 37.5%
            
            ⏰ 시간 절약:
            - 기존 방제: 하루 1ha
            - 드론 방제: 하루 10ha
            - 시간 단축: 90%
            
            🎯 정밀 방제로 약제 사용량 50% 감소!
            """
        };

        String selectedTip = automationTips[(int) (Math.random() * automationTips.length)];

        return AiTip.builder()
                .user(user)
                .tipType(AiTip.TipType.AUTOMATION_SUGGESTION)
                .title("⚡ 스마트팜 자동화 제안")
                .content(selectedTip)
                .targetDate(LocalDate.now())
                .priorityLevel(1)
                .isRead(false)
                .build();
    }

    private String getCurrentSeason() {
        int month = LocalDate.now().getMonthValue();
        return switch (month) {
            case 3, 4, 5 -> "봄";
            case 6, 7, 8 -> "여름";
            case 9, 10, 11 -> "가을";
            default -> "겨울";
        };
    }

    private List<AiTip> createFallbackTips(User user, UserPreference preference) {
        List<AiTip> fallbackTips = new ArrayList<>();

        // 기본 날씨 팁
        fallbackTips.add(createDummyWeatherTip(user));

        // 기본 작물 팁
        if (preference != null && !preference.getPrimaryCropsList().isEmpty()) {
            fallbackTips.add(createDummyCropGuideTip(user, preference.getPrimaryCropsList().get(0)));
        }

        // 기본 수익 팁
        fallbackTips.add(createDummyProfitTip(user));

        return fallbackTips;
    }

    // === 폴백용 더미 팁 생성 메서드들 ===

    private AiTip createDummyWeatherTip(User user) {
        return AiTip.builder()
                .user(user)
                .tipType(AiTip.TipType.WEATHER_ALERT)
                .title("🌡️ 오늘 날씨 주의사항")
                .content("오늘 최고온도가 30°C까지 올라갈 예정입니다. 오전 7시 전에 물주기를 완료하고, 오후 2-4시는 야외 작업을 피하세요.")
                .targetDate(LocalDate.now())
                .weatherCondition("고온")
                .priorityLevel(4)
                .isRead(false)
                .build();
    }

    private AiTip createDummyCropGuideTip(User user, String cropName) {
        return AiTip.builder()
                .user(user)
                .tipType(AiTip.TipType.CROP_GUIDE)
                .title("🌱 " + cropName + " 생육 관리 팁")
                .content(cropName + " 재배 시 이 시기에는 질소 비료를 줄이고 인산·칼륨 비료를 늘려주세요. 수분 관리에도 신경써주세요.")
                .targetDate(LocalDate.now())
                .cropType(cropName)
                .priorityLevel(3)
                .isRead(false)
                .build();
    }

    private AiTip createDummyProfitTip(User user) {
        return AiTip.builder()
                .user(user)
                .tipType(AiTip.TipType.PROFIT_TIP)
                .title("📈 수익 최적화 제안")
                .content("현재 시장 상황을 보면 출하 시기를 1주 늦추면 kg당 평균 200원 더 받을 수 있습니다.")
                .targetDate(LocalDate.now())
                .priorityLevel(2)
                .isRead(false)
                .build();
    }

    private NotificationListDto.NotificationItem convertToNotificationItem(AiTip tip) {
        return NotificationListDto.NotificationItem.builder()
                .id(tip.getId())
                .type(tip.getTipType().name())
                .icon(getTipTypeIcon(tip.getTipType()))
                .title(tip.getTitle())
                .summary(truncateForNotification(tip.getContent()))
                .priority(tip.getPriorityLevel())
                .isRead(tip.getIsRead())
                .cropType(tip.getCropType())
                .createdAt(tip.getCreatedAt())
                .targetDate(tip.getTargetDate())
                .build();
    }

    private String getTipTypeIcon(AiTip.TipType tipType) {
        return switch (tipType) {
            case WEATHER_ALERT -> "🌡️";
            case CROP_GUIDE -> "🌱";
            case PEST_ALERT -> "🚨";
            case PROFIT_TIP -> "📊";
            case AUTOMATION_SUGGESTION -> "⚡";
            case LABOR_MATCHING -> "🎯";
        };
    }

    private String truncateForNotification(String content) {
        if (content == null) {
            return "";
        }

        // 첫 번째 줄만 추출하거나 50자로 제한
        String[] lines = content.split("\\n");
        String firstLine = lines.length > 0 ? lines[0] : content;

        // 이모티콘 제거 (간단하게)
        String cleanContent = firstLine.replaceAll("[🌡️🌱🚨📊⚡🎯🍊🥕🥔🌾🔥☔✅💧🌿🍂📦❄️🐛🍄💰📍📅⚠️📈📏💚🌸💡🔧🛡️📞🔄]", "")
                .trim();

        if (cleanContent.length() <= 50) {
            return cleanContent;
        }

        return cleanContent.substring(0, 47) + "...";
    }
}
