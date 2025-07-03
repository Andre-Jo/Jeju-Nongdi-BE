package com.jeju_nongdi.jeju_nongdi.service;

import com.jeju_nongdi.jeju_nongdi.dto.JobPostingRequest;
import com.jeju_nongdi.jeju_nongdi.dto.JobPostingResponse;
import com.jeju_nongdi.jeju_nongdi.dto.JobPostingMarkerResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JobPostingService 테스트")
class JobPostingServiceTest {

    @Mock
    private JobPostingRepository jobPostingRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JobPostingService jobPostingService;

    private User testUser;
    private JobPosting testJobPosting;
    private JobPostingRequest testRequest;

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
        testJobPosting = JobPosting.builder()
                .id(1L)
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
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 테스트용 요청 객체 생성
        testRequest = JobPostingRequest.builder()
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
                .build();
    }

    @Test
    @DisplayName("일손 모집 공고를 성공적으로 생성한다")
    void createJobPosting_Success() {
        // given
        given(userRepository.findByEmail(testUser.getEmail())).willReturn(Optional.of(testUser));
        given(jobPostingRepository.save(any(JobPosting.class))).willReturn(testJobPosting);

        // when
        JobPostingResponse response = jobPostingService.createJobPosting(testRequest, testUser.getEmail());

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo(testRequest.getTitle());
        assertThat(response.getFarmName()).isEqualTo(testRequest.getFarmName());
        assertThat(response.getCropType()).isEqualTo(testRequest.getCropType());
        verify(jobPostingRepository).save(any(JobPosting.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 공고 생성 시 예외가 발생한다")
    void createJobPosting_UserNotFound_ThrowsException() {
        // given
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> jobPostingService.createJobPosting(testRequest, "nonexistent@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("일손 모집 공고를 ID로 조회한다")
    void getJobPosting_Success() {
        // given
        Long jobPostingId = 1L;
        given(jobPostingRepository.findById(jobPostingId)).willReturn(Optional.of(testJobPosting));

        // when
        JobPostingResponse response = jobPostingService.getJobPosting(jobPostingId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(jobPostingId);
        assertThat(response.getTitle()).isEqualTo(testJobPosting.getTitle());
    }

    @Test
    @DisplayName("존재하지 않는 공고 조회 시 예외가 발생한다")
    void getJobPosting_NotFound_ThrowsException() {
        // given
        Long jobPostingId = 999L;
        given(jobPostingRepository.findById(jobPostingId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> jobPostingService.getJobPosting(jobPostingId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("일손 모집 공고를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("활성 상태 공고 목록을 페이징으로 조회한다")
    void getActiveJobPostings_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<JobPosting> jobPostings = Arrays.asList(testJobPosting);
        Page<JobPosting> jobPostingPage = new PageImpl<>(jobPostings, pageable, 1);

        given(jobPostingRepository.findByStatusOrderByCreatedAtDesc(JobPosting.JobStatus.ACTIVE, pageable))
                .willReturn(jobPostingPage);

        // when
        Page<JobPostingResponse> response = jobPostingService.getActiveJobPostings(pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getTitle()).isEqualTo(testJobPosting.getTitle());
    }

    @Test
    @DisplayName("필터 조건으로 공고를 조회한다")
    void getFilteredJobPostings_Success() {
        // given
        List<JobPosting> jobPostings = Arrays.asList(testJobPosting);
        given(jobPostingRepository.findWithFilters(any(), any(), any(), any(), any()))
                .willReturn(jobPostings);

        // when
        List<JobPostingResponse> response = jobPostingService.getFilteredJobPostings(
                JobPosting.CropType.POTATO, JobPosting.WorkType.HARVESTING, "제주시");

        // then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getCropType()).isEqualTo(JobPosting.CropType.POTATO);
        assertThat(response.get(0).getWorkType()).isEqualTo(JobPosting.WorkType.HARVESTING);
    }

    @Test
    @DisplayName("지도 마커용 데이터를 조회한다")
    void getJobPostingMarkers_Success() {
        // given
        List<JobPosting> jobPostings = Arrays.asList(testJobPosting);
        given(jobPostingRepository.findAllForMap(JobPosting.JobStatus.ACTIVE)).willReturn(jobPostings);

        // when
        List<JobPostingMarkerResponse> response = jobPostingService.getJobPostingMarkers();

        // then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getId()).isEqualTo(testJobPosting.getId());
        assertThat(response.get(0).getLatitude()).isEqualTo(testJobPosting.getLatitude());
        assertThat(response.get(0).getLongitude()).isEqualTo(testJobPosting.getLongitude());
    }

    @Test
    @DisplayName("사용자별 공고를 조회한다")
    void getJobPostingsByUser_Success() {
        // given
        List<JobPosting> jobPostings = Arrays.asList(testJobPosting);
        given(userRepository.findByEmail(testUser.getEmail())).willReturn(Optional.of(testUser));
        given(jobPostingRepository.findByAuthorOrderByCreatedAtDesc(testUser)).willReturn(jobPostings);

        // when
        List<JobPostingResponse> response = jobPostingService.getJobPostingsByUser(testUser.getEmail());

        // then
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getAuthor().getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    @DisplayName("공고를 수정한다")
    void updateJobPosting_Success() {
        // given
        Long jobPostingId = 1L;
        JobPostingRequest updateRequest = JobPostingRequest.builder()
                .title("수정된 제목")
                .description("수정된 설명")
                .farmName("수정된 농장명")
                .address("수정된 주소")
                .latitude(new BigDecimal("33.111111"))
                .longitude(new BigDecimal("126.111111"))
                .cropType(JobPosting.CropType.CABBAGE)
                .workType(JobPosting.WorkType.PLANTING)
                .wages(120000)
                .wageType(JobPosting.WageType.DAILY)
                .workStartDate(LocalDate.now().plusDays(2))
                .workEndDate(LocalDate.now().plusDays(6))
                .recruitmentCount(10)
                .contactPhone("010-9876-5432")
                .contactEmail("updated@example.com")
                .build();

        given(jobPostingRepository.findById(jobPostingId)).willReturn(Optional.of(testJobPosting));
        given(userRepository.findByEmail(testUser.getEmail())).willReturn(Optional.of(testUser));
        given(jobPostingRepository.save(any(JobPosting.class))).willReturn(testJobPosting);

        // when
        JobPostingResponse response = jobPostingService.updateJobPosting(jobPostingId, updateRequest, testUser.getEmail());

        // then
        assertThat(response).isNotNull();
        verify(jobPostingRepository).save(any(JobPosting.class));
    }

    @Test
    @DisplayName("권한이 없는 사용자가 공고 수정 시 예외가 발생한다")
    void updateJobPosting_UnauthorizedUser_ThrowsException() {
        // given
        Long jobPostingId = 1L;
        User otherUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .nickname("다른사용자")
                .build();

        given(jobPostingRepository.findById(jobPostingId)).willReturn(Optional.of(testJobPosting));
        given(userRepository.findByEmail(otherUser.getEmail())).willReturn(Optional.of(otherUser));

        // when & then
        assertThatThrownBy(() -> jobPostingService.updateJobPosting(jobPostingId, testRequest, otherUser.getEmail()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("해당 공고의 작성자만 수정/삭제할 수 있습니다");
    }

    @Test
    @DisplayName("공고를 삭제한다")
    void deleteJobPosting_Success() {
        // given
        Long jobPostingId = 1L;
        given(jobPostingRepository.findById(jobPostingId)).willReturn(Optional.of(testJobPosting));
        given(userRepository.findByEmail(testUser.getEmail())).willReturn(Optional.of(testUser));

        // when
        jobPostingService.deleteJobPosting(jobPostingId, testUser.getEmail());

        // then
        verify(jobPostingRepository).delete(testJobPosting);
    }

    @Test
    @DisplayName("공고 상태를 변경한다")
    void updateJobPostingStatus_Success() {
        // given
        Long jobPostingId = 1L;
        JobPosting.JobStatus newStatus = JobPosting.JobStatus.CLOSED;

        given(jobPostingRepository.findById(jobPostingId)).willReturn(Optional.of(testJobPosting));
        given(userRepository.findByEmail(testUser.getEmail())).willReturn(Optional.of(testUser));
        given(jobPostingRepository.save(any(JobPosting.class))).willReturn(testJobPosting);

        // when
        JobPostingResponse response = jobPostingService.updateJobPostingStatus(jobPostingId, newStatus, testUser.getEmail());

        // then
        assertThat(response).isNotNull();
        verify(jobPostingRepository).save(any(JobPosting.class));
    }
}