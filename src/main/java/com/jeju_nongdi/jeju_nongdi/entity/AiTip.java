package com.jeju_nongdi.jeju_nongdi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_tips")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AiTip {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipType tipType;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;
    
    @Column(name = "crop_type")
    private String cropType;
    
    @Column(name = "weather_condition")
    private String weatherCondition;
    
    @Column(name = "priority_level")
    private Integer priorityLevel = 1; // 1: 낮음, 2: 보통, 3: 높음, 4: 긴급
    
    @Column(name = "is_read")
    private Boolean isRead = false;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    public enum TipType {
        WEATHER_ALERT("날씨 기반 알림"),
        CROP_GUIDE("작물별 생육 가이드"),
        PEST_ALERT("병해충 경보"),
        PROFIT_TIP("수익 최적화 팁"),
        AUTOMATION_SUGGESTION("자동화 제안"),
        LABOR_MATCHING("일손 매칭 추천");
        
        private final String description;
        
        TipType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
