package com.jeju_nongdi.jeju_nongdi.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MentoringTest {

    @Test
    @DisplayName("멘토링 엔티티 생성 테스트")
    void createMentoringEntity() {
        // given
        User author = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .name("멘토 테스트")
                .nickname("mentoruser")
                .phone("01012345678")
                .role(User.Role.USER)
                .build();

        // when
        Mentoring mentoring = Mentoring.builder()
                .title("농업 기초 멘토링")
                .description("농업 초보자를 위한 기초 멘토링을 제공합니다.")
                .mentoringType(Mentoring.MentoringType.MENTOR)
                .category(Mentoring.Category.CROP_CULTIVATION)
                .experienceLevel(Mentoring.ExperienceLevel.BEGINNER)
                .preferredLocation("제주시")
                .contactEmail("mentor@test.com")
                .contactPhone("010-1234-5678")
                .status(Mentoring.MentoringStatus.ACTIVE)
                .user(author)
                .build();

        // then
        assertThat(mentoring).isNotNull();
        assertThat(mentoring.getTitle()).isEqualTo("농업 기초 멘토링");
        assertThat(mentoring.getContent()).isEqualTo("농업 초보자를 위한 기초 멘토링을 제공합니다.");
        assertThat(mentoring.getMentoringType()).isEqualTo(Mentoring.MentoringType.MENTOR);
        assertThat(mentoring.getCategory()).isEqualTo(Mentoring.Category.CROP_CULTIVATION);
        assertThat(mentoring.getExperienceLevel()).isEqualTo(Mentoring.ExperienceLevel.BEGINNER);
        assertThat(mentoring.getLocation()).isEqualTo("제주시");
        assertThat(mentoring.getContactEmail()).isEqualTo("mentor@test.com");
        assertThat(mentoring.getContactPhone()).isEqualTo("010-1234-5678");
        assertThat(mentoring.getStatus()).isEqualTo(Mentoring.MentoringStatus.ACTIVE);
        assertThat(mentoring.getAuthor()).isEqualTo(author);
    }

    @Test
    @DisplayName("멘토링 정보 업데이트 테스트")
    void updateMentoringInfo() {
        // given
        User author = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .name("멘토 테스트")
                .build();

        Mentoring mentoring = Mentoring.builder()
                .title("원래 제목")
                .description("원래 내용")
                .mentoringType(Mentoring.MentoringType.MENTOR)
                .category(Mentoring.Category.CROP_CULTIVATION)
                .experienceLevel(Mentoring.ExperienceLevel.BEGINNER)
                .preferredLocation("원래 위치")
                .contactEmail("original@test.com")
                .contactPhone("010-0000-0000")
                .status(Mentoring.MentoringStatus.ACTIVE)
                .user(author)
                .build();

        // when
        mentoring.updateInfo(
                "수정된 제목",
                "수정된 내용",
                Mentoring.MentoringType.MENTEE,
                Mentoring.Category.FARM_MANAGEMENT,
                Mentoring.ExperienceLevel.INTERMEDIATE,
                "수정된 위치",
                "주중 오후 가능",
                "010-1111-1111",
                "updated@test.com"
        );

        // then
        assertThat(mentoring.getTitle()).isEqualTo("수정된 제목");
        assertThat(mentoring.getContent()).isEqualTo("수정된 내용");
        assertThat(mentoring.getMentoringType()).isEqualTo(Mentoring.MentoringType.MENTEE);
        assertThat(mentoring.getCategory()).isEqualTo(Mentoring.Category.FARM_MANAGEMENT);
        assertThat(mentoring.getExperienceLevel()).isEqualTo(Mentoring.ExperienceLevel.INTERMEDIATE);
        assertThat(mentoring.getLocation()).isEqualTo("수정된 위치");
        assertThat(mentoring.getContactEmail()).isEqualTo("updated@test.com");
        assertThat(mentoring.getContactPhone()).isEqualTo("010-1111-1111");
    }

    @Test
    @DisplayName("멘토링 상태 변경 테스트")
    void updateMentoringStatus() {
        // given
        User author = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .name("멘토 테스트")
                .build();

        Mentoring mentoring = Mentoring.builder()
                .title("농업 기초 멘토링")
                .status(Mentoring.MentoringStatus.ACTIVE)
                .user(author)
                .build();

        // when
        mentoring.updateStatus(Mentoring.MentoringStatus.COMPLETED);

        // then
        assertThat(mentoring.getStatus()).isEqualTo(Mentoring.MentoringStatus.COMPLETED);
    }

    @Test
    @DisplayName("작성자 확인 테스트")
    void isWrittenByUser() {
        // given
        User author = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .name("멘토 테스트")
                .build();

        User otherUser = User.builder()
                .id(2L)
                .email("other@test.com")
                .name("다른 사용자")
                .build();

        Mentoring mentoring = Mentoring.builder()
                .title("농업 기초 멘토링")
                .user(author)
                .build();

        // when & then
        assertThat(mentoring.isWrittenBy(author)).isTrue();
        assertThat(mentoring.isWrittenBy(otherUser)).isFalse();
    }

    @Test
    @DisplayName("MentoringType enum 테스트")
    void mentoringTypeEnumTest() {
        // when & then
        assertThat(Mentoring.MentoringType.MENTOR.getKoreanName()).isEqualTo("멘토");
        assertThat(Mentoring.MentoringType.MENTEE.getKoreanName()).isEqualTo("멘티");
    }

    @Test
    @DisplayName("Category enum 테스트")
    void categoryEnumTest() {
        // when & then
        assertThat(Mentoring.Category.CROP_CULTIVATION.getKoreanName()).isEqualTo("작물재배");
        assertThat(Mentoring.Category.LIVESTOCK.getKoreanName()).isEqualTo("축산");
        assertThat(Mentoring.Category.FARM_MANAGEMENT.getKoreanName()).isEqualTo("농장경영");
        assertThat(Mentoring.Category.AGRICULTURAL_TECHNOLOGY.getKoreanName()).isEqualTo("농업 기술");
        assertThat(Mentoring.Category.MARKETING.getKoreanName()).isEqualTo("판매/마케팅");
        assertThat(Mentoring.Category.CERTIFICATION.getKoreanName()).isEqualTo("인증");
        assertThat(Mentoring.Category.FUNDING.getKoreanName()).isEqualTo("자금 조달");
        assertThat(Mentoring.Category.OTHER.getKoreanName()).isEqualTo("기타");
    }

    @Test
    @DisplayName("ExperienceLevel enum 테스트")
    void experienceLevelEnumTest() {
        // when & then
        assertThat(Mentoring.ExperienceLevel.BEGINNER.getKoreanName()).isEqualTo("초급 (1년 미만)");
        assertThat(Mentoring.ExperienceLevel.INTERMEDIATE.getKoreanName()).isEqualTo("중급 (1-5년)");
        assertThat(Mentoring.ExperienceLevel.ADVANCED.getKoreanName()).isEqualTo("고급 (5-10년)");
        assertThat(Mentoring.ExperienceLevel.EXPERT.getKoreanName()).isEqualTo("전문가 (10년 이상)");
    }

    @Test
    @DisplayName("MentoringStatus enum 테스트")
    void mentoringStatusEnumTest() {
        // when & then
        assertThat(Mentoring.MentoringStatus.ACTIVE.getKoreanName()).isEqualTo("모집중");
        assertThat(Mentoring.MentoringStatus.MATCHED.getKoreanName()).isEqualTo("매칭완료");
        assertThat(Mentoring.MentoringStatus.COMPLETED.getKoreanName()).isEqualTo("완료");
        assertThat(Mentoring.MentoringStatus.CANCELLED.getKoreanName()).isEqualTo("취소됨");
    }

    @Test
    @DisplayName("멘토링 활성 상태 확인 테스트")
    void isMentoringActive() {
        // given
        User author = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .name("멘토 테스트")
                .build();

        Mentoring activeMentoring = Mentoring.builder()
                .title("활성 멘토링")
                .status(Mentoring.MentoringStatus.ACTIVE)
                .user(author)
                .build();

        Mentoring completedMentoring = Mentoring.builder()
                .title("완료된 멘토링")
                .status(Mentoring.MentoringStatus.COMPLETED)
                .user(author)
                .build();

        // when & then
        assertThat(activeMentoring.isActive()).isTrue();
        assertThat(completedMentoring.isActive()).isFalse();
    }
}
