package com.jeju_nongdi.jeju_nongdi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeju_nongdi.jeju_nongdi.dto.AuthResponse;
import com.jeju_nongdi.jeju_nongdi.dto.LoginRequest;
import com.jeju_nongdi.jeju_nongdi.dto.SignupRequest;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("회원가입 API 테스트")
    void signupTest() throws Exception {
        // given
        SignupRequest request = new SignupRequest("integration@test.com", "password123", "Integration Test", "integrationuser", "01012345678" );

        AuthResponse response =
                new AuthResponse("test.jwt.token", "integration@test.com", "Integration Test", "integrationuser", User.Role.USER.name());

        given(userService.signup(any(SignupRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test.jwt.token"))
                .andExpect(jsonPath("$.email").value("integration@test.com"))
                .andExpect(jsonPath("$.name").value("Integration Test"))
                .andExpect(jsonPath("$.nickname").value("integrationuser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("로그인 API 테스트")
    void loginTest() throws Exception {
        // given
        LoginRequest request = new LoginRequest("integration@test.com", "password123");

        AuthResponse response =
                new AuthResponse("test.jwt.token", "integration@test.com", "Integration Test", "integrationuser", User.Role.USER.name());

        given(userService.login(any(LoginRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test.jwt.token"))
                .andExpect(jsonPath("$.email").value("integration@test.com"))
                .andExpect(jsonPath("$.name").value("Integration Test"))
                .andExpect(jsonPath("$.nickname").value("integrationuser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }
}
