package com.jeju_nongdi.jeju_nongdi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeju_nongdi.jeju_nongdi.dto.AuthResponse;
import com.jeju_nongdi.jeju_nongdi.dto.JobPostingRequest;
import com.jeju_nongdi.jeju_nongdi.dto.JobPostingResponse;
import com.jeju_nongdi.jeju_nongdi.dto.SignupRequest;
import com.jeju_nongdi.jeju_nongdi.entity.JobPosting;
import com.jeju_nongdi.jeju_nongdi.repository.JobPostingRepository;
import com.jeju_nongdi.jeju_nongdi.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("JobPosting 통합 테스트")
public class JobPostingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    private String authToken;
    private String userEmail;
    private JobPostingRequest testJobPostingRequest;

    @BeforeEach
    void setUp() throws Exception {
        // 테스트용 사용자 회원가입 및 토큰 발급
        userEmail = "jobposting@test.com";
        SignupRequest signupRequest = new SignupRequest(
                userEmail, 
                "password123", 
                "JobPosting Test User", 
                "jobpostinguser", 
                "01087654321"
        );

        MvcResult signupResult = mockMvc.perform(post("/api/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String signupResponseContent = signupResult.getResponse().getContentAsString();
        AuthResponse signupResponse = objectMapper.readValue(signupResponseContent, AuthResponse.class);
        authToken = signupResponse.token();

        // 테스트용 JobPosting 요청 객체 생성
        testJobPostingRequest = JobPostingRequest.builder()
                .title("통합테스트 감자 수확 일손 구합니다")
                .description("통합테스트용 감자 수확을 도와주실 분을 찾습니다.")
                .farmName("통합테스트 제주 감자농장")
                .address("제주시 한림읍")
                .latitude(new BigDecimal("33.123456"))
                .longitude(new BigDecimal("126.123456"))
                .cropType(JobPosting.CropType.POTATO)
                .workType(JobPosting.WorkType.HARVESTING)
                .wages(120000)
                .wageType(JobPosting.WageType.DAILY)
                .workStartDate(LocalDate.now().plusDays(1))
                .workEndDate(LocalDate.now().plusDays(5))
                .recruitmentCount(5)
                .contactPhone("010-1234-5678")
                .contactEmail("farm@test.com")
                .build();
    }

    @AfterEach
    void tearDown() {
        jobPostingRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("공고 생성부터 조회까지의 전체 플로우 테스트")
    void createAndRetrieveJobPostingIntegrationTest() throws Exception {
        // 1. 공고 생성
        MvcResult createResult = mockMvc.perform(post("/api/job-postings")
                .header("Authorization", "Bearer " + authToken)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testJobPostingRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(testJobPostingRequest.getTitle()))
                .andExpect(jsonPath("$.farmName").value(testJobPostingRequest.getFarmName()))
                .andExpect(jsonPath("$.cropType").value(testJobPostingRequest.getCropType().toString()))
                .andExpect(jsonPath("$.author.email").value(userEmail))
                .andReturn();

        String createResponseContent = createResult.getResponse().getContentAsString();
        JobPostingResponse createdJobPosting = objectMapper.readValue(createResponseContent, JobPostingResponse.class);
        Long jobPostingId = createdJobPosting.getId();

        // 2. DB에 실제로 저장되었는지 확인
        assertThat(jobPostingRepository.existsById(jobPostingId)).isTrue();

        // 3. 개별 공고 조회
        mockMvc.perform(get("/api/job-postings/{id}", jobPostingId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(jobPostingId))
                .andExpect(jsonPath("$.title").value(testJobPostingRequest.getTitle()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // 4. 공고 목록에서 조회
        mockMvc.perform(get("/api/job-postings")
                .param("page", "0")
                .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(jobPostingId))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("공고 삭제 통합 테스트")
    void deleteJobPostingIntegrationTest() throws Exception {
        // 1. 공고 생성
        MvcResult createResult = mockMvc.perform(post("/api/job-postings")
                .header("Authorization", "Bearer " + authToken)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testJobPostingRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String createResponseContent = createResult.getResponse().getContentAsString();
        JobPostingResponse createdJobPosting = objectMapper.readValue(createResponseContent, JobPostingResponse.class);
        Long jobPostingId = createdJobPosting.getId();

        // 2. 공고 삭제
        mockMvc.perform(delete("/api/job-postings/{id}", jobPostingId)
                .header("Authorization", "Bearer " + authToken)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 3. 삭제 확인
        assertThat(jobPostingRepository.existsById(jobPostingId)).isFalse();
    }

    @Test
    @DisplayName("지도 마커용 데이터 조회 통합 테스트")
    void getJobPostingMarkersIntegrationTest() throws Exception {
        // 1. 공고 생성
        mockMvc.perform(post("/api/job-postings")
                .header("Authorization", "Bearer " + authToken)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testJobPostingRequest)))
                .andExpect(status().isCreated());

        // 2. 마커용 데이터 조회
        mockMvc.perform(get("/api/job-postings/markers"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].latitude").value(testJobPostingRequest.getLatitude()))
                .andExpect(jsonPath("$[0].longitude").value(testJobPostingRequest.getLongitude()))
                .andExpect(jsonPath("$[0].title").value(testJobPostingRequest.getTitle()));
    }

    @Test
    @DisplayName("인증되지 않은 사용자의 공고 생성 실패 테스트")
    void createJobPostingWithoutAuthenticationTest() throws Exception {
        mockMvc.perform(post("/api/job-postings")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testJobPostingRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
