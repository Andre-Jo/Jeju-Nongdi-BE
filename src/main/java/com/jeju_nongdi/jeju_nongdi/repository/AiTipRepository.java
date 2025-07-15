package com.jeju_nongdi.jeju_nongdi.repository;

import com.jeju_nongdi.jeju_nongdi.entity.AiTip;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AiTipRepository extends JpaRepository<AiTip, Long> {
    
    // 특정 사용자의 특정 날짜 팁들 조회
    List<AiTip> findByUserAndTargetDateOrderByPriorityLevelDescCreatedAtDesc(User user, LocalDate targetDate);
    
    // 특정 사용자의 읽지 않은 팁들 조회
    List<AiTip> findByUserAndIsReadFalseOrderByPriorityLevelDescCreatedAtDesc(User user);
    
    // 특정 사용자의 특정 날짜 읽지 않은 팁들 조회
    List<AiTip> findByUserAndTargetDateAndIsReadFalseOrderByPriorityLevelDescCreatedAtDesc(User user, LocalDate targetDate);
    
    // 특정 사용자의 특정 팁 유형들 조회
    List<AiTip> findByUserAndTipTypeInAndTargetDateOrderByPriorityLevelDescCreatedAtDesc(
            User user, List<AiTip.TipType> tipTypes, LocalDate targetDate);
    
    // 특정 사용자의 특정 작물 관련 팁들 조회
    List<AiTip> findByUserAndCropTypeAndTargetDateOrderByPriorityLevelDescCreatedAtDesc(
            User user, String cropType, LocalDate targetDate);
    
    // 특정 사용자의 우선순위 이상 팁들 조회
    List<AiTip> findByUserAndPriorityLevelGreaterThanEqualAndTargetDateOrderByPriorityLevelDescCreatedAtDesc(
            User user, Integer priorityLevel, LocalDate targetDate);
    
    // 특정 사용자의 읽지 않은 팁 개수
    @Query("SELECT COUNT(t) FROM AiTip t WHERE t.user = :user AND t.isRead = false")
    Integer countUnreadTipsByUser(@Param("user") User user);
    
    // 특정 사용자의 특정 날짜 긴급 팁 개수
    @Query("SELECT COUNT(t) FROM AiTip t WHERE t.user = :user AND t.targetDate = :targetDate AND t.priorityLevel = 4")
    Integer countUrgentTipsByUserAndDate(@Param("user") User user, @Param("targetDate") LocalDate targetDate);
    
    // 특정 사용자의 날짜 범위 내 팁들 조회
    List<AiTip> findByUserAndTargetDateBetweenOrderByTargetDateDescPriorityLevelDescCreatedAtDesc(
            User user, LocalDate startDate, LocalDate endDate);
    
    // 특정 사용자의 팁 유형별 개수 조회
    @Query("SELECT t.tipType, COUNT(t) FROM AiTip t WHERE t.user = :user AND t.targetDate = :targetDate GROUP BY t.tipType")
    List<Object[]> countTipsByTypeAndUserAndDate(@Param("user") User user, @Param("targetDate") LocalDate targetDate);
}
