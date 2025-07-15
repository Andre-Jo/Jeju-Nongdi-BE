package com.jeju_nongdi.jeju_nongdi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeju_nongdi.jeju_nongdi.dto.MentoringRequest;
import com.jeju_nongdi.jeju_nongdi.dto.MentoringResponse;
import com.jeju_nongdi.jeju_nongdi.entity.Mentoring;
import com.jeju_nongdi.jeju_nongdi.service.MentoringService;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MentoringControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MentoringService mentoringService;

    @InjectMocks
    private MentoringController mentoringController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(mentoringController).build();
    }

    @Test
    @DisplayName("멘토링 글 작성 API 테스트")
    void createMentoringTest() throws Exception {
        // given
        MentoringRequest request = MentoringRequest.builder()
                .title("농업 기초 멘토링")
                .description("농업 초보자를 위한 기초 멘토링을 제공합니다.")
                .mentoringType(Mentoring.MentoringType.MENTOR)
                .category(Mentoring.Category.CROP_CULTIVATION)
                .experienceLevel(Mentoring.ExperienceLevel.BEGINNER)
                .preferredLocation("제주시")
                .contactEmail("john@example.com")
                .contactPhone("010-1234-5678")
                .build();

        MentoringResponse response = MentoringResponse.builder()
                .id(1L)
                .title("농업 기초 멘토링")
                .description("농업 초보자를 위한 기초 멘토링을 제공합니다.")
                .mentoringType(Mentoring.MentoringType.MENTOR)
                .category(Mentoring.Category.CROP_CULTIVATION)
                .experienceLevel(Mentoring.ExperienceLevel.BEGINNER)
                .preferredLocation("제주시")
                .contactEmail("john@example.com")
                .contactPhone("010-1234-5678")
                .status(Mentoring.MentoringStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(mentoringService.createMentoring(any(MentoringRequest.class), any()))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/mentorings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("농업 기초 멘토링"))
                .andExpect(jsonPath("$.mentoringType").value("MENTOR"))
                .andExpect(jsonPath("$.category").value("CROP_CULTIVATION"));
    }

    @Test
    @DisplayName("멘토링 글 목록 조회 API 테스트")
    void getMentoringsTest() throws Exception {
        // given
        MentoringResponse response = MentoringResponse.builder()
                .id(1L)
                .title("농업 기초 멘토링")
                .description("농업 초보자를 위한 기초 멘토링을 제공합니다.")
                .mentoringType(Mentoring.MentoringType.MENTOR)
                .category(Mentoring.Category.CROP_CULTIVATION)
                .experienceLevel(Mentoring.ExperienceLevel.BEGINNER)
                .preferredLocation("제주시")
                .contactEmail("john@example.com")
                .contactPhone("010-1234-5678")
                .status(Mentoring.MentoringStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Page<MentoringResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);

        given(mentoringService.getMentorings(any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/mentorings")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("농업 기초 멘토링"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("멘토링 글 상세 조회 API 테스트")
    void getMentoringTest() throws Exception {
        // given
        Long mentoringId = 1L;
        MentoringResponse response = MentoringResponse.builder()
                .id(mentoringId)
                .title("농업 기초 멘토링")
                .description("농업 초보자를 위한 기초 멘토링을 제공합니다.")
                .mentoringType(Mentoring.MentoringType.MENTOR)
                .category(Mentoring.Category.CROP_CULTIVATION)
                .experienceLevel(Mentoring.ExperienceLevel.BEGINNER)
                .preferredLocation("제주시")
                .contactEmail("john@example.com")
                .contactPhone("010-1234-5678")
                .status(Mentoring.MentoringStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(mentoringService.getMentoring(mentoringId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/mentorings/{id}", mentoringId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mentoringId))
                .andExpect(jsonPath("$.title").value("농업 기초 멘토링"));
    }

    @Test
    @DisplayName("멘토링 글 삭제 API 테스트")
    void deleteMentoringTest() throws Exception {
        // given
        Long mentoringId = 1L;
        doNothing().when(mentoringService).deleteMentoring(eq(mentoringId), any());

        // when & then
        mockMvc.perform(delete("/api/mentorings/{id}", mentoringId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("내가 작성한 멘토링 글 목록 API 테스트")
    void getMyMentoringsTest() throws Exception {
        // given
        MentoringResponse response = MentoringResponse.builder()
                .id(1L)
                .title("농업 기초 멘토링")
                .description("농업 초보자를 위한 기초 멘토링을 제공합니다.")
                .mentoringType(Mentoring.MentoringType.MENTOR)
                .category(Mentoring.Category.CROP_CULTIVATION)
                .experienceLevel(Mentoring.ExperienceLevel.BEGINNER)
                .preferredLocation("제주시")
                .contactEmail("john@example.com")
                .contactPhone("010-1234-5678")
                .status(Mentoring.MentoringStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(mentoringService.getMyMentorings(any())).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/mentorings/my"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("농업 기초 멘토링"));
    }
}
