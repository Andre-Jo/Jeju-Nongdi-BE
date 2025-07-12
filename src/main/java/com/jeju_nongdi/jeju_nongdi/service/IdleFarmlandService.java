package com.jeju_nongdi.jeju_nongdi.service;

import com.jeju_nongdi.jeju_nongdi.dto.*;
import com.jeju_nongdi.jeju_nongdi.entity.IdleFarmland;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.repository.IdleFarmlandRepository;
import com.jeju_nongdi.jeju_nongdi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IdleFarmlandService {

    private final IdleFarmlandRepository idleFarmlandRepository;
    private final UserRepository userRepository;

    /**
     * 유휴 농지 등록
     */
    @Transactional
    public IdleFarmlandResponse createIdleFarmland(IdleFarmlandRequest request, UserDetails userDetails) {
        log.info("Creating idle farmland for user: {}", userDetails.getUsername());

        User owner = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        IdleFarmland idleFarmland = IdleFarmland.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .farmlandName(request.getFarmlandName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .areaSize(request.getAreaSize())
                .soilType(request.getSoilType())
                .usageType(request.getUsageType())
                .monthlyRent(request.getMonthlyRent())
                .availableStartDate(request.getAvailableStartDate())
                .availableEndDate(request.getAvailableEndDate())
                .waterSupply(request.getWaterSupply())
                .electricitySupply(request.getElectricitySupply())
                .farmingToolsIncluded(request.getFarmingToolsIncluded())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .owner(owner)
                .build();

        IdleFarmland savedFarmland = idleFarmlandRepository.save(idleFarmland);
        log.info("Created idle farmland with ID: {}", savedFarmland.getId());

        return IdleFarmlandResponse.from(savedFarmland);
    }

    /**
     * 유휴 농지 목록 조회 (페이징)
     */
    public Page<IdleFarmlandResponse> getIdleFarmlands(Pageable pageable) {
        log.info("Fetching idle farmlands with pagination");
        
        return idleFarmlandRepository.findAll(pageable)
                .map(IdleFarmlandResponse::from);
    }

    /**
     * 유휴 농지 상세 조회
     */
    public IdleFarmlandResponse getIdleFarmland(Long id) {
        log.info("Fetching idle farmland with ID: {}", id);

        IdleFarmland idleFarmland = idleFarmlandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("농지를 찾을 수 없습니다."));

        return IdleFarmlandResponse.from(idleFarmland);
    }

    /**
     * 유휴 농지 수정
     */
    @Transactional
    public IdleFarmlandResponse updateIdleFarmland(Long id, IdleFarmlandRequest request, UserDetails userDetails) {
        log.info("Updating idle farmland with ID: {}", id);

        IdleFarmland idleFarmland = idleFarmlandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("농지를 찾을 수 없습니다."));

        if (!idleFarmland.getOwner().getEmail().equals(userDetails.getUsername())) {
            throw new RuntimeException("자신의 농지만 수정할 수 있습니다.");
        }

        // 엔티티 업데이트
        idleFarmland.setTitle(request.getTitle());
        idleFarmland.setDescription(request.getDescription());
        idleFarmland.setFarmlandName(request.getFarmlandName());
        idleFarmland.setAddress(request.getAddress());
        idleFarmland.setLatitude(request.getLatitude());
        idleFarmland.setLongitude(request.getLongitude());
        idleFarmland.setAreaSize(request.getAreaSize());
        idleFarmland.setSoilType(request.getSoilType());
        idleFarmland.setUsageType(request.getUsageType());
        idleFarmland.setMonthlyRent(request.getMonthlyRent());
        idleFarmland.setAvailableStartDate(request.getAvailableStartDate());
        idleFarmland.setAvailableEndDate(request.getAvailableEndDate());
        idleFarmland.setWaterSupply(request.getWaterSupply());
        idleFarmland.setElectricitySupply(request.getElectricitySupply());
        idleFarmland.setFarmingToolsIncluded(request.getFarmingToolsIncluded());
        idleFarmland.setContactPhone(request.getContactPhone());
        idleFarmland.setContactEmail(request.getContactEmail());

        IdleFarmland updatedFarmland = idleFarmlandRepository.save(idleFarmland);
        log.info("Updated idle farmland with ID: {}", id);

        return IdleFarmlandResponse.from(updatedFarmland);
    }

    /**
     * 유휴 농지 삭제
     */
    @Transactional
    public void deleteIdleFarmland(Long id, UserDetails userDetails) {
        log.info("Deleting idle farmland with ID: {}", id);

        IdleFarmland idleFarmland = idleFarmlandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("농지를 찾을 수 없습니다."));

        if (!idleFarmland.getOwner().getEmail().equals(userDetails.getUsername())) {
            throw new RuntimeException("자신의 농지만 삭제할 수 있습니다.");
        }

        idleFarmlandRepository.delete(idleFarmland);
        log.info("Deleted idle farmland with ID: {}", id);
    }

    /**
     * 농지 상태 변경
     */
    @Transactional
    public IdleFarmlandResponse updateFarmlandStatus(Long id, IdleFarmland.FarmlandStatus status, UserDetails userDetails) {
        log.info("Updating farmland status for ID: {} to {}", id, status);

        IdleFarmland idleFarmland = idleFarmlandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("농지를 찾을 수 없습니다."));

        if (!idleFarmland.getOwner().getEmail().equals(userDetails.getUsername())) {
            throw new RuntimeException("자신의 농지만 수정할 수 있습니다.");
        }

        idleFarmland.setStatus(status);
        IdleFarmland updatedFarmland = idleFarmlandRepository.save(idleFarmland);

        return IdleFarmlandResponse.from(updatedFarmland);
    }

    /**
     * 내 농지 목록 조회
     */
    public List<IdleFarmlandResponse> getMyIdleFarmlands(UserDetails userDetails) {
        log.info("Fetching my idle farmlands for user: {}", userDetails.getUsername());

        User owner = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return idleFarmlandRepository.findByOwner(owner)
                .stream()
                .map(IdleFarmlandResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 농지 검색 (필터링)
     */
    public List<IdleFarmlandResponse> searchIdleFarmlands(
            String address, 
            IdleFarmland.UsageType usageType,
            IdleFarmland.SoilType soilType,
            BigDecimal minArea,
            BigDecimal maxArea,
            Integer minRent,
            Integer maxRent) {
        
        log.info("Searching idle farmlands with filters");

        return idleFarmlandRepository.findAvailableFarmlandsWithFilters(
                        address, usageType, soilType, minArea, maxArea, minRent, maxRent)
                .stream()
                .map(IdleFarmlandResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 지도 마커용 데이터 조회
     */
    public List<IdleFarmlandMarkerResponse> getIdleFarmlandMarkers() {
        log.info("Fetching idle farmland markers");

        return idleFarmlandRepository.findAllAvailableForMap()
                .stream()
                .map(IdleFarmlandMarkerResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 지역별 지도 마커용 데이터 조회
     */
    public List<IdleFarmlandMarkerResponse> getIdleFarmlandMarkersByRegion(String region) {
        log.info("Fetching idle farmland markers for region: {}", region);

        return idleFarmlandRepository.findByRegionForMap(region)
                .stream()
                .map(IdleFarmlandMarkerResponse::from)
                .collect(Collectors.toList());
    }
}
