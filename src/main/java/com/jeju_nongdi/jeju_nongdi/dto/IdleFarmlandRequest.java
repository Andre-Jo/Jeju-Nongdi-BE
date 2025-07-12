package com.jeju_nongdi.jeju_nongdi.dto;

import com.jeju_nongdi.jeju_nongdi.entity.IdleFarmland;
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
public class IdleFarmlandRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자 이내여야 합니다")
    private String title;

    @Size(max = 1000, message = "설명은 1000자 이내여야 합니다")
    private String description;

    @NotBlank(message = "농지명은 필수입니다")
    @Size(max = 100, message = "농지명은 100자 이내여야 합니다")
    private String farmlandName;

    @NotBlank(message = "주소는 필수입니다")
    @Size(max = 200, message = "주소는 200자 이내여야 합니다")
    private String address;

    @NotNull(message = "위도는 필수입니다")
    @DecimalMin(value = "33.0", message = "유효하지 않은 위도입니다")
    @DecimalMax(value = "34.0", message = "유효하지 않은 위도입니다")
    private BigDecimal latitude;

    @NotNull(message = "경도는 필수입니다")
    @DecimalMin(value = "126.0", message = "유효하지 않은 경도입니다")
    @DecimalMax(value = "127.0", message = "유효하지 않은 경도입니다")
    private BigDecimal longitude;

    @NotNull(message = "면적은 필수입니다")
    @DecimalMin(value = "1.0", message = "면적은 1㎡ 이상이어야 합니다")
    @DecimalMax(value = "999999.0", message = "면적이 너무 큽니다")
    private BigDecimal areaSize;

    private IdleFarmland.SoilType soilType;

    @NotNull(message = "이용 유형은 필수입니다")
    private IdleFarmland.UsageType usageType;

    @Min(value = 0, message = "임대료는 0 이상이어야 합니다")
    @Max(value = 99999999, message = "임대료가 너무 큽니다")
    private Integer monthlyRent;

    private LocalDate availableStartDate;

    private LocalDate availableEndDate;

    private Boolean waterSupply;

    private Boolean electricitySupply;

    private Boolean farmingToolsIncluded;

    @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "올바른 전화번호 형식이 아닙니다")
    private String contactPhone;

    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String contactEmail;

    // 유효성 검증 메서드
    @AssertTrue(message = "연락처 정보(전화번호 또는 이메일) 중 하나는 필수입니다")
    public boolean isContactInfoValid() {
        return (contactPhone != null && !contactPhone.trim().isEmpty()) ||
               (contactEmail != null && !contactEmail.trim().isEmpty());
    }

    @AssertTrue(message = "종료일은 시작일보다 늦어야 합니다")
    public boolean isDateRangeValid() {
        if (availableStartDate == null || availableEndDate == null) {
            return true; // null 체크는 다른 어노테이션에서 처리
        }
        return !availableEndDate.isBefore(availableStartDate);
    }
}
