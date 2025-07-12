package com.jeju_nongdi.jeju_nongdi.repository;

import com.jeju_nongdi.jeju_nongdi.entity.Mentoring;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentoringRepository extends JpaRepository<Mentoring, Long> {

    // 상태별 멘토링 목록 조회 (페이징)
    Page<Mentoring> findByStatus(Mentoring.MentoringStatus status, Pageable pageable);
    
    // 상태별 멘토링 목록 조회 (페이징, 테스트 호환성)
    Page<Mentoring> findByStatusOrderByCreatedAtDesc(Mentoring.MentoringStatus status, Pageable pageable);

    // 특정 사용자의 멘토링 목록 조회
    List<Mentoring> findByUserOrderByCreatedAtDesc(User user);
    
    // 테스트 호환성을 위한 author 메서드 (실제로는 user 필드를 사용)
    default List<Mentoring> findByAuthorOrderByCreatedAtDesc(User user) {
        return findByUserOrderByCreatedAtDesc(user);
    }

    // 멘토링 타입별 조회
    List<Mentoring> findByMentoringTypeAndStatus(Mentoring.MentoringType mentoringType, Mentoring.MentoringStatus status);
    
    // 테스트 호환성을 위한 OrderBy 포함 메서드
    List<Mentoring> findByMentoringTypeAndStatusOrderByCreatedAtDesc(Mentoring.MentoringType mentoringType, Mentoring.MentoringStatus status);

    // 카테고리별 조회
    List<Mentoring> findByCategoryAndStatus(Mentoring.Category category, Mentoring.MentoringStatus status);
    
    // 테스트 호환성을 위한 OrderBy 포함 메서드
    List<Mentoring> findByCategoryAndStatusOrderByCreatedAtDesc(Mentoring.Category category, Mentoring.MentoringStatus status);

    // 경험 수준별 조회
    List<Mentoring> findByExperienceLevelAndStatus(Mentoring.ExperienceLevel experienceLevel, Mentoring.MentoringStatus status);

    // 지역별 조회 (preferredLocation 포함)
    @Query("SELECT m FROM Mentoring m WHERE m.status = :status AND " +
           "(:location IS NULL OR m.preferredLocation LIKE %:location%)")
    List<Mentoring> findByLocationAndStatus(@Param("location") String location, 
                                          @Param("status") Mentoring.MentoringStatus status);

    // 복합 검색 쿼리
    @Query("SELECT m FROM Mentoring m WHERE " +
           "(:mentoringType IS NULL OR m.mentoringType = :mentoringType) AND " +
           "(:category IS NULL OR m.category = :category) AND " +
           "(:experienceLevel IS NULL OR m.experienceLevel = :experienceLevel) AND " +
           "(:location IS NULL OR m.preferredLocation LIKE %:location%) AND " +
           "m.status = :status " +
           "ORDER BY m.createdAt DESC")
    List<Mentoring> searchMentorings(@Param("mentoringType") Mentoring.MentoringType mentoringType,
                                   @Param("category") Mentoring.Category category,
                                   @Param("experienceLevel") Mentoring.ExperienceLevel experienceLevel,
                                   @Param("location") String location,
                                   @Param("status") Mentoring.MentoringStatus status);

    // ACTIVE 상태의 모든 멘토링 조회 (최신순)
    List<Mentoring> findByStatusOrderByCreatedAtDesc(Mentoring.MentoringStatus status);

    // 제목으로 검색
    @Query("SELECT m FROM Mentoring m WHERE m.status = :status AND " +
           "(:keyword IS NULL OR m.title LIKE %:keyword% OR m.description LIKE %:keyword%)")
    List<Mentoring> findByKeywordAndStatus(@Param("keyword") String keyword, 
                                         @Param("status") Mentoring.MentoringStatus status);
}
