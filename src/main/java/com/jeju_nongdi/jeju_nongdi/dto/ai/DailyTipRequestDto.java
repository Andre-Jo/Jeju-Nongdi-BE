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
public class DailyTipRequestDto {
    
    private LocalDate targetDate; // 조회할 날짜 (기본값: 오늘)
    private List<String> tipTypes; // 원하는 팁 유형들 (null이면 모든 유형)
    private String cropType; // 특정 작물 관련 팁만 (선택사항)
    private Integer priorityLevel; // 최소 우선순위 (1 이상의 팁만)
    private Boolean onlyUnread; // 읽지 않은 팁만 조회할지 여부
    
    // 기본값 설정을 위한 정적 메서드
    public static DailyTipRequestDto getDefault() {
        return DailyTipRequestDto.builder()
                .targetDate(LocalDate.now())
                .onlyUnread(false)
                .priorityLevel(1)
                .build();
    }
}
