package com.jeju_nongdi.jeju_nongdi.service;

import com.jeju_nongdi.jeju_nongdi.dto.ai.UserPreferenceDto;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.entity.UserPreference;
import com.jeju_nongdi.jeju_nongdi.repository.UserPreferenceRepository;
import com.jeju_nongdi.jeju_nongdi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserPreferenceService {
    
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserRepository userRepository;
    
    /**
     * 사용자 설정 조회
     */
    @Transactional(readOnly = true)
    public UserPreference getUserPreference(Long userId) {
        return userPreferenceRepository.findByUserId(userId)
                .orElse(null);
    }
    
    /**
     * 사용자 설정 조회 (DTO)
     */
    @Transactional(readOnly = true)
    public UserPreferenceDto getUserPreferenceDto(Long userId) {
        Optional<UserPreference> preference = userPreferenceRepository.findByUserId(userId);
        return preference.map(UserPreferenceDto::from).orElse(null);
    }
    
    /**
     * 사용자 설정 생성 또는 업데이트
     */
    public UserPreferenceDto createOrUpdateUserPreference(Long userId, UserPreferenceDto preferenceDto) {
        User user = getUserById(userId);
        
        Optional<UserPreference> existingPreference = userPreferenceRepository.findByUser(user);
        
        UserPreference preference;
        if (existingPreference.isPresent()) {
            // 기존 설정 업데이트
            preference = existingPreference.get();
            updatePreferenceFromDto(preference, preferenceDto);
            log.info("사용자 {} 설정 업데이트", userId);
        } else {
            // 새 설정 생성
            preference = preferenceDto.toEntity();
            preference.setUser(user);
            log.info("사용자 {} 설정 생성", userId);
        }
        
        UserPreference savedPreference = userPreferenceRepository.save(preference);
        return UserPreferenceDto.from(savedPreference);
    }
    
    /**
     * 사용자 설정 삭제
     */
    public void deleteUserPreference(Long userId) {
        Optional<UserPreference> preference = userPreferenceRepository.findByUserId(userId);
        if (preference.isPresent()) {
            userPreferenceRepository.delete(preference.get());
            log.info("사용자 {} 설정 삭제", userId);
        }
    }
    
    /**
     * 특정 작물을 기르는 사용자들 조회
     */
    @Transactional(readOnly = true)
    public List<UserPreference> getUsersByPrimaryCrop(String cropName) {
        return userPreferenceRepository.findByPrimaryCropsContaining(cropName);
    }
    
    /**
     * 특정 지역의 사용자들 조회
     */
    @Transactional(readOnly = true)
    public List<UserPreference> getUsersByLocation(String location) {
        return userPreferenceRepository.findByFarmLocationContaining(location);
    }
    
    /**
     * 특정 알림을 활성화한 사용자들 조회
     */
    @Transactional(readOnly = true)
    public List<UserPreference> getUsersByNotificationType(String notificationType) {
        return userPreferenceRepository.findByNotificationTypeEnabled(notificationType);
    }
    
    /**
     * 설정이 없는 사용자들 조회
     */
    @Transactional(readOnly = true)
    public List<User> getUsersWithoutPreferences() {
        return userPreferenceRepository.findUsersWithoutPreferences();
    }
    
    /**
     * 사용자 맞춤 설정 검증
     */
    public boolean isValidPreference(UserPreferenceDto preferenceDto) {
        if (preferenceDto == null) {
            return false;
        }
        
        // 농장 크기 검증
        if (preferenceDto.getFarmSize() != null && preferenceDto.getFarmSize() <= 0) {
            return false;
        }
        
        // 농업 경력 검증
        if (preferenceDto.getFarmingExperience() != null && preferenceDto.getFarmingExperience() < 0) {
            return false;
        }
        
        // 알림 시간 검증
        if (preferenceDto.getPreferredTipTime() != null) {
            try {
                String[] timeParts = preferenceDto.getPreferredTipTime().split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 기본 설정 생성
     */
    public UserPreferenceDto createDefaultPreference(Long userId) {
        UserPreferenceDto defaultPreference = UserPreferenceDto.builder()
                .primaryCrops(List.of("감귤"))
                .farmLocation("제주특별자치도")
                .farmSize(1000.0)
                .farmingExperience(1)
                .notificationWeather(true)
                .notificationPest(true)
                .notificationMarket(true)
                .notificationLabor(true)
                .preferredTipTime("08:00")
                .farmingType("TRADITIONAL")
                .build();
        
        return createOrUpdateUserPreference(userId, defaultPreference);
    }
    
    // === Private Helper Methods ===
    
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }
    
    private void updatePreferenceFromDto(UserPreference preference, UserPreferenceDto dto) {
        if (dto.getPrimaryCrops() != null) {
            preference.setPrimaryCropsList(dto.getPrimaryCrops());
        }
        if (dto.getFarmLocation() != null) {
            preference.setFarmLocation(dto.getFarmLocation());
        }
        if (dto.getFarmSize() != null) {
            preference.setFarmSize(dto.getFarmSize());
        }
        if (dto.getFarmingExperience() != null) {
            preference.setFarmingExperience(dto.getFarmingExperience());
        }
        if (dto.getNotificationWeather() != null) {
            preference.setNotificationWeather(dto.getNotificationWeather());
        }
        if (dto.getNotificationPest() != null) {
            preference.setNotificationPest(dto.getNotificationPest());
        }
        if (dto.getNotificationMarket() != null) {
            preference.setNotificationMarket(dto.getNotificationMarket());
        }
        if (dto.getNotificationLabor() != null) {
            preference.setNotificationLabor(dto.getNotificationLabor());
        }
        if (dto.getPreferredTipTime() != null) {
            preference.setPreferredTipTime(dto.getPreferredTipTime());
        }
        if (dto.getFarmingType() != null) {
            preference.setFarmingType(UserPreference.FarmingType.valueOf(dto.getFarmingType()));
        }
    }
}
