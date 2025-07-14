package com.jeju_nongdi.jeju_nongdi.repository;

import com.jeju_nongdi.jeju_nongdi.entity.CropInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CropInfoRepository extends JpaRepository<CropInfo, Long> {
    
    // 작물명으로 조회
    Optional<CropInfo> findByCropName(String cropName);
    
    // 작물명 부분 검색
    List<CropInfo> findByCropNameContainingIgnoreCase(String cropName);
    
    // 작물 분류별 조회
    List<CropInfo> findByCropCategory(String cropCategory);
    
    // 제주 특산물 조회
    List<CropInfo> findByIsJejuSpecialtyTrue();
    
    // 파종 시기에 맞는 작물들 조회
    @Query("SELECT c FROM CropInfo c WHERE c.plantingSeason LIKE %:month%")
    List<CropInfo> findByPlantingSeasonContaining(@Param("month") String month);
    
    // 수확 시기에 맞는 작물들 조회
    @Query("SELECT c FROM CropInfo c WHERE c.harvestSeason LIKE %:month%")
    List<CropInfo> findByHarvestSeasonContaining(@Param("month") String month);
    
    // 생육 기간별 조회
    List<CropInfo> findByGrowthPeriodBetween(Integer minDays, Integer maxDays);
    
    // 물 요구량별 조회
    List<CropInfo> findByWaterRequirement(String waterRequirement);
    
    // 작물명 목록 조회 (자동완성용)
    @Query("SELECT c.cropName FROM CropInfo c ORDER BY c.cropName")
    List<String> findAllCropNames();
    
    // 카테고리 목록 조회
    @Query("SELECT DISTINCT c.cropCategory FROM CropInfo c WHERE c.cropCategory IS NOT NULL ORDER BY c.cropCategory")
    List<String> findAllCategories();
    
    // 특정 병해충이 있는 작물들 조회
    @Query("SELECT c FROM CropInfo c WHERE c.commonPests LIKE %:pest%")
    List<CropInfo> findByCommonPestsContaining(@Param("pest") String pest);
}
