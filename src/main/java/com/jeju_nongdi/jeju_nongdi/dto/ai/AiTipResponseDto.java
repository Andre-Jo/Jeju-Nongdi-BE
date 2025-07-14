package com.jeju_nongdi.jeju_nongdi.dto.ai;

import com.jeju_nongdi.jeju_nongdi.entity.AiTip;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiTipResponseDto {
    
    private Long id;
    private String tipType;
    private String tipTypeDescription;
    private String title;
    private String content;
    private LocalDate targetDate;
    private String cropType;
    private String weatherCondition;
    private Integer priorityLevel;
    private String priorityLevelText;
    private Boolean isRead;
    private LocalDateTime createdAt;
    
    public static AiTipResponseDto from(AiTip aiTip) {
        return AiTipResponseDto.builder()
                .id(aiTip.getId())
                .tipType(aiTip.getTipType().name())
                .tipTypeDescription(aiTip.getTipType().getDescription())
                .title(aiTip.getTitle())
                .content(aiTip.getContent())
                .targetDate(aiTip.getTargetDate())
                .cropType(aiTip.getCropType())
                .weatherCondition(aiTip.getWeatherCondition())
                .priorityLevel(aiTip.getPriorityLevel())
                .priorityLevelText(getPriorityLevelText(aiTip.getPriorityLevel()))
                .isRead(aiTip.getIsRead())
                .createdAt(aiTip.getCreatedAt())
                .build();
    }
    
    private static String getPriorityLevelText(Integer level) {
        return switch (level) {
            case 1 -> "낮음";
            case 2 -> "보통";
            case 3 -> "높음";
            case 4 -> "긴급";
            default -> "알 수 없음";
        };
    }
}
