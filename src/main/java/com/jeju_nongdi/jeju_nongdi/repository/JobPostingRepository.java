package com.jeju_nongdi.jeju_nongdi.repository;

import com.jeju_nongdi.jeju_nongdi.entity.JobPosting;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    // 활성 상태의 공고만 조회
    List<JobPosting> findByStatusOrderByCreatedAtDesc(JobPosting.JobStatus status);

    // 페이징을 지원하는 활성 공고 조회
    Page<JobPosting> findByStatusOrderByCreatedAtDesc(JobPosting.JobStatus status, Pageable pageable);

    // 작물 타입별 조회
    List<JobPosting> findByCropTypeAndStatusOrderByCreatedAtDesc(
            JobPosting.CropType cropType, 
            JobPosting.JobStatus status
    );

    // 작업 타입별 조회
    List<JobPosting> findByWorkTypeAndStatusOrderByCreatedAtDesc(
            JobPosting.WorkType workType, 
            JobPosting.JobStatus status
    );

    // 지역별 조회 (주소 포함)
    List<JobPosting> findByAddressContainingAndStatusOrderByCreatedAtDesc(
            String address, 
            JobPosting.JobStatus status
    );

    // 특정 사용자가 작성한 공고 조회
    List<JobPosting> findByAuthorOrderByCreatedAtDesc(User author);

    // 기간별 조회
    List<JobPosting> findByWorkStartDateGreaterThanEqualAndWorkEndDateLessThanEqualAndStatusOrderByCreatedAtDesc(
            LocalDate startDate, 
            LocalDate endDate, 
            JobPosting.JobStatus status
    );

    // 지도 마커용 데이터 조회 (위도, 경도, 기본정보만)
    @Query("SELECT jp FROM JobPosting jp WHERE jp.status = :status")
    List<JobPosting> findAllForMap(@Param("status") JobPosting.JobStatus status);

    // 복합 필터링 조회
    @Query("""
        SELECT jp FROM JobPosting jp 
        WHERE jp.status = :status 
        AND (:cropType IS NULL OR jp.cropType = :cropType)
        AND (:workType IS NULL OR jp.workType = :workType)
        AND (:address IS NULL OR jp.address LIKE %:address%)
        AND jp.workStartDate >= :currentDate
        ORDER BY jp.createdAt DESC
        """)
    List<JobPosting> findWithFilters(
            @Param("status") JobPosting.JobStatus status,
            @Param("cropType") JobPosting.CropType cropType,
            @Param("workType") JobPosting.WorkType workType,
            @Param("address") String address,
            @Param("currentDate") LocalDate currentDate
    );

    // 복합 필터링 조회 (페이징)
    @Query("""
        SELECT jp FROM JobPosting jp 
        WHERE jp.status = :status 
        AND (:cropType IS NULL OR jp.cropType = :cropType)
        AND (:workType IS NULL OR jp.workType = :workType)
        AND (:address IS NULL OR jp.address LIKE %:address%)
        AND jp.workStartDate >= :currentDate
        ORDER BY jp.createdAt DESC
        """)
    Page<JobPosting> findWithFilters(
            @Param("status") JobPosting.JobStatus status,
            @Param("cropType") JobPosting.CropType cropType,
            @Param("workType") JobPosting.WorkType workType,
            @Param("address") String address,
            @Param("currentDate") LocalDate currentDate,
            Pageable pageable
    );

    // 작성자 ID로 조회
    List<JobPosting> findByAuthor_IdOrderByCreatedAtDesc(Long authorId);

    // 급여 범위별 조회
    List<JobPosting> findByWagesBetweenAndStatusOrderByCreatedAtDesc(
            Integer minWages, 
            Integer maxWages, 
            JobPosting.JobStatus status
    );
}
