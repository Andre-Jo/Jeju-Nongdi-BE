package com.jeju_nongdi.jeju_nongdi.dto;

import com.jeju_nongdi.jeju_nongdi.entity.Mentoring;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentoringResponse {

    private Long id;
    private String title;
    private String description;
    private Mentoring.MentoringType mentoringType;
    private String mentoringTypeName;
    private Mentoring.Category category;
    private String categoryName;
    private Mentoring.ExperienceLevel experienceLevel;
    private String experienceLevelName;
    private String preferredLocation;
    private String preferredSchedule;
    private String contactPhone;
    private String contactEmail;
    private Mentoring.MentoringStatus status;
    private String statusName;
    private UserResponse author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 테스트 호환성을 위한 편의 메서드들
    public String getContent() {
        return this.description;
    }
    
    public void setContent(String content) {
        this.description = content;
    }

    public String getLocation() {
        return this.preferredLocation;
    }
    
    public void setLocation(String location) {
        this.preferredLocation = location;
    }

    public String getAuthorName() {
        return this.author != null ? this.author.getName() : null;
    }

    // Entity에서 Response로 변환하는 static factory method
    public static MentoringResponse from(Mentoring mentoring) {
        return MentoringResponse.builder()
                .id(mentoring.getId())
                .title(mentoring.getTitle())
                .description(mentoring.getDescription())
                .mentoringType(mentoring.getMentoringType())
                .mentoringTypeName(mentoring.getMentoringType().getKoreanName())
                .category(mentoring.getCategory())
                .categoryName(mentoring.getCategory().getKoreanName())
                .experienceLevel(mentoring.getExperienceLevel())
                .experienceLevelName(mentoring.getExperienceLevel().getKoreanName())
                .preferredLocation(mentoring.getPreferredLocation())
                .preferredSchedule(mentoring.getPreferredSchedule())
                .contactPhone(mentoring.getContactPhone())
                .contactEmail(mentoring.getContactEmail())
                .status(mentoring.getStatus())
                .statusName(mentoring.getStatus().getKoreanName())
                .author(UserResponse.from(mentoring.getUser()))
                .createdAt(mentoring.getCreatedAt())
                .updatedAt(mentoring.getUpdatedAt())
                .build();
    }
}
