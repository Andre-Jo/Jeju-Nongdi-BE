package com.jeju_nongdi.jeju_nongdi.dto;

import com.jeju_nongdi.jeju_nongdi.entity.IdleFarmland;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdleFarmlandResponse {

    private Long id;
    private String title;
    private String description;
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
    private Boolean waterSupply;
    private Boolean electricitySupply;
    private Boolean farmingToolsIncluded;
    private String contactPhone;
    private String contactEmail;
    private IdleFarmland.FarmlandStatus status;
    private String statusName;
    private UserResponse owner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 테스트 호환성을 위한 편의 메서드들
    public BigDecimal getArea() {
        return this.areaSize;
    }
    
    public void setArea(BigDecimal area) {
        this.areaSize = area;
    }

    public Integer getRentPrice() {
        return this.monthlyRent;
    }
    
    public void setRentPrice(Integer rentPrice) {
        this.monthlyRent = rentPrice;
    }

    public String getAdditionalInfo() {
        return this.description;
    }
    
    public void setAdditionalInfo(String additionalInfo) {
        this.description = additionalInfo;
    }

    public String getOwnerName() {
        return this.owner != null ? this.owner.getName() : null;
    }

    // 엔티티에서 DTO로 변환하는 정적 메서드
    public static IdleFarmlandResponse from(IdleFarmland idleFarmland) {
        return IdleFarmlandResponse.builder()
                .id(idleFarmland.getId())
                .title(idleFarmland.getTitle())
                .description(idleFarmland.getDescription())
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
                .waterSupply(idleFarmland.getWaterSupply())
                .electricitySupply(idleFarmland.getElectricitySupply())
                .farmingToolsIncluded(idleFarmland.getFarmingToolsIncluded())
                .contactPhone(idleFarmland.getContactPhone())
                .contactEmail(idleFarmland.getContactEmail())
                .status(idleFarmland.getStatus())
                .statusName(idleFarmland.getStatus().getKoreanName())
                .owner(UserResponse.from(idleFarmland.getOwner()))
                .createdAt(idleFarmland.getCreatedAt())
                .updatedAt(idleFarmland.getUpdatedAt())
                .build();
    }
}
