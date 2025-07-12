package com.jeju_nongdi.jeju_nongdi.dto;

import com.jeju_nongdi.jeju_nongdi.entity.Mentoring;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentoringRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다")
    private String title;

    @NotBlank(message = "설명은 필수입니다")
    @Size(max = 1000, message = "설명은 1000자를 초과할 수 없습니다")
    private String description;

    @NotNull(message = "멘토링 타입은 필수입니다")
    private Mentoring.MentoringType mentoringType;

    @NotNull(message = "카테고리는 필수입니다")
    private Mentoring.Category category;

    @NotNull(message = "경험 수준은 필수입니다")
    private Mentoring.ExperienceLevel experienceLevel;

    @Size(max = 100, message = "희망 지역은 100자를 초과할 수 없습니다")
    private String preferredLocation;

    @Size(max = 200, message = "희망 일정은 200자를 초과할 수 없습니다")
    private String preferredSchedule;

    @Size(max = 20, message = "연락처는 20자를 초과할 수 없습니다")
    private String contactPhone;

    @Email(message = "올바른 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다")
    private String contactEmail;

    // 연락처 정보 유효성 검증
    public boolean isContactInfoValid() {
        return (contactPhone != null && !contactPhone.trim().isEmpty()) ||
               (contactEmail != null && !contactEmail.trim().isEmpty());
    }
}
