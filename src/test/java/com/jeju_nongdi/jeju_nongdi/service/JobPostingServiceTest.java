package com.jeju_nongdi.jeju_nongdi.service;

import com.jeju_nongdi.jeju_nongdi.dto.JobPostingRequest;
import com.jeju_nongdi.jeju_nongdi.dto.JobPostingResponse;
import com.jeju_nongdi.jeju_nongdi.entity.JobPosting;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.repository.JobPostingRepository;
import com.jeju_nongdi.jeju_nongdi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobPostingService 단위 테스트")
class JobPostingServiceTest {

    @Mock
    private JobPostingRepository jobPostingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobPostingService jobPostingService;

    private User testUser;
    private JobPostingRequest testRequest;
    private JobPosting testJobPosting;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .name("테스트 사용자")
                .nickname("테스터")
                .phone("010-1234-5678")
                .build();

        // 테스트용 요청 DTO 생성
        testRequest = JobPostingRequest.builder()
                .title("감자 수확 일손 구합니다")
                .description("건실한 일손을 찾습니다")
                .farmName("제주 감자농장")
                .address("제주특별자치도 제주시 애월읍")
                .latitude(new BigDecimal("33.4996"))
                .longitude(new BigDecimal("126.5312"))
                .cropType(JobPosting.CropType.POTATO)
                .workType(JobPosting.WorkType.HARVESTING)
                .wages(100000)
                .wageType(JobPosting.WageType.DAILY)
                .workStartDate(LocalDate.now().plusDays(7))
                .workEndDate(LocalDate.now().plusDays(14))
                .recruitmentCount(5)
                .contactPhone("010-1234-5678")
                .contactEmail("farmer@example.com")
                .build();

        // 테스트용 엔티티 생성
        testJobPosting = JobPosting.builder()
                .id(1L)
                .title(testRequest.getTitle())
                .description(testRequest.getDescription())
                .farmName(testRequest.getFarmName())
                .address(testRequest.getAddress())
                .latitude(testRequest.getLatitude())
                .longitude(testRequest.getLongitude())
                .cropType(testRequest.getCropType())
                .workType(testRequest.getWorkType())
                .wages(testRequest.getWages())
                .wageType(testRequest.getWageType())
                .workStartDate(testRequest.getWorkStartDate())
                .workEndDate(testRequest.getWorkEndDate())
                .recruitmentCount(testRequest.getRecruitmentCount())
                .contactPhone(testRequest.getContactPhone())
                .contactEmail(testRequest.getContactEmail())
                .author(testUser)
                .status(JobPosting.JobStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("일손 모집 공고 생성 성공")
    void createJobPosting_Success() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.of(testUser));
        given(jobPostingRepository.save(any(JobPosting.class))).willReturn(testJobPosting);

        // when
        JobPostingResponse response = jobPostingService.createJobPosting(testRequest, testUser.getEmail());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo(testRequest.getTitle());
        assertThat(response.getFarmName()).isEqualTo(testRequest.getFarmName());
        assertThat(response.getCropType()).isEqualTo(testRequest.getCropType());
        assertThat(response.getAuthor().getEmail()).isEqualTo(testUser.getEmail());

        then(userRepository).should(times(1)).findByEmail(testUser.getEmail());
        then(jobPostingRepository).should(times(1)).save(any(JobPosting.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 공고 생성 시 예외 발생")
    void createJobPosting_UserNotFound_ThrowsException() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> 
            jobPostingService.createJobPosting(testRequest, "nonexistent@example.com"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("사용자를 찾을 수 없습니다");

        then(userRepository).should(times(1)).findByEmail("nonexistent@example.com");
        then(jobPostingRepository).should(times(0)).save(any(JobPosting.class));
    }

    @Test
    @DisplayName("일손 모집 공고 조회 성공")
    void getJobPosting_Success() {
        // given
        given(jobPostingRepository.findById(1L)).willReturn(Optional.of(testJobPosting));

        // when
        JobPostingResponse response = jobPostingService.getJobPosting(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo(testJobPosting.getTitle());

        then(jobPostingRepository).should(times(1)).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 공고 조회 시 예외 발생")
    void getJobPosting_NotFound_ThrowsException() {
        // given
        given(jobPostingRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> jobPostingService.getJobPosting(999L))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("일손 모집 공고를 찾을 수 없습니다");

        then(jobPostingRepository).should(times(1)).findById(999L);
    }

    @Test
    @DisplayName("일손 모집 공고 수정 성공")
    void updateJobPosting_Success() {
        // given
        given(jobPostingRepository.findById(1L)).willReturn(Optional.of(testJobPosting));
        given(userRepository.findByEmail(testUser.getEmail())).willReturn(Optional.of(testUser));
        given(jobPostingRepository.save(any(JobPosting.class))).willReturn(testJobPosting);

        JobPostingRequest updateRequest = JobPostingRequest.builder()
                .title("수정된 제목")
                .description(testRequest.getDescription())
                .farmName(testRequest.getFarmName())
                .address(testRequest.getAddress())
                .latitude(testRequest.getLatitude())
                .longitude(testRequest.getLongitude())
                .cropType(testRequest.getCropType())
                .workType(testRequest.getWorkType())
                .wages(120000)
                .wageType(testRequest.getWageType())
                .workStartDate(testRequest.getWorkStartDate())
                .workEndDate(testRequest.getWorkEndDate())
                .recruitmentCount(testRequest.getRecruitmentCount())
                .contactPhone(testRequest.getContactPhone())
                .contactEmail(testRequest.getContactEmail())
                .build();

        // when
        JobPostingResponse response = jobPostingService.updateJobPosting(1L, updateRequest, testUser.getEmail());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("수정된 제목");
        assertThat(response.getWages()).isEqualTo(120000);

        then(jobPostingRepository).should(times(1)).findById(1L);
        then(userRepository).should(times(1)).findByEmail(testUser.getEmail());
        then(jobPostingRepository).should(times(1)).save(any(JobPosting.class));
    }

    @Test
    @DisplayName("다른 사용자의 공고 수정 시 예외 발생")
    void updateJobPosting_UnauthorizedUser_ThrowsException() {
        // given
        User anotherUser = User.builder()
                .id(2L)
                .email("another@example.com")
                .build();

        given(jobPostingRepository.findById(1L)).willReturn(Optional.of(testJobPosting));
        given(userRepository.findByEmail("another@example.com")).willReturn(Optional.of(anotherUser));

        // when & then
        assertThatThrownBy(() -> 
            jobPostingService.updateJobPosting(1L, testRequest, "another@example.com"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("해당 공고의 작성자만 수정/삭제할 수 있습니다");

        then(jobPostingRepository).should(times(1)).findById(1L);
        then(userRepository).should(times(1)).findByEmail("another@example.com");
        then(jobPostingRepository).should(times(0)).save(any(JobPosting.class));
    }

    @Test
    @DisplayName("일손 모집 공고 삭제 성공")
    void deleteJobPosting_Success() {
        // given
        given(jobPostingRepository.findById(1L)).willReturn(Optional.of(testJobPosting));
        given(userRepository.findByEmail(testUser.getEmail())).willReturn(Optional.of(testUser));

        // when
        jobPostingService.deleteJobPosting(1L, testUser.getEmail());

        // then
        then(jobPostingRepository).should(times(1)).findById(1L);
        then(userRepository).should(times(1)).findByEmail(testUser.getEmail());
        then(jobPostingRepository).should(times(1)).delete(testJobPosting);
    }

    @Test
    @DisplayName("공고 상태 변경 성공")
    void updateJobPostingStatus_Success() {
        // given
        given(jobPostingRepository.findById(1L)).willReturn(Optional.of(testJobPosting));
        given(userRepository.findByEmail(testUser.getEmail())).willReturn(Optional.of(testUser));
        given(jobPostingRepository.save(any(JobPosting.class))).willReturn(testJobPosting);

        // when
        JobPostingResponse response = jobPostingService.updateJobPostingStatus(
                1L, JobPosting.JobStatus.CLOSED, testUser.getEmail());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(JobPosting.JobStatus.CLOSED);

        then(jobPostingRepository).should(times(1)).findById(1L);
        then(userRepository).should(times(1)).findByEmail(testUser.getEmail());
        then(jobPostingRepository).should(times(1)).save(any(JobPosting.class));
    }
}
