package com.jeju_nongdi.jeju_nongdi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeju_nongdi.jeju_nongdi.dto.AuthResponse;
import com.jeju_nongdi.jeju_nongdi.dto.LoginRequest;
import com.jeju_nongdi.jeju_nongdi.dto.SignupRequest;
import com.jeju_nongdi.jeju_nongdi.entity.User;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // application-test.properties 활성화
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        signupRequest =
                new SignupRequest("integration@test.com", "password123", "Integration Test", "integrationuser", "01012345678" );

        loginRequest = new LoginRequest("integration@test.com","password123");
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 및 로그인 통합 테스트")
    void signupAndLoginIntegrationTest() throws Exception {
        // 1. 회원가입 테스트
        MvcResult signupResult = mockMvc.perform(post("/api/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value(signupRequest.email()))
                .andExpect(jsonPath("$.nickname").value(signupRequest.nickname()))
                .andReturn();

        // 토큰 추출
        String signupResponseContent = signupResult.getResponse().getContentAsString();
        AuthResponse signupResponse = objectMapper.readValue(signupResponseContent, AuthResponse.class);
        String token = signupResponse.token();
        assertThat(token).isNotEmpty();

        // 2. 사용자가 DB에 저장되었는지 확인
        boolean userExists = userRepository.existsByEmail(signupRequest.email());
        assertThat(userExists).isTrue();

        // 3. 로그인 테스트
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value(loginRequest.email()))
                .andExpect(jsonPath("$.role").value(User.Role.USER.name()));
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 실패 테스트")
    void loginFailWithWrongPasswordTest() throws Exception {
        // 1. 회원가입
        mockMvc.perform(post("/api/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // 2. 잘못된 비밀번호로 로그인 시도
        LoginRequest wrongPasswordRequest = new LoginRequest(loginRequest.email(), "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("중복 회원가입 실패 테스트")
    void duplicateSignupFailTest() throws Exception {
        // 1. 첫 번째 회원가입
        mockMvc.perform(post("/api/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk());

        // 2. 동일한 이메일로 두 번째 회원가입 시도
        mockMvc.perform(post("/api/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}
