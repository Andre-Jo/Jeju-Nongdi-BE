package com.jeju_nongdi.jeju_nongdi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeju_nongdi.jeju_nongdi.dto.IdleFarmlandMarkerResponse;
import com.jeju_nongdi.jeju_nongdi.dto.IdleFarmlandRequest;
import com.jeju_nongdi.jeju_nongdi.dto.IdleFarmlandResponse;
import com.jeju_nongdi.jeju_nongdi.entity.IdleFarmland;
import com.jeju_nongdi.jeju_nongdi.service.IdleFarmlandService;
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

import java.math.BigDecimal;
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
class IdleFarmlandControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private IdleFarmlandService idleFarmlandService;

    @InjectMocks
    private IdleFarmlandController idleFarmlandController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(idleFarmlandController).build();
    }

    @Test
    @DisplayName("유휴 농지 등록 API 테스트")
    void createIdleFarmlandTest() throws Exception {
        // given
        IdleFarmlandRequest request = IdleFarmlandRequest.builder()
                .title("제주시 애월읍 농지")
                .description("제주시 애월읍에 위치한 유휴 농지입니다.")
                .farmlandName("애월읍 농지")
                .address("제주시 애월읍 고성리")
                .latitude(BigDecimal.valueOf(33.459722))
                .longitude(BigDecimal.valueOf(126.331389))
                .areaSize(BigDecimal.valueOf(1000.50))
                .usageType(IdleFarmland.UsageType.CULTIVATION)
                .soilType(IdleFarmland.SoilType.VOLCANIC_ASH)
                .monthlyRent(500000)
                .contactEmail("owner@example.com")
                .contactPhone("010-1234-5678")
                .build();

        IdleFarmlandResponse response = IdleFarmlandResponse.builder()
                .id(1L)
                .title("제주시 애월읍 농지")
                .description("제주시 애월읍에 위치한 유휴 농지입니다.")
                .address("제주시 애월읍 고성리")
                .latitude(BigDecimal.valueOf(33.459722))
                .longitude(BigDecimal.valueOf(126.331389))
                .areaSize(BigDecimal.valueOf(1000.50))
                .usageType(IdleFarmland.UsageType.CULTIVATION)
                .soilType(IdleFarmland.SoilType.VOLCANIC_ASH)
                .monthlyRent(500000)
                .contactEmail("owner@example.com")
                .contactPhone("010-1234-5678")
                .status(IdleFarmland.FarmlandStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(idleFarmlandService.createIdleFarmland(any(IdleFarmlandRequest.class), any()))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/idle-farmlands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("제주시 애월읍 농지"))
                .andExpect(jsonPath("$.usageType").value("CULTIVATION"))
                .andExpect(jsonPath("$.soilType").value("VOLCANIC_ASH"));
    }

    @Test
    @DisplayName("유휴 농지 목록 조회 API 테스트")
    void getIdleFarmlandsTest() throws Exception {
        // given
        IdleFarmlandResponse response = IdleFarmlandResponse.builder()
                .id(1L)
                .title("제주시 애월읍 농지")
                .description("제주시 애월읍에 위치한 유휴 농지입니다.")
                .address("제주시 애월읍 고성리")
                .latitude(BigDecimal.valueOf(33.459722))
                .longitude(BigDecimal.valueOf(126.331389))
                .areaSize(BigDecimal.valueOf(1000.50))
                .usageType(IdleFarmland.UsageType.CULTIVATION)
                .soilType(IdleFarmland.SoilType.VOLCANIC_ASH)
                .monthlyRent(500000)
                .contactEmail("owner@example.com")
                .contactPhone("010-1234-5678")
                .status(IdleFarmland.FarmlandStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Page<IdleFarmlandResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);

        given(idleFarmlandService.getIdleFarmlands(any())).willReturn(page);

        // when & then
        mockMvc.perform(get("/api/idle-farmlands")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("제주시 애월읍 농지"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("유휴 농지 상세 조회 API 테스트")
    void getIdleFarmlandTest() throws Exception {
        // given
        Long farmlandId = 1L;
        IdleFarmlandResponse response = IdleFarmlandResponse.builder()
                .id(farmlandId)
                .title("제주시 애월읍 농지")
                .description("제주시 애월읍에 위치한 유휴 농지입니다.")
                .address("제주시 애월읍 고성리")
                .latitude(BigDecimal.valueOf(33.459722))
                .longitude(BigDecimal.valueOf(126.331389))
                .areaSize(BigDecimal.valueOf(1000.50))
                .usageType(IdleFarmland.UsageType.CULTIVATION)
                .soilType(IdleFarmland.SoilType.VOLCANIC_ASH)
                .monthlyRent(500000)
                .contactEmail("owner@example.com")
                .contactPhone("010-1234-5678")
                .status(IdleFarmland.FarmlandStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(idleFarmlandService.getIdleFarmland(farmlandId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/idle-farmlands/{id}", farmlandId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(farmlandId))
                .andExpect(jsonPath("$.title").value("제주시 애월읍 농지"));
    }

    @Test
    @DisplayName("유휴 농지 삭제 API 테스트")
    void deleteIdleFarmlandTest() throws Exception {
        // given
        Long farmlandId = 1L;
        doNothing().when(idleFarmlandService).deleteIdleFarmland(eq(farmlandId), any());

        // when & then
        mockMvc.perform(delete("/api/idle-farmlands/{id}", farmlandId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("내 농지 목록 조회 API 테스트")
    void getMyIdleFarmlandsTest() throws Exception {
        // given
        IdleFarmlandResponse response = IdleFarmlandResponse.builder()
                .id(1L)
                .title("제주시 애월읍 농지")
                .description("제주시 애월읍에 위치한 유휴 농지입니다.")
                .address("제주시 애월읍 고성리")
                .latitude(BigDecimal.valueOf(33.459722))
                .longitude(BigDecimal.valueOf(126.331389))
                .areaSize(BigDecimal.valueOf(1000.50))
                .usageType(IdleFarmland.UsageType.CULTIVATION)
                .soilType(IdleFarmland.SoilType.VOLCANIC_ASH)
                .monthlyRent(500000)
                .contactEmail("owner@example.com")
                .contactPhone("010-1234-5678")
                .status(IdleFarmland.FarmlandStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        given(idleFarmlandService.getMyIdleFarmlands(any())).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/idle-farmlands/my"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("제주시 애월읍 농지"));
    }

    @Test
    @DisplayName("지도 마커용 데이터 조회 API 테스트")
    void getIdleFarmlandMarkersTest() throws Exception {
        // given
        IdleFarmlandMarkerResponse marker = IdleFarmlandMarkerResponse.builder()
                .id(1L)
                .title("제주시 애월읍 농지")
                .address("제주시 애월읍 고성리")
                .latitude(BigDecimal.valueOf(33.459722))
                .longitude(BigDecimal.valueOf(126.331389))
                .areaSize(BigDecimal.valueOf(1000.50))
                .monthlyRent(500000)
                .status(IdleFarmland.FarmlandStatus.AVAILABLE)
                .build();

        given(idleFarmlandService.getIdleFarmlandMarkers()).willReturn(List.of(marker));

        // when & then
        mockMvc.perform(get("/api/idle-farmlands/markers"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("제주시 애월읍 농지"))
                .andExpect(jsonPath("$[0].latitude").value(33.459722));
    }
}
