package com.jeju_nongdi.jeju_nongdi.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyTipSummaryDto {
    
    private LocalDate targetDate;
    private String weatherSummary; // 날씨 요약
    private Integer totalTips; // 전체 팁 개수
    private Integer unreadTips; // 읽지 않은 팁 개수
    private Integer urgentTips; // 긴급 팁 개수
    private List<AiTipResponseDto> tips; // 팁 목록
    private List<String> todayTasks; // 오늘 해야 할 주요 농업 작업들
    
    // 팁 통계 정보
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TipStatistics {
        private Integer weatherAlerts;
        private Integer cropGuides;
        private Integer pestAlerts;
        private Integer profitTips;
        private Integer automationSuggestions;
        private Integer laborRecommendations;
    }
    
    private TipStatistics statistics;
}
