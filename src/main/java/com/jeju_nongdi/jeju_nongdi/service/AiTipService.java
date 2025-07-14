package com.jeju_nongdi.jeju_nongdi.service;

import com.jeju_nongdi.jeju_nongdi.dto.ai.AiTipResponseDto;
import com.jeju_nongdi.jeju_nongdi.dto.ai.DailyTipRequestDto;
import com.jeju_nongdi.jeju_nongdi.dto.ai.DailyTipSummaryDto;
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
     * 사용자 맞춤 팁 자동 생성 (더미 구현)
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
        
        // 더미 팁들 생성
        if (preference != null && preference.getNotificationWeather()) {
            newTips.add(createDummyWeatherTip(user));
        }
        
        if (preference != null && !preference.getPrimaryCropsList().isEmpty()) {
            newTips.add(createDummyCropGuideTip(user, preference.getPrimaryCropsList().get(0)));
        }
        
        if (preference != null && preference.getNotificationMarket()) {
            newTips.add(createDummyProfitTip(user));
        }
        
        aiTipRepository.saveAll(newTips);
        log.info("사용자 {}에게 {} 개의 일일 팁을 생성했습니다.", userId, newTips.size());
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
        // TODO: 실제 날씨 API 연동 시 구현
        return "맑음, 최고 25°C / 최저 18°C, 오후에 구름 많음";
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
    
    // 더미 팁 생성 메서드들
    private AiTip createDummyWeatherTip(User user) {
        return AiTip.builder()
                .user(user)
                .tipType(AiTip.TipType.WEATHER_ALERT)
                .title("🌡️ 오늘 고온 주의보")
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
}
