package com.jeju_nongdi.jeju_nongdi.service;

import com.jeju_nongdi.jeju_nongdi.dto.MentoringRequest;
import com.jeju_nongdi.jeju_nongdi.dto.MentoringResponse;
import com.jeju_nongdi.jeju_nongdi.entity.Mentoring;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.repository.MentoringRepository;
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
class MentoringServiceTest {

    @Mock
    private MentoringRepository mentoringRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private MentoringService mentoringService;

    private User user;
    private Mentoring mentoring;
    private MentoringRequest mentoringRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("mentor@test.com")
                .password("encodedPassword")
                .name("멘토 테스트")
                .nickname("mentoruser")
                .phone("01012345678")
                .role(User.Role.USER)
                .build();

        mentoring = Mentoring.builder()
                .id(1L)
                .title("농업 기초 멘토링")
                .description("농업 초보자를 위한 기초 멘토링을 제공합니다.")
                .mentoringType(Mentoring.MentoringType.MENTOR)
                .category(Mentoring.Category.CROP_CULTIVATION)
                .experienceLevel(Mentoring.ExperienceLevel.BEGINNER)
                .preferredLocation("제주시")
                .contactEmail("mentor@test.com")
                .contactPhone("010-1234-5678")
                .status(Mentoring.MentoringStatus.ACTIVE)
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        mentoringRequest = new MentoringRequest(
                "농업 기초 멘토링",
                "농업 초보자를 위한 기초 멘토링을 제공합니다.",
                Mentoring.MentoringType.MENTOR,
                Mentoring.Category.CROP_CULTIVATION,
                Mentoring.ExperienceLevel.BEGINNER,
                "제주시",
                null, // preferredSchedule
                "010-1234-5678",
                "mentor@test.com"
        );
    }

    @Test
    @DisplayName("멘토링 글 생성 성공 테스트")
    void createMentoringSuccess() {
        // given
        given(userDetails.getUsername()).willReturn("mentor@test.com");
        given(userService.getCurrentUser(anyString())).willReturn(user);
        given(mentoringRepository.save(any(Mentoring.class))).willReturn(mentoring);

        // when
        MentoringResponse response = mentoringService.createMentoring(mentoringRequest, userDetails);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("농업 기초 멘토링");
        assertThat(response.getMentoringType()).isEqualTo(Mentoring.MentoringType.MENTOR);
        assertThat(response.getCategory()).isEqualTo(Mentoring.Category.CROP_CULTIVATION);
        verify(mentoringRepository).save(any(Mentoring.class));
    }

    @Test
    @DisplayName("멘토링 글 목록 조회 성공 테스트")
    void getMentoringsSuccess() {
        // given
        Pageable pageable = PageRequest.of(0, 20);
        Page<Mentoring> mentoringPage = new PageImpl<>(List.of(mentoring), pageable, 1);
        
        given(mentoringRepository.findByStatusOrderByCreatedAtDesc(
                Mentoring.MentoringStatus.ACTIVE, pageable)).willReturn(mentoringPage);

        // when
        Page<MentoringResponse> result = mentoringService.getMentorings(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("농업 기초 멘토링");
        verify(mentoringRepository).findByStatusOrderByCreatedAtDesc(Mentoring.MentoringStatus.ACTIVE, pageable);
    }

    @Test
    @DisplayName("멘토링 글 상세 조회 성공 테스트")
    void getMentoringSuccess() {
        // given
        Long mentoringId = 1L;
        given(mentoringRepository.findById(mentoringId)).willReturn(Optional.of(mentoring));

        // when
        MentoringResponse response = mentoringService.getMentoring(mentoringId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(mentoringId);
        assertThat(response.getTitle()).isEqualTo("농업 기초 멘토링");
        verify(mentoringRepository).findById(mentoringId);
    }

    @Test
    @DisplayName("존재하지 않는 멘토링 글 조회 시 예외 발생 테스트")
    void getMentoringNotFound() {
        // given
        Long mentoringId = 999L;
        given(mentoringRepository.findById(mentoringId)).willReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> {
            mentoringService.getMentoring(mentoringId);
        });
    }

    @Test
    @DisplayName("멘토링 글 수정 성공 테스트")
    void updateMentoringSuccess() {
        // given
        Long mentoringId = 1L;
        MentoringRequest updateRequest = new MentoringRequest(
                "수정된 농업 기초 멘토링",
                "수정된 농업 초보자를 위한 기초 멘토링을 제공합니다.",
                Mentoring.MentoringType.MENTOR,
                Mentoring.Category.CROP_CULTIVATION,
                Mentoring.ExperienceLevel.INTERMEDIATE,
                "서귀포시",
                "주중 오후 가능", // preferredSchedule
                "010-1234-5678",
                "mentor@test.com"
        );

        given(userDetails.getUsername()).willReturn("mentor@test.com");
        given(mentoringRepository.findById(mentoringId)).willReturn(Optional.of(mentoring));
        given(userService.getCurrentUser(anyString())).willReturn(user);
        given(mentoringRepository.save(any(Mentoring.class))).willReturn(mentoring);

        // when
        MentoringResponse response = mentoringService.updateMentoring(mentoringId, updateRequest, userDetails);

        // then
        assertThat(response).isNotNull();
        verify(mentoringRepository).findById(mentoringId);
        verify(mentoringRepository).save(any(Mentoring.class));
    }

    @Test
    @DisplayName("작성자가 아닌 사용자의 멘토링 글 수정 시 예외 발생 테스트")
    void updateMentoringUnauthorized() {
        // given
        Long mentoringId = 1L;
        User otherUser = User.builder()
                .id(2L)
                .email("other@test.com")
                .name("다른 사용자")
                .build();

        given(userDetails.getUsername()).willReturn("other@test.com");
        given(mentoringRepository.findById(mentoringId)).willReturn(Optional.of(mentoring));
        given(userService.getCurrentUser(anyString())).willReturn(otherUser);

        // when & then
        assertThrows(RuntimeException.class, () -> {
            mentoringService.updateMentoring(mentoringId, mentoringRequest, userDetails);
        });
    }

    @Test
    @DisplayName("멘토링 글 삭제 성공 테스트")
    void deleteMentoringSuccess() {
        // given
        Long mentoringId = 1L;
        given(userDetails.getUsername()).willReturn("mentor@test.com");
        given(mentoringRepository.findById(mentoringId)).willReturn(Optional.of(mentoring));
        given(userService.getCurrentUser(anyString())).willReturn(user);

        // when
        mentoringService.deleteMentoring(mentoringId, userDetails);

        // then
        verify(mentoringRepository).findById(mentoringId);
        verify(mentoringRepository).delete(mentoring);
    }

    @Test
    @DisplayName("멘토링 상태 변경 성공 테스트")
    void updateMentoringStatusSuccess() {
        // given
        Long mentoringId = 1L;
        Mentoring.MentoringStatus newStatus = Mentoring.MentoringStatus.COMPLETED;
        
        given(userDetails.getUsername()).willReturn("mentor@test.com");
        given(mentoringRepository.findById(mentoringId)).willReturn(Optional.of(mentoring));
        given(userService.getCurrentUser(anyString())).willReturn(user);
        given(mentoringRepository.save(any(Mentoring.class))).willReturn(mentoring);

        // when
        MentoringResponse response = mentoringService.updateMentoringStatus(mentoringId, newStatus, userDetails);

        // then
        assertThat(response).isNotNull();
        verify(mentoringRepository).findById(mentoringId);
        verify(mentoringRepository).save(any(Mentoring.class));
    }

    @Test
    @DisplayName("내가 작성한 멘토링 글 목록 조회 성공 테스트")
    void getMyMentoringsSuccess() {
        // given
        given(userDetails.getUsername()).willReturn("mentor@test.com");
        given(userService.getCurrentUser(anyString())).willReturn(user);
        given(mentoringRepository.findByUserOrderByCreatedAtDesc(user)).willReturn(List.of(mentoring));

        // when
        List<MentoringResponse> result = mentoringService.getMyMentorings(userDetails);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("농업 기초 멘토링");
        verify(mentoringRepository).findByUserOrderByCreatedAtDesc(user);
    }

    @Test
    @DisplayName("멘토링 타입별 조회 성공 테스트")
    void getMentoringsByTypeSuccess() {
        // given
        Mentoring.MentoringType mentoringType = Mentoring.MentoringType.MENTOR;
        given(mentoringRepository.findByMentoringTypeAndStatusOrderByCreatedAtDesc(
                mentoringType, Mentoring.MentoringStatus.ACTIVE)).willReturn(List.of(mentoring));

        // when
        List<MentoringResponse> result = mentoringService.getMentoringsByType(mentoringType);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMentoringType()).isEqualTo(mentoringType);
        verify(mentoringRepository).findByMentoringTypeAndStatusOrderByCreatedAtDesc(
                mentoringType, Mentoring.MentoringStatus.ACTIVE);
    }

    @Test
    @DisplayName("카테고리별 조회 성공 테스트")
    void getMentoringsByCategorySuccess() {
        // given
        Mentoring.Category category = Mentoring.Category.CROP_CULTIVATION;
        given(mentoringRepository.findByCategoryAndStatusOrderByCreatedAtDesc(
                category, Mentoring.MentoringStatus.ACTIVE)).willReturn(List.of(mentoring));

        // when
        List<MentoringResponse> result = mentoringService.getMentoringsByCategory(category);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo(category);
        verify(mentoringRepository).findByCategoryAndStatusOrderByCreatedAtDesc(
                category, Mentoring.MentoringStatus.ACTIVE);
    }
}
