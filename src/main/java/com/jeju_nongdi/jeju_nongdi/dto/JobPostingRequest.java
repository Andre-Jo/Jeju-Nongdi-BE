package com.jeju_nongdi.jeju_nongdi.dto;

import com.jeju_nongdi.jeju_nongdi.entity.JobPosting;
import jakarta.validation.constraints.*;
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
public class JobPostingRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자 이하로 입력해주세요")
    private String title;

    @Size(max = 2000, message = "상세 설명은 2000자 이하로 입력해주세요")
    private String description;

    @NotBlank(message = "농장명은 필수입니다")
    @Size(max = 50, message = "농장명은 50자 이하로 입력해주세요")
    private String farmName;

    @NotBlank(message = "주소는 필수입니다")
    @Size(max = 200, message = "주소는 200자 이하로 입력해주세요")
    private String address;

    @NotNull(message = "위도는 필수입니다")
    @DecimalMin(value = "33.0", message = "올바른 위도를 입력해주세요 (제주도 범위: 33.0~34.0)")
    @DecimalMax(value = "34.0", message = "올바른 위도를 입력해주세요 (제주도 범위: 33.0~34.0)")
    private BigDecimal latitude;

    @NotNull(message = "경도는 필수입니다")
    @DecimalMin(value = "126.0", message = "올바른 경도를 입력해주세요 (제주도 범위: 126.0~127.0)")
    @DecimalMax(value = "127.0", message = "올바른 경도를 입력해주세요 (제주도 범위: 126.0~127.0)")
    private BigDecimal longitude;

    @NotNull(message = "작물 종류는 필수입니다")
    private JobPosting.CropType cropType;

    @NotNull(message = "작업 종류는 필수입니다")
    private JobPosting.WorkType workType;

    @NotNull(message = "급여는 필수입니다")
    @Min(value = 1000, message = "급여는 1,000원 이상이어야 합니다")
    @Max(value = 1000000, message = "급여는 1,000,000원 이하여야 합니다")
    private Integer wages;

    @Builder.Default
    private JobPosting.WageType wageType = JobPosting.WageType.DAILY;

    @NotNull(message = "작업 시작일은 필수입니다")
    @Future(message = "작업 시작일은 미래 날짜여야 합니다")
    private LocalDate workStartDate;

    @NotNull(message = "작업 종료일은 필수입니다")
    @Future(message = "작업 종료일은 미래 날짜여야 합니다")
    private LocalDate workEndDate;

    @NotNull(message = "모집 인원은 필수입니다")
    @Min(value = 1, message = "모집 인원은 1명 이상이어야 합니다")
    @Max(value = 100, message = "모집 인원은 100명 이하여야 합니다")
    private Integer recruitmentCount;

    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다 (예: 010-1234-5678)")
    private String contactPhone;

    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String contactEmail;

    // 커스텀 검증: 종료일이 시작일보다 뒤에 있는지 확인
    @AssertTrue(message = "작업 종료일은 시작일보다 뒤여야 합니다")
    public boolean isWorkEndDateValid() {
        if (workStartDate == null || workEndDate == null) {
            return true; // null 체크는 다른 검증에서 처리
        }
        return workEndDate.isAfter(workStartDate) || workEndDate.isEqual(workStartDate);
    }

    // 커스텀 검증: 연락처 중 하나는 필수
    @AssertTrue(message = "전화번호 또는 이메일 중 하나는 필수입니다")
    public boolean isContactInfoValid() {
        return (contactPhone != null && !contactPhone.trim().isEmpty()) ||
               (contactEmail != null && !contactEmail.trim().isEmpty());
    }
}
