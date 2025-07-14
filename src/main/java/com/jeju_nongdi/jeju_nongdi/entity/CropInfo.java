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

@Entity
@Table(name = "crop_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CropInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String cropName; // 작물명 (감귤, 보리, 감자 등)
    
    @Column(name = "crop_category")
    private String cropCategory; // 작물 분류 (과일, 곡물, 채소 등)
    
    @Column(name = "planting_season")
    private String plantingSeason; // 파종 시기
    
    @Column(name = "harvest_season")
    private String harvestSeason; // 수확 시기
    
    @Column(name = "growth_period")
    private Integer growthPeriod; // 생육 기간 (일)
    
    @Column(name = "optimal_temperature")
    private String optimalTemperature; // 최적 온도 범위
    
    @Column(name = "water_requirement")
    private String waterRequirement; // 물 요구량 (적음/보통/많음)
    
    @Column(name = "common_pests", columnDefinition = "TEXT")
    private String commonPests; // 주요 병해충 (JSON 형태)
    
    @Column(name = "fertilizer_schedule", columnDefinition = "TEXT")
    private String fertilizerSchedule; // 시비 일정 (JSON 형태)
    
    @Column(name = "care_tips", columnDefinition = "TEXT")
    private String careTips; // 관리 요령
    
    @Column(name = "market_info", columnDefinition = "TEXT")
    private String marketInfo; // 시장 정보 및 가격 트렌드
    
    @Column(name = "is_jeju_specialty")
    private Boolean isJejuSpecialty = false; // 제주 특산물 여부
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 현재 시기가 파종철인지 확인하는 메서드
    public boolean isPlantingSeason(int month) {
        if (plantingSeason == null) return false;
        // 간단한 체크 로직 (나중에 더 정교하게 구현)
        return plantingSeason.contains(String.valueOf(month));
    }
    
    // 현재 시기가 수확철인지 확인하는 메서드
    public boolean isHarvestSeason(int month) {
        if (harvestSeason == null) return false;
        return harvestSeason.contains(String.valueOf(month));
    }
}
