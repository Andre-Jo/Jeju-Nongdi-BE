package com.jeju_nongdi.jeju_nongdi.dto;

import com.jeju_nongdi.jeju_nongdi.entity.JobPosting;
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
public class JobPostingResponse {

    private Long id;
    private String title;
    private String description;
    private String farmName;
    private String address;
    private double latitude;
    private double longitude;
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
    private String contactPhone;
    private String contactEmail;
    private JobPosting.JobStatus status;
    private String statusName;
    private AuthorInfo author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorInfo {
        private Long id;
        private String name;
        private String nickname;
        private String phone;
        private String email;
    }

    // 엔티티에서 DTO로 변환하는 정적 메서드
    public static JobPostingResponse from(JobPosting jobPosting) {
        return JobPostingResponse.builder()
                .id(jobPosting.getId())
                .title(jobPosting.getTitle())
                .description(jobPosting.getDescription())
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
                .contactPhone(jobPosting.getContactPhone())
                .contactEmail(jobPosting.getContactEmail())
                .status(jobPosting.getStatus())
                .statusName(jobPosting.getStatus().getKoreanName())
                .author(AuthorInfo.builder()
                        .id(jobPosting.getAuthor().getId())
                        .name(jobPosting.getAuthor().getName())
                        .nickname(jobPosting.getAuthor().getNickname())
                        .phone(jobPosting.getAuthor().getPhone())
                        .email(jobPosting.getAuthor().getEmail())
                        .build())
                .createdAt(jobPosting.getCreatedAt())
                .updatedAt(jobPosting.getUpdatedAt())
                .build();
    }
}
