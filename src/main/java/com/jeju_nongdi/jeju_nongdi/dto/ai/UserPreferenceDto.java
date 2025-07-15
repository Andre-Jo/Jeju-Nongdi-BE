package com.jeju_nongdi.jeju_nongdi.dto.ai;

import com.jeju_nongdi.jeju_nongdi.entity.UserPreference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferenceDto {
    
    private Long id;
    private List<String> primaryCrops;
    private String farmLocation;
    private Double farmSize;
    private Integer farmingExperience;
    private Boolean notificationWeather;
    private Boolean notificationPest;
    private Boolean notificationMarket;
    private Boolean notificationLabor;
    private String preferredTipTime;
    private String farmingType;
    private String farmingTypeDescription;
    
    public static UserPreferenceDto from(UserPreference preference) {
        return UserPreferenceDto.builder()
                .id(preference.getId())
                .primaryCrops(preference.getPrimaryCropsList())
                .farmLocation(preference.getFarmLocation())
                .farmSize(preference.getFarmSize())
                .farmingExperience(preference.getFarmingExperience())
                .notificationWeather(preference.getNotificationWeather())
                .notificationPest(preference.getNotificationPest())
                .notificationMarket(preference.getNotificationMarket())
                .notificationLabor(preference.getNotificationLabor())
                .preferredTipTime(preference.getPreferredTipTime())
                .farmingType(preference.getFarmingType() != null ? preference.getFarmingType().name() : null)
                .farmingTypeDescription(preference.getFarmingType() != null ? preference.getFarmingType().getDescription() : null)
                .build();
    }
    
    public UserPreference toEntity() {
        UserPreference preference = UserPreference.builder()
                .farmLocation(this.farmLocation)
                .farmSize(this.farmSize)
                .farmingExperience(this.farmingExperience)
                .notificationWeather(this.notificationWeather)
                .notificationPest(this.notificationPest)
                .notificationMarket(this.notificationMarket)
                .notificationLabor(this.notificationLabor)
                .preferredTipTime(this.preferredTipTime)
                .build();
        
        preference.setPrimaryCropsList(this.primaryCrops);
        
        if (this.farmingType != null) {
            preference.setFarmingType(UserPreference.FarmingType.valueOf(this.farmingType));
        }
        
        return preference;
    }
}
