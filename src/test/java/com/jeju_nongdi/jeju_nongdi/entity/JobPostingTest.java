package com.jeju_nongdi.jeju_nongdi.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JobPosting 엔티티 테스트")
class JobPostingTest {

    private User testUser;
    private JobPosting jobPosting;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("농부김씨")
                .password("password123")
                .build();

        // 테스트용 일손 모집 공고 생성
        jobPosting = JobPosting.builder()
                .title("감자 수확 일손 구합니다")
                .description("감자 수확을 도와주실 분을 찾습니다.")
                .farmName("제주 감자농장")
                .address("제주시 한림읍")
                .latitude(new BigDecimal("33.123456"))
                .longitude(new BigDecimal("126.123456"))
                .cropType(JobPosting.CropType.POTATO)
                .workType(JobPosting.WorkType.HARVESTING)
                .wages(100000)
                .wageType(JobPosting.WageType.DAILY)
                .workStartDate(LocalDate.now().plusDays(1))
                .workEndDate(LocalDate.now().plusDays(5))
                .recruitmentCount(5)
                .contactPhone("010-1234-5678")
                .contactEmail("farm@example.com")
                .status(JobPosting.JobStatus.ACTIVE)
                .author(testUser)
                .build();
    }

    @Test
    @DisplayName("JobPosting 엔티티가 올바르게 생성된다")
    void createJobPosting() {
        // then
        assertThat(jobPosting).isNotNull();
        assertThat(jobPosting.getTitle()).isEqualTo("감자 수확 일손 구합니다");
        assertThat(jobPosting.getDescription()).isEqualTo("감자 수확을 도와주실 분을 찾습니다.");
        assertThat(jobPosting.getFarmName()).isEqualTo("제주 감자농장");
        assertThat(jobPosting.getAddress()).isEqualTo("제주시 한림읍");
        assertThat(jobPosting.getLatitude()).isEqualTo(new BigDecimal("33.123456"));
        assertThat(jobPosting.getLongitude()).isEqualTo(new BigDecimal("126.123456"));
        assertThat(jobPosting.getCropType()).isEqualTo(JobPosting.CropType.POTATO);
        assertThat(jobPosting.getWorkType()).isEqualTo(JobPosting.WorkType.HARVESTING);
        assertThat(jobPosting.getWages()).isEqualTo(100000);
        assertThat(jobPosting.getWageType()).isEqualTo(JobPosting.WageType.DAILY);
        assertThat(jobPosting.getRecruitmentCount()).isEqualTo(5);
        assertThat(jobPosting.getContactPhone()).isEqualTo("010-1234-5678");
        assertThat(jobPosting.getContactEmail()).isEqualTo("farm@example.com");
        assertThat(jobPosting.getStatus()).isEqualTo(JobPosting.JobStatus.ACTIVE);
        assertThat(jobPosting.getAuthor()).isEqualTo(testUser);
    }

    @Test
    @DisplayName("기본값이 올바르게 설정된다")
    void defaultValues() {
        // given
        JobPosting jobPostingWithDefaults = JobPosting.builder()
                .title("테스트 공고")
                .farmName("테스트 농장")
                .address("테스트 주소")
                .latitude(new BigDecimal("33.0"))
                .longitude(new BigDecimal("126.0"))
                .cropType(JobPosting.CropType.POTATO)
                .workType(JobPosting.WorkType.HARVESTING)
                .wages(100000)
                .workStartDate(LocalDate.now())
                .workEndDate(LocalDate.now().plusDays(1))
                .recruitmentCount(1)
                .author(testUser)
                .build();

        // then
        assertThat(jobPostingWithDefaults.getWageType()).isEqualTo(JobPosting.WageType.DAILY);
        assertThat(jobPostingWithDefaults.getStatus()).isEqualTo(JobPosting.JobStatus.ACTIVE);
        assertThat(jobPostingWithDefaults.getCreatedAt()).isNotNull();
        assertThat(jobPostingWithDefaults.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("@PreUpdate 어노테이션이 정상 동작한다")
    void preUpdateAnnotation() {
        // given
        LocalDateTime originalUpdatedAt = jobPosting.getUpdatedAt();

        // when
        try {
            Thread.sleep(1); // 시간 차이를 위한 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        jobPosting.onUpdate();

        // then
        assertThat(jobPosting.getUpdatedAt()).isAfter(originalUpdatedAt);
    }

    @Test
    @DisplayName("CropType enum의 한국어 이름이 올바르게 반환된다")
    void cropTypeKoreanName() {
        // then
        assertThat(JobPosting.CropType.POTATO.getKoreanName()).isEqualTo("감자");
        assertThat(JobPosting.CropType.BARLEY.getKoreanName()).isEqualTo("보리");
        assertThat(JobPosting.CropType.CABBAGE.getKoreanName()).isEqualTo("양배추");
        assertThat(JobPosting.CropType.CARROT.getKoreanName()).isEqualTo("당근");
        assertThat(JobPosting.CropType.ONION.getKoreanName()).isEqualTo("양파");
        assertThat(JobPosting.CropType.GARLIC.getKoreanName()).isEqualTo("마늘");
        assertThat(JobPosting.CropType.RADISH.getKoreanName()).isEqualTo("무");
        assertThat(JobPosting.CropType.LETTUCE.getKoreanName()).isEqualTo("상추");
        assertThat(JobPosting.CropType.SPINACH.getKoreanName()).isEqualTo("시금치");
        assertThat(JobPosting.CropType.TANGERINE.getKoreanName()).isEqualTo("귤");
        assertThat(JobPosting.CropType.OTHER.getKoreanName()).isEqualTo("기타");
    }

    @Test
    @DisplayName("WorkType enum의 한국어 이름이 올바르게 반환된다")
    void workTypeKoreanName() {
        // then
        assertThat(JobPosting.WorkType.PLANTING.getKoreanName()).isEqualTo("파종/심기");
        assertThat(JobPosting.WorkType.WEEDING.getKoreanName()).isEqualTo("잡초제거");
        assertThat(JobPosting.WorkType.HARVESTING.getKoreanName()).isEqualTo("수확");
        assertThat(JobPosting.WorkType.PACKING.getKoreanName()).isEqualTo("포장");
        assertThat(JobPosting.WorkType.PRUNING.getKoreanName()).isEqualTo("가지치기");
        assertThat(JobPosting.WorkType.FERTILIZING.getKoreanName()).isEqualTo("시비");
        assertThat(JobPosting.WorkType.WATERING.getKoreanName()).isEqualTo("물주기");
        assertThat(JobPosting.WorkType.OTHER.getKoreanName()).isEqualTo("기타");
    }

    @Test
    @DisplayName("WageType enum의 한국어 이름이 올바르게 반환된다")
    void wageTypeKoreanName() {
        // then
        assertThat(JobPosting.WageType.HOURLY.getKoreanName()).isEqualTo("시급");
        assertThat(JobPosting.WageType.DAILY.getKoreanName()).isEqualTo("일급");
        assertThat(JobPosting.WageType.MONTHLY.getKoreanName()).isEqualTo("월급");
    }

    @Test
    @DisplayName("JobStatus enum의 한국어 이름이 올바르게 반환된다")
    void jobStatusKoreanName() {
        // then
        assertThat(JobPosting.JobStatus.ACTIVE.getKoreanName()).isEqualTo("모집중");
        assertThat(JobPosting.JobStatus.CLOSED.getKoreanName()).isEqualTo("모집완료");
        assertThat(JobPosting.JobStatus.CANCELLED.getKoreanName()).isEqualTo("취소됨");
    }

    @Test
    @DisplayName("작업 시작일이 종료일보다 이전이어야 한다")
    void workStartDateShouldBeBeforeEndDate() {
        // given
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(5);

        // when
        jobPosting.setWorkStartDate(startDate);
        jobPosting.setWorkEndDate(endDate);

        // then
        assertThat(jobPosting.getWorkStartDate()).isBefore(jobPosting.getWorkEndDate());
    }

    @Test
    @DisplayName("연락처 정보가 선택적으로 설정된다")
    void contactInfoIsOptional() {
        // given
        JobPosting jobPostingWithoutContact = JobPosting.builder()
                .title("연락처 없는 공고")
                .farmName("농장명")
                .address("주소")
                .latitude(new BigDecimal("33.0"))
                .longitude(new BigDecimal("126.0"))
                .cropType(JobPosting.CropType.POTATO)
                .workType(JobPosting.WorkType.HARVESTING)
                .wages(100000)
                .workStartDate(LocalDate.now())
                .workEndDate(LocalDate.now().plusDays(1))
                .recruitmentCount(1)
                .author(testUser)
                .build();

        // then
        assertThat(jobPostingWithoutContact.getContactPhone()).isNull();
        assertThat(jobPostingWithoutContact.getContactEmail()).isNull();
    }
}