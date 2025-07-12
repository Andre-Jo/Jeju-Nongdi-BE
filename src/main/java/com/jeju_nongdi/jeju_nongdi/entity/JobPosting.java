package com.jeju_nongdi.jeju_nongdi.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_postings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "farm_name", nullable = false)
    private String farmName;

    @Column(nullable = false)
    private String address;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude; // 위도

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude; // 경도

    @Enumerated(EnumType.STRING)
    @Column(name = "crop_type", nullable = false)
    private CropType cropType; // 수확 작물

    @Enumerated(EnumType.STRING)
    @Column(name = "work_type", nullable = false)
    private WorkType workType; // 근무 내용

    @Column(nullable = false)
    private Integer wages; // 임금

    @Enumerated(EnumType.STRING)
    @Column(name = "wage_type", nullable = false)
    @Builder.Default
    private WageType wageType = WageType.DAILY;

    @Column(name = "work_start_date", nullable = false)
    private LocalDate workStartDate;

    @Column(name = "work_end_date", nullable = false)
    private LocalDate workEndDate;

    @Column(name = "recruitment_count", nullable = false)
    private Integer recruitmentCount;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private JobStatus status = JobStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum CropType {
        POTATO("감자"),
        BARLEY("보리"),
        CABBAGE("양배추"),
        CARROT("당근"),
        ONION("양파"),
        GARLIC("마늘"),
        RADISH("무"),
        LETTUCE("상추"),
        SPINACH("시금치"),
        TANGERINE("귤"),
        OTHER("기타");

        private final String koreanName;

        CropType(String koreanName) {
            this.koreanName = koreanName;
        }

        public String getKoreanName() {
            return koreanName;
        }
    }

    public enum WorkType {
        PLANTING("파종/심기"),
        WEEDING("잡초제거"),
        HARVESTING("수확"),
        PACKING("포장"),
        PRUNING("가지치기"),
        FERTILIZING("시비"),
        WATERING("물주기"),
        OTHER("기타");

        private final String koreanName;

        WorkType(String koreanName) {
            this.koreanName = koreanName;
        }

        public String getKoreanName() {
            return koreanName;
        }
    }

    public enum WageType {
        HOURLY("시급"),
        DAILY("일급"),
        MONTHLY("월급");

        private final String koreanName;

        WageType(String koreanName) {
            this.koreanName = koreanName;
        }

        public String getKoreanName() {
            return koreanName;
        }
    }

    public enum JobStatus {
        ACTIVE("모집중"),
        CLOSED("모집완료"),
        CANCELLED("취소됨");

        private final String koreanName;

        JobStatus(String koreanName) {
            this.koreanName = koreanName;
        }

        public String getKoreanName() {
            return koreanName;
        }
    }
}
