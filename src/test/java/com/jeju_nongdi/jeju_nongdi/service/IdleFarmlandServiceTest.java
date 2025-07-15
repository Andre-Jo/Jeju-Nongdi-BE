package com.jeju_nongdi.jeju_nongdi.service;

import com.jeju_nongdi.jeju_nongdi.dto.IdleFarmlandMarkerResponse;
import com.jeju_nongdi.jeju_nongdi.dto.IdleFarmlandRequest;
import com.jeju_nongdi.jeju_nongdi.dto.IdleFarmlandResponse;
import com.jeju_nongdi.jeju_nongdi.entity.IdleFarmland;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.repository.IdleFarmlandRepository;
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
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IdleFarmlandServiceTest {

    @Mock
    private IdleFarmlandRepository idleFarmlandRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private IdleFarmlandService idleFarmlandService;

    private User user;
    private IdleFarmland idleFarmland;
    private IdleFarmlandRequest idleFarmlandRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("owner@test.com")
                .password("encodedPassword")
                .name("농지 소유자")
                .nickname("farmowner")
                .phone("01012345678")
                .role(User.Role.USER)
                .build();

        idleFarmland = IdleFarmland.builder()
                .id(1L)
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
                .contactEmail("owner@test.com")
                .contactPhone("010-1234-5678")
                .status(IdleFarmland.FarmlandStatus.AVAILABLE)
                .owner(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        idleFarmlandRequest = IdleFarmlandRequest.builder()
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
                .contactEmail("owner@test.com")
                .contactPhone("010-1234-5678")
                .build();
    }

    @Test
    @DisplayName("유휴 농지 생성 성공 테스트")
    void createIdleFarmlandSuccess() {
        // given
        given(userDetails.getUsername()).willReturn("owner@test.com");
        given(userService.getCurrentUser(anyString())).willReturn(user);
        given(idleFarmlandRepository.save(any(IdleFarmland.class))).willReturn(idleFarmland);

        // when
        IdleFarmlandResponse response = idleFarmlandService.createIdleFarmland(idleFarmlandRequest, userDetails);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("제주시 애월읍 농지");
        assertThat(response.getUsageType()).isEqualTo(IdleFarmland.UsageType.CULTIVATION);
        assertThat(response.getSoilType()).isEqualTo(IdleFarmland.SoilType.VOLCANIC_ASH);
        verify(idleFarmlandRepository).save(any(IdleFarmland.class));
    }

    @Test
    @DisplayName("유휴 농지 목록 조회 성공 테스트")
    void getIdleFarmlandsSuccess() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<IdleFarmland> farmlandPage = new PageImpl<>(List.of(idleFarmland), pageable, 1);
        
        given(idleFarmlandRepository.findByStatus(
                IdleFarmland.FarmlandStatus.AVAILABLE, pageable)).willReturn(farmlandPage);

        // when
        Page<IdleFarmlandResponse> result = idleFarmlandService.getIdleFarmlands(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("제주시 애월읍 농지");
        verify(idleFarmlandRepository).findByStatus(
                IdleFarmland.FarmlandStatus.AVAILABLE, pageable);
    }

    @Test
    @DisplayName("유휴 농지 상세 조회 성공 테스트")
    void getIdleFarmlandSuccess() {
        // given
        Long farmlandId = 1L;
        given(idleFarmlandRepository.findById(farmlandId)).willReturn(Optional.of(idleFarmland));

        // when
        IdleFarmlandResponse response = idleFarmlandService.getIdleFarmland(farmlandId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(farmlandId);
        assertThat(response.getTitle()).isEqualTo("제주시 애월읍 농지");
        verify(idleFarmlandRepository).findById(farmlandId);
    }

    @Test
    @DisplayName("존재하지 않는 유휴 농지 조회 시 예외 발생 테스트")
    void getIdleFarmlandNotFound() {
        // given
        Long farmlandId = 999L;
        given(idleFarmlandRepository.findById(farmlandId)).willReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () ->
            idleFarmlandService.getIdleFarmland(farmlandId)
        );
    }

    @Test
    @DisplayName("유휴 농지 수정 성공 테스트")
    void updateIdleFarmlandSuccess() {
        // given
        Long farmlandId = 1L;
        IdleFarmlandRequest updateRequest = IdleFarmlandRequest.builder()
                .title("수정된 제주시 애월읍 농지")
                .description("수정된 제주시 애월읍에 위치한 유휴 농지입니다.")
                .farmlandName("수정된 애월읍 농지")
                .address("제주시 애월읍 고성리")
                .latitude(BigDecimal.valueOf(33.459722))
                .longitude(BigDecimal.valueOf(126.331389))
                .areaSize(BigDecimal.valueOf(1200.75))
                .usageType(IdleFarmland.UsageType.LIVESTOCK)
                .soilType(IdleFarmland.SoilType.CLAY)
                .monthlyRent(600000)
                .contactEmail("owner@test.com")
                .contactPhone("010-1234-5678")
                .build();

        given(userDetails.getUsername()).willReturn("owner@test.com");
        given(idleFarmlandRepository.findById(farmlandId)).willReturn(Optional.of(idleFarmland));
        given(userService.getCurrentUser(anyString())).willReturn(user);
        given(idleFarmlandRepository.save(any(IdleFarmland.class))).willReturn(idleFarmland);

        // when
        IdleFarmlandResponse response = idleFarmlandService.updateIdleFarmland(farmlandId, updateRequest, userDetails);

        // then
        assertThat(response).isNotNull();
        verify(idleFarmlandRepository).findById(farmlandId);
        verify(idleFarmlandRepository).save(any(IdleFarmland.class));
    }

    @Test
    @DisplayName("소유자가 아닌 사용자의 유휴 농지 수정 시 예외 발생 테스트")
    void updateIdleFarmlandUnauthorized() {
        // given
        Long farmlandId = 1L;
        User otherUser = User.builder()
                .id(2L)
                .email("other@test.com")
                .name("다른 사용자")
                .build();

        given(userDetails.getUsername()).willReturn("other@test.com");
        given(idleFarmlandRepository.findById(farmlandId)).willReturn(Optional.of(idleFarmland));
        given(userService.getCurrentUser(anyString())).willReturn(otherUser);

        // when & then
        assertThrows(RuntimeException.class, () ->
            idleFarmlandService.updateIdleFarmland(farmlandId, idleFarmlandRequest, userDetails)
        );
    }

    @Test
    @DisplayName("유휴 농지 삭제 성공 테스트")
    void deleteIdleFarmlandSuccess() {
        // given
        Long farmlandId = 1L;
        given(userDetails.getUsername()).willReturn("owner@test.com");
        given(idleFarmlandRepository.findById(farmlandId)).willReturn(Optional.of(idleFarmland));
        given(userService.getCurrentUser(anyString())).willReturn(user);

        // when
        idleFarmlandService.deleteIdleFarmland(farmlandId, userDetails);

        // then
        verify(idleFarmlandRepository).findById(farmlandId);
        verify(idleFarmlandRepository).delete(idleFarmland);
    }

    @Test
    @DisplayName("농지 상태 변경 성공 테스트")
    void updateFarmlandStatusSuccess() {
        // given
        Long farmlandId = 1L;
        IdleFarmland.FarmlandStatus newStatus = IdleFarmland.FarmlandStatus.RENTED;
        
        given(userDetails.getUsername()).willReturn("owner@test.com");
        given(idleFarmlandRepository.findById(farmlandId)).willReturn(Optional.of(idleFarmland));
        given(userService.getCurrentUser(anyString())).willReturn(user);
        given(idleFarmlandRepository.save(any(IdleFarmland.class))).willReturn(idleFarmland);

        // when
        IdleFarmlandResponse response = idleFarmlandService.updateFarmlandStatus(farmlandId, newStatus, userDetails);

        // then
        assertThat(response).isNotNull();
        verify(idleFarmlandRepository).findById(farmlandId);
        verify(idleFarmlandRepository).save(any(IdleFarmland.class));
    }

    @Test
    @DisplayName("내가 등록한 농지 목록 조회 성공 테스트")
    void getMyIdleFarmlandsSuccess() {
        // given
        given(userDetails.getUsername()).willReturn("owner@test.com");
        given(userService.getCurrentUser(anyString())).willReturn(user);
        given(idleFarmlandRepository.findByOwner(user)).willReturn(List.of(idleFarmland));

        // when
        List<IdleFarmlandResponse> result = idleFarmlandService.getMyIdleFarmlands(userDetails);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("제주시 애월읍 농지");
        verify(idleFarmlandRepository).findByOwner(user);
    }

    @Test
    @DisplayName("지도 마커용 데이터 조회 성공 테스트")
    void getIdleFarmlandMarkersSuccess() {
        // given
        given(idleFarmlandRepository.findByStatusOrderByCreatedAtDesc(
                IdleFarmland.FarmlandStatus.AVAILABLE)).willReturn(List.of(idleFarmland));

        // when
        List<IdleFarmlandMarkerResponse> result = idleFarmlandService.getIdleFarmlandMarkers();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("제주시 애월읍 농지");
        assertThat(result.getFirst().getLatitude()).isEqualTo(BigDecimal.valueOf(33.459722));
        assertThat(result.getFirst().getLongitude()).isEqualTo(BigDecimal.valueOf(126.331389));
        verify(idleFarmlandRepository).findByStatusOrderByCreatedAtDesc(
                IdleFarmland.FarmlandStatus.AVAILABLE);
    }

    @Test
    @DisplayName("지역별 지도 마커용 데이터 조회 성공 테스트")
    void getIdleFarmlandMarkersByRegionSuccess() {
        // given
        String region = "제주시";
        given(idleFarmlandRepository.findByAddressContainingAndStatusOrderByCreatedAtDesc(
                region, IdleFarmland.FarmlandStatus.AVAILABLE)).willReturn(List.of(idleFarmland));

        // when
        List<IdleFarmlandMarkerResponse> result = idleFarmlandService.getIdleFarmlandMarkersByRegion(region);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("제주시 애월읍 농지");
        verify(idleFarmlandRepository).findByAddressContainingAndStatusOrderByCreatedAtDesc(
                region, IdleFarmland.FarmlandStatus.AVAILABLE);
    }

    @Test
    @DisplayName("농지 검색 성공 테스트")
    void searchIdleFarmlandsSuccess() {
        // given
        String address = "제주시";
        IdleFarmland.UsageType usageType = IdleFarmland.UsageType.CULTIVATION;
        IdleFarmland.SoilType soilType = IdleFarmland.SoilType.VOLCANIC_ASH;
        BigDecimal minArea = BigDecimal.valueOf(500);
        BigDecimal maxArea = BigDecimal.valueOf(2000);
        Integer minRent = 300000;
        Integer maxRent = 700000;

        given(idleFarmlandRepository.findByAddressContainingAndUsageTypeAndSoilTypeAndAreaBetweenAndRentPriceBetweenAndStatusOrderByCreatedAtDesc(
                address, usageType, soilType, minArea, maxArea, minRent, maxRent, IdleFarmland.FarmlandStatus.AVAILABLE))
                .willReturn(List.of(idleFarmland));

        // when
        List<IdleFarmlandResponse> result = idleFarmlandService.searchIdleFarmlands(
                address, usageType, soilType, minArea, maxArea, minRent, maxRent);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("제주시 애월읍 농지");
        verify(idleFarmlandRepository).findByAddressContainingAndUsageTypeAndSoilTypeAndAreaBetweenAndRentPriceBetweenAndStatusOrderByCreatedAtDesc(
                address, usageType, soilType, minArea, maxArea, minRent, maxRent, IdleFarmland.FarmlandStatus.AVAILABLE);
    }
}
