package com.jeju_nongdi.jeju_nongdi.service;

import com.jeju_nongdi.jeju_nongdi.dto.MentoringRequest;
import com.jeju_nongdi.jeju_nongdi.dto.MentoringResponse;
import com.jeju_nongdi.jeju_nongdi.entity.Mentoring;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.repository.MentoringRepository;
import com.jeju_nongdi.jeju_nongdi.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MentoringService {

    private final MentoringRepository mentoringRepository;
    private final UserRepository userRepository;

    /**
     * 멘토링 글 생성
     */
    public MentoringResponse createMentoring(MentoringRequest request, UserDetails userDetails) {
        log.info("Creating mentoring post: {}", request.getTitle());

        // 연락처 정보 유효성 검증
        if (!request.isContactInfoValid()) {
            throw new IllegalArgumentException("연락처 정보(전화번호 또는 이메일) 중 하나는 필수입니다.");
        }

        User user = getUserByEmail(userDetails.getUsername());

        Mentoring mentoring = Mentoring.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .mentoringType(request.getMentoringType())
                .category(request.getCategory())
                .experienceLevel(request.getExperienceLevel())
                .preferredLocation(request.getPreferredLocation())
                .preferredSchedule(request.getPreferredSchedule())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .user(user)
                .build();

        Mentoring savedMentoring = mentoringRepository.save(mentoring);
        log.info("Created mentoring post with ID: {}", savedMentoring.getId());

        return MentoringResponse.from(savedMentoring);
    }

    /**
     * 멘토링 글 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<MentoringResponse> getMentorings(Pageable pageable) {
        log.info("Fetching mentoring posts with pagination");
        
        Page<Mentoring> mentorings = mentoringRepository.findByStatus(Mentoring.MentoringStatus.ACTIVE, pageable);
        return mentorings.map(MentoringResponse::from);
    }

    /**
     * 멘토링 글 상세 조회
     */
    @Transactional(readOnly = true)
    public MentoringResponse getMentoring(Long id) {
        log.info("Fetching mentoring post with ID: {}", id);
        
        Mentoring mentoring = mentoringRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("멘토링 글을 찾을 수 없습니다: " + id));

        return MentoringResponse.from(mentoring);
    }

    /**
     * 멘토링 글 수정
     */
    public MentoringResponse updateMentoring(Long id, MentoringRequest request, UserDetails userDetails) {
        log.info("Updating mentoring post with ID: {}", id);

        // 연락처 정보 유효성 검증
        if (!request.isContactInfoValid()) {
            throw new IllegalArgumentException("연락처 정보(전화번호 또는 이메일) 중 하나는 필수입니다.");
        }

        Mentoring mentoring = mentoringRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("멘토링 글을 찾을 수 없습니다: " + id));

        // 작성자 권한 확인
        if (!mentoring.getUser().getEmail().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("자신이 작성한 글만 수정할 수 있습니다.");
        }

        // 필드 업데이트
        mentoring.setTitle(request.getTitle());
        mentoring.setDescription(request.getDescription());
        mentoring.setMentoringType(request.getMentoringType());
        mentoring.setCategory(request.getCategory());
        mentoring.setExperienceLevel(request.getExperienceLevel());
        mentoring.setPreferredLocation(request.getPreferredLocation());
        mentoring.setPreferredSchedule(request.getPreferredSchedule());
        mentoring.setContactPhone(request.getContactPhone());
        mentoring.setContactEmail(request.getContactEmail());

        Mentoring updatedMentoring = mentoringRepository.save(mentoring);
        log.info("Updated mentoring post with ID: {}", updatedMentoring.getId());

        return MentoringResponse.from(updatedMentoring);
    }

    /**
     * 멘토링 글 삭제
     */
    public void deleteMentoring(Long id, UserDetails userDetails) {
        log.info("Deleting mentoring post with ID: {}", id);

        Mentoring mentoring = mentoringRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("멘토링 글을 찾을 수 없습니다: " + id));

        // 작성자 권한 확인
        if (!mentoring.getUser().getEmail().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("자신이 작성한 글만 삭제할 수 있습니다.");
        }

        mentoringRepository.delete(mentoring);
        log.info("Deleted mentoring post with ID: {}", id);
    }

    /**
     * 멘토링 상태 변경
     */
    public MentoringResponse updateMentoringStatus(Long id, Mentoring.MentoringStatus status, UserDetails userDetails) {
        log.info("Updating mentoring status for ID: {} to {}", id, status);

        Mentoring mentoring = mentoringRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("멘토링 글을 찾을 수 없습니다: " + id));

        // 작성자 권한 확인
        if (!mentoring.getUser().getEmail().equals(userDetails.getUsername())) {
            throw new AccessDeniedException("자신이 작성한 글만 상태를 변경할 수 있습니다.");
        }

        mentoring.setStatus(status);
        Mentoring updatedMentoring = mentoringRepository.save(mentoring);

        return MentoringResponse.from(updatedMentoring);
    }

    /**
     * 내가 작성한 멘토링 글 목록 조회
     */
    @Transactional(readOnly = true)
    public List<MentoringResponse> getMyMentorings(UserDetails userDetails) {
        log.info("Fetching my mentoring posts");

        User user = getUserByEmail(userDetails.getUsername());
        List<Mentoring> mentorings = mentoringRepository.findByUserOrderByCreatedAtDesc(user);

        return mentorings.stream()
                .map(MentoringResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 멘토링 검색
     */
    @Transactional(readOnly = true)
    public List<MentoringResponse> searchMentorings(Mentoring.MentoringType mentoringType,
                                                   Mentoring.Category category,
                                                   Mentoring.ExperienceLevel experienceLevel,
                                                   String location,
                                                   String keyword) {
        log.info("Searching mentoring posts with filters");

        List<Mentoring> mentorings;

        if (keyword != null && !keyword.trim().isEmpty()) {
            // 키워드 검색
            mentorings = mentoringRepository.findByKeywordAndStatus(keyword, Mentoring.MentoringStatus.ACTIVE);
        } else {
            // 필터 검색
            mentorings = mentoringRepository.searchMentorings(
                    mentoringType, category, experienceLevel, location, Mentoring.MentoringStatus.ACTIVE);
        }

        return mentorings.stream()
                .map(MentoringResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 멘토링 타입별 조회
     */
    @Transactional(readOnly = true)
    public List<MentoringResponse> getMentoringsByType(Mentoring.MentoringType mentoringType) {
        log.info("Fetching mentoring posts by type: {}", mentoringType);

        List<Mentoring> mentorings = mentoringRepository.findByMentoringTypeAndStatus(
                mentoringType, Mentoring.MentoringStatus.ACTIVE);

        return mentorings.stream()
                .map(MentoringResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리별 조회
     */
    @Transactional(readOnly = true)
    public List<MentoringResponse> getMentoringsByCategory(Mentoring.Category category) {
        log.info("Fetching mentoring posts by category: {}", category);

        List<Mentoring> mentorings = mentoringRepository.findByCategoryAndStatus(
                category, Mentoring.MentoringStatus.ACTIVE);

        return mentorings.stream()
                .map(MentoringResponse::from)
                .collect(Collectors.toList());
    }

    // Private helper methods
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }
}
