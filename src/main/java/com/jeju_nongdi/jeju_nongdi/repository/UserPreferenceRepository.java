package com.jeju_nongdi.jeju_nongdi.repository;

import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    
    // 특정 사용자의 설정 조회
    Optional<UserPreference> findByUser(User user);
    
    // 특정 사용자 ID로 설정 조회
    Optional<UserPreference> findByUserId(Long userId);
    
    // 특정 작물을 기르는 사용자들 조회
    @Query("SELECT up FROM UserPreference up WHERE up.primaryCrops LIKE %:cropName%")
    List<UserPreference> findByPrimaryCropsContaining(@Param("cropName") String cropName);
    
    // 특정 지역의 사용자들 조회
    List<UserPreference> findByFarmLocationContaining(String location);
    
    // 특정 농업 유형의 사용자들 조회
    List<UserPreference> findByFarmingType(UserPreference.FarmingType farmingType);
    
    // 특정 알림을 활성화한 사용자들 조회
    @Query("SELECT up FROM UserPreference up WHERE " +
           "(up.notificationWeather = true AND :notificationType = 'WEATHER') OR " +
           "(up.notificationPest = true AND :notificationType = 'PEST') OR " +
           "(up.notificationMarket = true AND :notificationType = 'MARKET') OR " +
           "(up.notificationLabor = true AND :notificationType = 'LABOR')")
    List<UserPreference> findByNotificationTypeEnabled(@Param("notificationType") String notificationType);
    
    // 농업 경력별 사용자들 조회
    List<UserPreference> findByFarmingExperienceGreaterThanEqual(Integer experience);
    
    // 농장 크기별 사용자들 조회
    List<UserPreference> findByFarmSizeGreaterThanEqual(Double size);
    
    // 설정이 없는 사용자 확인
    @Query("SELECT u FROM User u WHERE u.id NOT IN (SELECT up.user.id FROM UserPreference up)")
    List<User> findUsersWithoutPreferences();
}
