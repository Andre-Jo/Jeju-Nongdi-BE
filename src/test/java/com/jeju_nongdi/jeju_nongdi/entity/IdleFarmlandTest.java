package com.jeju_nongdi.jeju_nongdi.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class IdleFarmlandTest {

    @Test
    @DisplayName("유휴 농지 엔티티 생성 테스트")
    void createIdleFarmlandEntity() {
        // given
        User owner = User.builder()
                .id(1L)
                .email("owner@test.com")
                .name("농지 소유자")
                .nickname("farmowner")
                .phone("01012345678")
                .role(User.Role.USER)
                .build();

        // when
        IdleFarmland idleFarmland = IdleFarmland.builder()
                .title("제주시 애월읍 농지")
                .description("제주시 애월읍에 위치한 유휴 농지입니다.")
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
                .owner(owner)
                .build();

        // then
        assertThat(idleFarmland).isNotNull();
        assertThat(idleFarmland.getTitle()).isEqualTo("제주시 애월읍 농지");
        assertThat(idleFarmland.getDescription()).isEqualTo("제주시 애월읍에 위치한 유휴 농지입니다.");
        assertThat(idleFarmland.getAddress()).isEqualTo("제주시 애월읍 고성리");
        assertThat(idleFarmland.getLatitude()).isEqualTo(BigDecimal.valueOf(33.459722));
        assertThat(idleFarmland.getLongitude()).isEqualTo(BigDecimal.valueOf(126.331389));
        assertThat(idleFarmland.getArea()).isEqualTo(BigDecimal.valueOf(1000.50));
        assertThat(idleFarmland.getUsageType()).isEqualTo(IdleFarmland.UsageType.CULTIVATION);
        assertThat(idleFarmland.getSoilType()).isEqualTo(IdleFarmland.SoilType.VOLCANIC_ASH);
        assertThat(idleFarmland.getRentPrice()).isEqualTo(500000);
        assertThat(idleFarmland.getAdditionalInfo()).isEqualTo("제주시 애월읍에 위치한 유휴 농지입니다.");
        assertThat(idleFarmland.getContactEmail()).isEqualTo("owner@test.com");
        assertThat(idleFarmland.getContactPhone()).isEqualTo("010-1234-5678");
        assertThat(idleFarmland.getStatus()).isEqualTo(IdleFarmland.FarmlandStatus.AVAILABLE);
        assertThat(idleFarmland.getOwner()).isEqualTo(owner);
    }

    @Test
    @DisplayName("유휴 농지 정보 업데이트 테스트")
    void updateIdleFarmlandInfo() {
        // given
        User owner = User.builder()
                .id(1L)
                .email("owner@test.com")
                .name("농지 소유자")
                .build();

        IdleFarmland idleFarmland = IdleFarmland.builder()
                .title("원래 제목")
                .description("원래 설명")
                .address("원래 주소")
                .latitude(BigDecimal.valueOf(33.0))
                .longitude(BigDecimal.valueOf(126.0))
                .areaSize(BigDecimal.valueOf(500.0))
                .usageType(IdleFarmland.UsageType.CULTIVATION)
                .soilType(IdleFarmland.SoilType.VOLCANIC_ASH)
                .monthlyRent(300000)
                .contactEmail("original@test.com")
                .contactPhone("010-0000-0000")
                .status(IdleFarmland.FarmlandStatus.AVAILABLE)
                .owner(owner)
                .build();

        // when
        idleFarmland.updateInfo(
                "수정된 제목",
                "수정된 설명",
                "수정된 주소",
                BigDecimal.valueOf(33.459722),
                BigDecimal.valueOf(126.331389),
                BigDecimal.valueOf(1000.50),
                IdleFarmland.UsageType.LIVESTOCK,
                IdleFarmland.SoilType.CLAY,
                600000,
                "수정된 추가 정보",
                "updated@test.com",
                "010-1111-1111"
        );

        // then
        assertThat(idleFarmland.getTitle()).isEqualTo("수정된 제목");
        assertThat(idleFarmland.getDescription()).isEqualTo("수정된 설명");
        assertThat(idleFarmland.getAddress()).isEqualTo("수정된 주소");
        assertThat(idleFarmland.getLatitude()).isEqualTo(BigDecimal.valueOf(33.459722));
        assertThat(idleFarmland.getLongitude()).isEqualTo(BigDecimal.valueOf(126.331389));
        assertThat(idleFarmland.getArea()).isEqualTo(BigDecimal.valueOf(1000.50));
        assertThat(idleFarmland.getUsageType()).isEqualTo(IdleFarmland.UsageType.LIVESTOCK);
        assertThat(idleFarmland.getSoilType()).isEqualTo(IdleFarmland.SoilType.CLAY);
        assertThat(idleFarmland.getRentPrice()).isEqualTo(600000);
        assertThat(idleFarmland.getAdditionalInfo()).isEqualTo("수정된 설명"); // getAdditionalInfo()는 description을 반환
        assertThat(idleFarmland.getContactEmail()).isEqualTo("updated@test.com");
        assertThat(idleFarmland.getContactPhone()).isEqualTo("010-1111-1111");
    }

    @Test
    @DisplayName("유휴 농지 상태 변경 테스트")
    void updateFarmlandStatus() {
        // given
        User owner = User.builder()
                .id(1L)
                .email("owner@test.com")
                .name("농지 소유자")
                .build();

        IdleFarmland idleFarmland = IdleFarmland.builder()
                .title("제주시 애월읍 농지")
                .status(IdleFarmland.FarmlandStatus.AVAILABLE)
                .owner(owner)
                .build();

        // when
        idleFarmland.updateStatus(IdleFarmland.FarmlandStatus.RENTED);

        // then
        assertThat(idleFarmland.getStatus()).isEqualTo(IdleFarmland.FarmlandStatus.RENTED);
    }

    @Test
    @DisplayName("소유자 확인 테스트")
    void isOwnedByUser() {
        // given
        User owner = User.builder()
                .id(1L)
                .email("owner@test.com")
                .name("농지 소유자")
                .build();

        User otherUser = User.builder()
                .id(2L)
                .email("other@test.com")
                .name("다른 사용자")
                .build();

        IdleFarmland idleFarmland = IdleFarmland.builder()
                .title("제주시 애월읍 농지")
                .owner(owner)
                .build();

        // when & then
        assertThat(idleFarmland.isOwnedBy(owner)).isTrue();
        assertThat(idleFarmland.isOwnedBy(otherUser)).isFalse();
    }

    @Test
    @DisplayName("UsageType enum 테스트")
    void usageTypeEnumTest() {
        // when & then
        assertThat(IdleFarmland.UsageType.CULTIVATION.getKoreanName()).isEqualTo("재배용");
        assertThat(IdleFarmland.UsageType.LIVESTOCK.getKoreanName()).isEqualTo("축산용");
        assertThat(IdleFarmland.UsageType.WAREHOUSE.getKoreanName()).isEqualTo("창고용");
        assertThat(IdleFarmland.UsageType.MIXED.getKoreanName()).isEqualTo("복합용");
        assertThat(IdleFarmland.UsageType.OTHER.getKoreanName()).isEqualTo("기타");
    }

    @Test
    @DisplayName("SoilType enum 테스트")
    void soilTypeEnumTest() {
        // when & then
        assertThat(IdleFarmland.SoilType.VOLCANIC_ASH.getKoreanName()).isEqualTo("화산재토");
        assertThat(IdleFarmland.SoilType.CLAY.getKoreanName()).isEqualTo("점토");
        assertThat(IdleFarmland.SoilType.SANDY.getKoreanName()).isEqualTo("사질토");
        assertThat(IdleFarmland.SoilType.LOAM.getKoreanName()).isEqualTo("양토");
        assertThat(IdleFarmland.SoilType.GRAVEL.getKoreanName()).isEqualTo("자갈토");
        assertThat(IdleFarmland.SoilType.OTHER.getKoreanName()).isEqualTo("기타");
    }

    @Test
    @DisplayName("FarmlandStatus enum 테스트")
    void farmlandStatusEnumTest() {
        // when & then
        assertThat(IdleFarmland.FarmlandStatus.AVAILABLE.getKoreanName()).isEqualTo("이용가능");
        assertThat(IdleFarmland.FarmlandStatus.RENTED.getKoreanName()).isEqualTo("임대중");
        assertThat(IdleFarmland.FarmlandStatus.UNAVAILABLE.getKoreanName()).isEqualTo("임대 불가");
    }
}
