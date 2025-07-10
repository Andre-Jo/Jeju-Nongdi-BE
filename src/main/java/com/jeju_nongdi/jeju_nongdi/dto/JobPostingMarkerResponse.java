package com.jeju_nongdi.jeju_nongdi.dto;

import com.jeju_nongdi.jeju_nongdi.entity.JobPosting;
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
public class JobPostingMarkerResponse {

    private Long id;
    private String title;
    private String farmName;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private JobPosting.CropType cropType;
    private String cropTypeName;
    private JobPosting.WorkType workType;
    private String workTypeName;
    private Integer wages;
    private JobPosting.WageType wageType;
    private String wageTypeName;
    private LocalDate workStartDate;
    private LocalDate workEndDate;
    private Integer recruitmentCount;
    private JobPosting.JobStatus status;
    private String statusName;

    // 엔티티에서 마커 DTO로 변환하는 정적 메서드
    public static JobPostingMarkerResponse from(JobPosting jobPosting) {
        return JobPostingMarkerResponse.builder()
                .id(jobPosting.getId())
                .title(jobPosting.getTitle())
                .farmName(jobPosting.getFarmName())
                .address(jobPosting.getAddress())
                .latitude(jobPosting.getLatitude())
                .longitude(jobPosting.getLongitude())
                .cropType(jobPosting.getCropType())
                .cropTypeName(jobPosting.getCropType().getKoreanName())
                .workType(jobPosting.getWorkType())
                .workTypeName(jobPosting.getWorkType().getKoreanName())
                .wages(jobPosting.getWages())
                .wageType(jobPosting.getWageType())
                .wageTypeName(jobPosting.getWageType().getKoreanName())
                .workStartDate(jobPosting.getWorkStartDate())
                .workEndDate(jobPosting.getWorkEndDate())
                .recruitmentCount(jobPosting.getRecruitmentCount())
                .status(jobPosting.getStatus())
                .statusName(jobPosting.getStatus().getKoreanName())
                .build();
    }
}
