package com.jeju_nongdi.jeju_nongdi.dto;

import com.jeju_nongdi.jeju_nongdi.entity.IdleFarmland;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdleFarmlandMarkerResponse {

    private Long id;
    private String title;
    private String farmlandName;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal areaSize;
    private IdleFarmland.SoilType soilType;
    private String soilTypeName;
    private IdleFarmland.UsageType usageType;
    private String usageTypeName;
    private Integer monthlyRent;
    private LocalDate availableStartDate;
    private LocalDate availableEndDate;
    private IdleFarmland.FarmlandStatus status;
    private String statusName;

    // 테스트 호환성을 위한 편의 메서드
    public BigDecimal getArea() {
        return this.areaSize;
    }
    
    public void setArea(BigDecimal area) {
        this.areaSize = area;
    }

    // 엔티티에서 마커 DTO로 변환하는 정적 메서드
    public static IdleFarmlandMarkerResponse from(IdleFarmland idleFarmland) {
        return IdleFarmlandMarkerResponse.builder()
                .id(idleFarmland.getId())
                .title(idleFarmland.getTitle())
                .farmlandName(idleFarmland.getFarmlandName())
                .address(idleFarmland.getAddress())
                .latitude(idleFarmland.getLatitude())
                .longitude(idleFarmland.getLongitude())
                .areaSize(idleFarmland.getAreaSize())
                .soilType(idleFarmland.getSoilType())
                .soilTypeName(idleFarmland.getSoilType() != null ? idleFarmland.getSoilType().getKoreanName() : null)
                .usageType(idleFarmland.getUsageType())
                .usageTypeName(idleFarmland.getUsageType().getKoreanName())
                .monthlyRent(idleFarmland.getMonthlyRent())
                .availableStartDate(idleFarmland.getAvailableStartDate())
                .availableEndDate(idleFarmland.getAvailableEndDate())
                .status(idleFarmland.getStatus())
                .statusName(idleFarmland.getStatus().getKoreanName())
                .build();
    }
}
