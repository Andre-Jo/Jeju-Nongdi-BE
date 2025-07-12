package com.jeju_nongdi.jeju_nongdi.repository;

import com.jeju_nongdi.jeju_nongdi.entity.IdleFarmland;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface IdleFarmlandRepository extends JpaRepository<IdleFarmland, Long> {

    // 상태별 조회
    Page<IdleFarmland> findByStatus(IdleFarmland.FarmlandStatus status, Pageable pageable);
    
    // 테스트 호환성을 위한 메서드들
    Page<IdleFarmland> findByStatusOrderByCreatedAtDesc(IdleFarmland.FarmlandStatus status, Pageable pageable);
    List<IdleFarmland> findByStatusOrderByCreatedAtDesc(IdleFarmland.FarmlandStatus status);

    // 소유자별 조회
    List<IdleFarmland> findByOwner(User owner);
    List<IdleFarmland> findByOwnerOrderByCreatedAtDesc(User owner);

    // 이용 유형별 조회
    List<IdleFarmland> findByUsageType(IdleFarmland.UsageType usageType);

    // 주소로 검색
    @Query("SELECT f FROM IdleFarmland f WHERE f.address LIKE %:address% AND f.status = 'AVAILABLE'")
    List<IdleFarmland> findByAddressContainingAndStatusAvailable(@Param("address") String address);
    
    // 테스트를 위한 주소와 상태로 검색
    List<IdleFarmland> findByAddressContainingAndStatusOrderByCreatedAtDesc(String address, IdleFarmland.FarmlandStatus status);

    // 면적 범위로 검색
    @Query("SELECT f FROM IdleFarmland f WHERE f.areaSize BETWEEN :minArea AND :maxArea AND f.status = 'AVAILABLE'")
    List<IdleFarmland> findByAreaSizeBetweenAndStatusAvailable(
            @Param("minArea") BigDecimal minArea, 
            @Param("maxArea") BigDecimal maxArea
    );

    // 임대료 범위로 검색
    @Query("SELECT f FROM IdleFarmland f WHERE f.monthlyRent BETWEEN :minRent AND :maxRent AND f.status = 'AVAILABLE'")
    List<IdleFarmland> findByMonthlyRentBetweenAndStatusAvailable(
            @Param("minRent") Integer minRent, 
            @Param("maxRent") Integer maxRent
    );

    // 복합 검색 (지역, 이용유형, 면적, 임대료)
    @Query("SELECT f FROM IdleFarmland f WHERE " +
           "(:address IS NULL OR f.address LIKE %:address%) AND " +
           "(:usageType IS NULL OR f.usageType = :usageType) AND " +
           "(:soilType IS NULL OR f.soilType = :soilType) AND " +
           "(:minArea IS NULL OR f.areaSize >= :minArea) AND " +
           "(:maxArea IS NULL OR f.areaSize <= :maxArea) AND " +
           "(:minRent IS NULL OR f.monthlyRent >= :minRent) AND " +
           "(:maxRent IS NULL OR f.monthlyRent <= :maxRent) AND " +
           "f.status = 'AVAILABLE'")
    List<IdleFarmland> findAvailableFarmlandsWithFilters(
            @Param("address") String address,
            @Param("usageType") IdleFarmland.UsageType usageType,
            @Param("soilType") IdleFarmland.SoilType soilType,
            @Param("minArea") BigDecimal minArea,
            @Param("maxArea") BigDecimal maxArea,
            @Param("minRent") Integer minRent,
            @Param("maxRent") Integer maxRent
    );
    
    // 테스트를 위한 복합 검색 (실제 필드명 기준)
    @Query("SELECT f FROM IdleFarmland f WHERE " +
           "(:address IS NULL OR f.address LIKE %:address%) AND " +
           "(:usageType IS NULL OR f.usageType = :usageType) AND " +
           "(:soilType IS NULL OR f.soilType = :soilType) AND " +
           "(:minArea IS NULL OR f.areaSize >= :minArea) AND " +
           "(:maxArea IS NULL OR f.areaSize <= :maxArea) AND " +
           "(:minRent IS NULL OR f.monthlyRent >= :minRent) AND " +
           "(:maxRent IS NULL OR f.monthlyRent <= :maxRent) AND " +
           "f.status = :status " +
           "ORDER BY f.createdAt DESC")
    List<IdleFarmland> findByAddressContainingAndUsageTypeAndSoilTypeAndAreaBetweenAndRentPriceBetweenAndStatusOrderByCreatedAtDesc(
            @Param("address") String address,
            @Param("usageType") IdleFarmland.UsageType usageType,
            @Param("soilType") IdleFarmland.SoilType soilType,
            @Param("minArea") BigDecimal minArea,
            @Param("maxArea") BigDecimal maxArea,
            @Param("minRent") Integer minRent,
            @Param("maxRent") Integer maxRent,
            @Param("status") IdleFarmland.FarmlandStatus status
    );

    // 지도 마커용 - 이용 가능한 농지만
    @Query("SELECT f FROM IdleFarmland f WHERE f.status = 'AVAILABLE'")
    List<IdleFarmland> findAllAvailableForMap();

    // 지역별 지도 마커용
    @Query("SELECT f FROM IdleFarmland f WHERE f.address LIKE %:region% AND f.status = 'AVAILABLE'")
    List<IdleFarmland> findByRegionForMap(@Param("region") String region);

    // 인기 농지 (최신순)
    @Query("SELECT f FROM IdleFarmland f WHERE f.status = 'AVAILABLE' ORDER BY f.createdAt DESC")
    List<IdleFarmland> findRecentAvailableFarmlands(Pageable pageable);
}
