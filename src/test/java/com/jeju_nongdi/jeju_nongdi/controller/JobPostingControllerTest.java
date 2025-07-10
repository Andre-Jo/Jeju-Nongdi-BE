package com.jeju_nongdi.jeju_nongdi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeju_nongdi.jeju_nongdi.dto.JobPostingMarkerResponse;
import com.jeju_nongdi.jeju_nongdi.dto.JobPostingRequest;
import com.jeju_nongdi.jeju_nongdi.dto.JobPostingResponse;
import com.jeju_nongdi.jeju_nongdi.entity.JobPosting;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.service.JobPostingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("JobPostingController 테스트")
class JobPostingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JobPostingService jobPostingService;

    private JobPostingRequest testRequest;
    private JobPostingResponse testResponse;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("농부김씨")
                .build();

        // 테스트용 요청 객체
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

        // 테스트용 응답 객체
        testResponse = JobPostingResponse.builder()
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
                .author(JobPostingResponse.AuthorInfo.builder()
                        .id(1L)
                        .email("test@example.com")
                        .nickname("농부김씨")
                        .build())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("일손 모집 공고 생성 - 성공")
    @WithMockUser(username = "test@example.com")
    void createJobPosting_Success() throws Exception {
        // given
        given(jobPostingService.createJobPosting(any(JobPostingRequest.class), anyString()))
                .willReturn(testResponse);

        // when & then
        mockMvc.perform(post("/api/job-postings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(testResponse.getTitle()))
                .andExpect(jsonPath("$.farmName").value(testResponse.getFarmName()))
                .andExpect(jsonPath("$.cropType").value(testResponse.getCropType().toString()))
                .andExpect(jsonPath("$.workType").value(testResponse.getWorkType().toString()))
                .andExpect(jsonPath("$.wages").value(testResponse.getWages()));

        verify(jobPostingService).createJobPosting(any(JobPostingRequest.class), anyString());
    }

    @Test
    @DisplayName("일손 모집 공고 목록 조회 - 성공")
    @WithMockUser(username = "test@example.com")
    void getJobPostings_Success() throws Exception {
        // given
        List<JobPostingResponse> jobPostings = List.of(testResponse);
        Page<JobPostingResponse> page = new PageImpl<>(jobPostings, PageRequest.of(0, 20), 1);
        given(jobPostingService.getActiveJobPostings(any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/job-postings")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value(testResponse.getTitle()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(20));

        verify(jobPostingService).getActiveJobPostings(any());
    }

    @Test
    @DisplayName("일손 모집 공고 필터링 조회 - 성공")
    @WithMockUser(username = "test@example.com")
    void getFilteredJobPostings_Success() throws Exception {
        // given
        List<JobPostingResponse> jobPostings = List.of(testResponse);
        given(jobPostingService.getFilteredJobPostings(any(), any(), any())).willReturn(jobPostings);

        // when & then
        mockMvc.perform(get("/api/job-postings/filter")
                        .param("cropType", "POTATO")
                        .param("workType", "HARVESTING")
                        .param("address", "제주시"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value(testResponse.getTitle()))
                .andExpect(jsonPath("$[0].cropType").value("POTATO"))
                .andExpect(jsonPath("$[0].workType").value("HARVESTING"));

        verify(jobPostingService).getFilteredJobPostings(
                JobPosting.CropType.POTATO,
                JobPosting.WorkType.HARVESTING,
                "제주시"
        );
    }

    @Test
    @DisplayName("지도 마커용 데이터 조회 - 성공")
    @WithMockUser(username = "test@example.com")
    void getJobPostingMarkers_Success() throws Exception {
        // given
        JobPostingMarkerResponse markerResponse = JobPostingMarkerResponse.builder()
                .id(1L)
                .title("감자 수확 일손 구합니다")
                .farmName("제주 감자농장")
                .latitude(new BigDecimal("33.123456"))
                .longitude(new BigDecimal("126.123456"))
                .cropType(JobPosting.CropType.POTATO)
                .workType(JobPosting.WorkType.HARVESTING)
                .wages(100000)
                .wageType(JobPosting.WageType.DAILY)
                .build();

        given(jobPostingService.getJobPostingMarkers()).willReturn(List.of(markerResponse));

        // when & then
        mockMvc.perform(get("/api/job-postings/markers"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("감자 수확 일손 구합니다"))
                .andExpect(jsonPath("$[0].latitude").value(33.123456))
                .andExpect(jsonPath("$[0].longitude").value(126.123456));

        verify(jobPostingService).getJobPostingMarkers();
    }

    @Test
    @DisplayName("일손 모집 공고 상세 조회 - 성공")
    @WithMockUser(username = "test@example.com")
    void getJobPosting_Success() throws Exception {
        // given
        Long jobPostingId = 1L;
        given(jobPostingService.getJobPosting(jobPostingId)).willReturn(testResponse);

        // when & then
        mockMvc.perform(get("/api/job-postings/{id}", jobPostingId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(jobPostingId))
                .andExpect(jsonPath("$.title").value(testResponse.getTitle()))
                .andExpect(jsonPath("$.description").value(testResponse.getDescription()));

        verify(jobPostingService).getJobPosting(jobPostingId);
    }

    @Test
    @DisplayName("일손 모집 공고 수정 - 성공")
    @WithMockUser(username = "test@example.com")
    void updateJobPosting_Success() throws Exception {
        // given
        Long jobPostingId = 1L;
        given(jobPostingService.updateJobPosting(eq(jobPostingId), any(JobPostingRequest.class), anyString()))
                .willReturn(testResponse);

        // when & then
        mockMvc.perform(put("/api/job-postings/{id}", jobPostingId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(testResponse.getTitle()));

        verify(jobPostingService).updateJobPosting(eq(jobPostingId), any(JobPostingRequest.class), anyString());
    }

    @Test
    @DisplayName("일손 모집 공고 삭제 - 성공")
    @WithMockUser(username = "test@example.com")
    void deleteJobPosting_Success() throws Exception {
        // given
        Long jobPostingId = 1L;
        doNothing().when(jobPostingService).deleteJobPosting(jobPostingId, "test@example.com");

        // when & then
        mockMvc.perform(delete("/api/job-postings/{id}", jobPostingId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("공고가 성공적으로 삭제되었습니다."))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(jobPostingService).deleteJobPosting(jobPostingId, "test@example.com");
    }

    @Test
    @DisplayName("일손 모집 공고 상태 변경 - 성공")
    @WithMockUser(username = "test@example.com")
    void updateJobPostingStatus_Success() throws Exception {
        // given
        Long jobPostingId = 1L;
        JobPosting.JobStatus newStatus = JobPosting.JobStatus.CLOSED;
        given(jobPostingService.updateJobPostingStatus(jobPostingId, newStatus, "test@example.com"))
                .willReturn(testResponse);

        // when & then
        mockMvc.perform(patch("/api/job-postings/{id}/status", jobPostingId)
                        .with(csrf())
                        .param("status", newStatus.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(jobPostingId));

        verify(jobPostingService).updateJobPostingStatus(jobPostingId, newStatus, "test@example.com");
    }

    @Test
    @DisplayName("내가 작성한 일손 모집 공고 조회 - 성공")
    @WithMockUser(username = "test@example.com")
    void getMyJobPostings_Success() throws Exception {
        // given
        List<JobPostingResponse> myJobPostings = List.of(testResponse);
        given(jobPostingService.getJobPostingsByUser("test@example.com")).willReturn(myJobPostings);

        // when & then
        mockMvc.perform(get("/api/job-postings/my"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value(testResponse.getTitle()))
                .andExpect(jsonPath("$[0].author.email").value("test@example.com"));

        verify(jobPostingService).getJobPostingsByUser("test@example.com");
    }

    @Test
    @DisplayName("작물 타입 목록 조회 - 성공")
    @WithMockUser(username = "test@example.com")
    void getCropTypes_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/job-postings/crop-types"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        
        // Enum 조회는 서비스를 사용하지 않으므로 verify 하지 않음
    }

    @Test
    @DisplayName("작업 타입 목록 조회 - 성공")
    @WithMockUser(username = "test@example.com")
    void getWorkTypes_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/job-postings/work-types"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("급여 타입 목록 조회 - 성공")
    @WithMockUser(username = "test@example.com")
    void getWageTypes_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/job-postings/wage-types"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("공고 상태 목록 조회 - 성공")
    @WithMockUser(username = "test@example.com")
    void getJobStatuses_Success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/job-postings/job-statuses"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
