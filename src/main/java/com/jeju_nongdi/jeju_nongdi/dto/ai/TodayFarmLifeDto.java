package com.jeju_nongdi.jeju_nongdi.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "오늘의 농살 - 메인 화면용 DTO")
public class TodayFarmLifeDto {
    
    @Schema(description = "날짜")
    private LocalDate date;
    
    @Schema(description = "날씨 요약")
    private String weatherSummary;
    
    @Schema(description = "오늘의 메인 팁")
    private MainTipInfo mainTip;
    
    @Schema(description = "긴급 알림 개수")
    private Integer urgentCount;
    
    @Schema(description = "전체 팁 개수")
    private Integer totalTipCount;
    
    @Schema(description = "읽지 않은 팁 개수")
    private Integer unreadCount;
    
    @Schema(description = "오늘의 할 일")
    private List<String> todayTasks;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "메인 팁 정보")
    public static class MainTipInfo {
        
        @Schema(description = "팁 ID")
        private Long tipId;
        
        @Schema(description = "팁 유형")
        private String tipType;
        
        @Schema(description = "아이콘")
        private String icon;
        
        @Schema(description = "제목")
        private String title;
        
        @Schema(description = "내용 요약 (최대 100자)")
        private String summary;
        
        @Schema(description = "우선순위")
        private Integer priority;
        
        @Schema(description = "작물 유형")
        private String cropType;
    }
}
