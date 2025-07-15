package com.jeju_nongdi.jeju_nongdi.dto.ai;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "알림 리스트 DTO")
public class NotificationListDto {
    
    @Schema(description = "전체 알림 개수")
    private Long totalCount;
    
    @Schema(description = "읽지 않은 알림 개수")
    private Integer unreadCount;
    
    @Schema(description = "현재 페이지")
    private Integer currentPage;
    
    @Schema(description = "전체 페이지 수")
    private Integer totalPages;
    
    @Schema(description = "알림 리스트")
    private List<NotificationItem> notifications;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "알림 항목")
    public static class NotificationItem {
        
        @Schema(description = "알림 ID")
        private Long id;
        
        @Schema(description = "알림 유형")
        private String type;
        
        @Schema(description = "아이콘")
        private String icon;
        
        @Schema(description = "제목")
        private String title;
        
        @Schema(description = "내용 요약")
        private String summary;
        
        @Schema(description = "우선순위")
        private Integer priority;
        
        @Schema(description = "읽음 여부")
        private Boolean isRead;
        
        @Schema(description = "작물 유형")
        private String cropType;
        
        @Schema(description = "생성 일시")
        private LocalDateTime createdAt;
        
        @Schema(description = "대상 날짜")
        private LocalDate targetDate;
        
        @Schema(description = "우선순위 라벨")
        public String getPriorityLabel() {
            return switch (priority) {
                case 4 -> "긴급";
                case 3 -> "높음";
                case 2 -> "보통";
                case 1 -> "낮음";
                default -> "일반";
            };
        }
        
        @Schema(description = "우선순위 색상")
        public String getPriorityColor() {
            return switch (priority) {
                case 4 -> "#ff4444"; // 빨강
                case 3 -> "#ff8800"; // 주황
                case 2 -> "#4488ff"; // 파랑
                case 1 -> "#888888"; // 회색
                default -> "#666666"; // 진회색
            };
        }
    }
}
