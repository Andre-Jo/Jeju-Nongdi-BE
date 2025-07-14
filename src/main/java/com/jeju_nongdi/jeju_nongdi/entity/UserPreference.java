package com.jeju_nongdi.jeju_nongdi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class UserPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "primary_crops")
    private String primaryCrops; // JSON 형태로 저장: ["감귤", "보리", "감자"]
    
    @Column(name = "farm_location")
    private String farmLocation; // 농장 위치 (시/군/구)
    
    @Column(name = "farm_size")
    private Double farmSize; // 농장 크기 (평방미터)
    
    @Column(name = "farming_experience")
    private Integer farmingExperience; // 농업 경력 (년)
    
    @Column(name = "notification_weather")
    private Boolean notificationWeather = true;
    
    @Column(name = "notification_pest")
    private Boolean notificationPest = true;
    
    @Column(name = "notification_market")
    private Boolean notificationMarket = true;
    
    @Column(name = "notification_labor")
    private Boolean notificationLabor = true;
    
    @Column(name = "preferred_tip_time")
    private String preferredTipTime = "08:00"; // 알림 받을 시간
    
    @Enumerated(EnumType.STRING)
    @Column(name = "farming_type")
    private FarmingType farmingType;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum FarmingType {
        TRADITIONAL("전통농업"),
        ORGANIC("유기농업"),
        SMART_FARM("스마트팜"),
        GREENHOUSE("시설농업");
        
        private final String description;
        
        FarmingType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 작물 리스트를 다루는 헬퍼 메서드들
    public List<String> getPrimaryCropsList() {
        if (primaryCrops == null || primaryCrops.isEmpty()) {
            return List.of();
        }
        // 간단한 파싱 (나중에 JSON 라이브러리 사용할 수 있음)
        return List.of(primaryCrops.replaceAll("[\\[\\]\"]", "").split(","));
    }
    
    public void setPrimaryCropsList(List<String> crops) {
        if (crops == null || crops.isEmpty()) {
            this.primaryCrops = "[]";
        } else {
            this.primaryCrops = "[\"" + String.join("\",\"", crops) + "\"]";
        }
    }
}
