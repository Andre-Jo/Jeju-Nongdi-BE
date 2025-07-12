package com.jeju_nongdi.jeju_nongdi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeju_nongdi.jeju_nongdi.dto.MentoringRequest;
import com.jeju_nongdi.jeju_nongdi.dto.MentoringResponse;
import com.jeju_nongdi.jeju_nongdi.entity.Mentoring;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.service.CustomUserDetailsService;
import com.jeju_nongdi.jeju_nongdi.service.MentoringService;
import com.jeju_nongdi.jeju_nongdi.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MentoringController.class)
class MentoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MentoringService mentoringService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("멘토링 글 작성 API 테스트")
    @WithMockUser
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
                        .with(csrf())
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
    @WithMockUser
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
    @WithMockUser
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
    @DisplayName("멘토링 글 수정 API 테스트")
    @WithMockUser
    void updateMentoringTest() throws Exception {
        // given
        Long mentoringId = 1L;
        MentoringRequest request = MentoringRequest.builder()
                .title("수정된 농업 기초 멘토링")
                .description("수정된 농업 초보자를 위한 기초 멘토링을 제공합니다.")
                .mentoringType(Mentoring.MentoringType.MENTOR)
                .category(Mentoring.Category.CROP_CULTIVATION)
                .experienceLevel(Mentoring.ExperienceLevel.BEGINNER)
                .preferredLocation("제주시")
                .contactEmail("john@example.com")
                .contactPhone("010-1234-5678")
                .build();

        MentoringResponse response = MentoringResponse.builder()
                .id(mentoringId)
                .title("수정된 농업 기초 멘토링")
                .description("수정된 농업 초보자를 위한 기초 멘토링을 제공합니다.")
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

        given(mentoringService.updateMentoring(eq(mentoringId), any(MentoringRequest.class), any()))
                .willReturn(response);

        // when & then
        mockMvc.perform(put("/api/mentorings/{id}", mentoringId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mentoringId))
                .andExpect(jsonPath("$.title").value("수정된 농업 기초 멘토링"));
    }

    @Test
    @DisplayName("멘토링 글 삭제 API 테스트")
    @WithMockUser
    void deleteMentoringTest() throws Exception {
        // given
        Long mentoringId = 1L;
        doNothing().when(mentoringService).deleteMentoring(eq(mentoringId), any());

        // when & then
        mockMvc.perform(delete("/api/mentorings/{id}", mentoringId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("멘토링 글이 성공적으로 삭제되었습니다."));
    }

    @Test
    @DisplayName("멘토링 상태 변경 API 테스트")
    @WithMockUser
    void updateMentoringStatusTest() throws Exception {
        // given
        Long mentoringId = 1L;
        Mentoring.MentoringStatus newStatus = Mentoring.MentoringStatus.COMPLETED;

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
                .status(newStatus)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(mentoringService.updateMentoringStatus(eq(mentoringId), eq(newStatus), any()))
                .willReturn(response);

        // when & then
        mockMvc.perform(patch("/api/mentorings/{id}/status", mentoringId)
                        .with(csrf())
                        .param("status", newStatus.name()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mentoringId))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("내가 작성한 멘토링 글 목록 API 테스트")
    @WithMockUser
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

    @Test
    @DisplayName("멘토링 타입 목록 조회 API 테스트")
    @WithMockUser
    void getMentoringTypesTest() throws Exception {
        // when & then
        mockMvc.perform(get("/api/mentorings/mentoring-types"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("카테고리 목록 조회 API 테스트")
    @WithMockUser
    void getCategoriesTest() throws Exception {
        // when & then
        mockMvc.perform(get("/api/mentorings/categories"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
