package com.jeju_nongdi.jeju_nongdi.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("인증되지 않은 요청은 401 응답 테스트")
    void unauthenticatedRequestShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/auth/profile"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("허용된 경로는 인증 없이 접근 가능 테스트")
    void permittedPathsShouldBeAccessible() throws Exception {
        // 회원가입 경로는 허용되어야 함
        mockMvc.perform(options("/api/auth/signup"))
                .andExpect(status().isOk());

        // 로그인 경로는 허용되어야 함
        mockMvc.perform(options("/api/auth/login"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("CORS 설정 테스트")
    void corsConfigurationTest() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                    .header("Origin", "http://localhost:3000")
                    .header("Access-Control-Request-Method", "POST")
                    .header("Access-Control-Request-Headers", "Authorization, Content-Type"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }
}
