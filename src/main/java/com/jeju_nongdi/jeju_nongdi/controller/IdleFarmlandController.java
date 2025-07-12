package com.jeju_nongdi.jeju_nongdi.controller;

import com.jeju_nongdi.jeju_nongdi.dto.*;
import com.jeju_nongdi.jeju_nongdi.entity.IdleFarmland;
import com.jeju_nongdi.jeju_nongdi.service.IdleFarmlandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/idle-farmlands")
@RequiredArgsConstructor
@Tag(name = "유휴 농지", description = "유휴 농지 관련 API")
public class IdleFarmlandController {

    private final IdleFarmlandService idleFarmlandService;

    @PostMapping
    @Operation(summary = "유휴 농지 등록", description = "새로운 유휴 농지를 등록합니다. 인증된 사용자만 등록할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "유휴 농지 등록 성공",
                    content = @Content(schema = @Schema(implementation = IdleFarmlandResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<IdleFarmlandResponse> createIdleFarmland(
            @Valid @RequestBody @Parameter(description = "유휴 농지 등록 요청") IdleFarmlandRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Creating idle farmland: {}", request.getTitle());
        IdleFarmlandResponse response = idleFarmlandService.createIdleFarmland(request, userDetails);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "유휴 농지 목록 조회", description = "페이징된 유휴 농지 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유휴 농지 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = IdleFarmlandResponse.class)))
    })
    public ResponseEntity<Page<IdleFarmlandResponse>> getIdleFarmlands(
            @Parameter(description = "페이징 정보") @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Fetching idle farmlands with pagination");
        Page<IdleFarmlandResponse> response = idleFarmlandService.getIdleFarmlands(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "유휴 농지 상세 조회", description = "특정 유휴 농지의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유휴 농지 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = IdleFarmlandResponse.class))),
            @ApiResponse(responseCode = "404", description = "유휴 농지를 찾을 수 없음",
                    content = @Content)
    })
    public ResponseEntity<IdleFarmlandResponse> getIdleFarmland(
            @Parameter(description = "유휴 농지 ID") @PathVariable Long id) {
        log.info("Fetching idle farmland with ID: {}", id);
        IdleFarmlandResponse response = idleFarmlandService.getIdleFarmland(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "유휴 농지 수정", description = "기존 유휴 농지 정보를 수정합니다. 작성자만 수정할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유휴 농지 수정 성공",
                    content = @Content(schema = @Schema(implementation = IdleFarmlandResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "유휴 농지를 찾을 수 없음",
                    content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<IdleFarmlandResponse> updateIdleFarmland(
            @Parameter(description = "유휴 농지 ID") @PathVariable Long id,
            @Valid @RequestBody @Parameter(description = "유휴 농지 수정 요청") IdleFarmlandRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Updating idle farmland with ID: {}", id);
        IdleFarmlandResponse response = idleFarmlandService.updateIdleFarmland(id, request, userDetails);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "유휴 농지 삭제", description = "유휴 농지를 삭제합니다. 작성자만 삭제할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "유휴 농지 삭제 성공",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "유휴 농지를 찾을 수 없음",
                    content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<com.jeju_nongdi.jeju_nongdi.dto.ApiResponse<Void>> deleteIdleFarmland(
            @Parameter(description = "유휴 농지 ID") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Deleting idle farmland with ID: {}", id);
        idleFarmlandService.deleteIdleFarmland(id, userDetails);
        
        com.jeju_nongdi.jeju_nongdi.dto.ApiResponse<Void> response = 
                new com.jeju_nongdi.jeju_nongdi.dto.ApiResponse<>(true, "농지가 성공적으로 삭제되었습니다.", null);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "농지 상태 변경", description = "농지의 상태를 변경합니다. 작성자만 변경할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "농지 상태 변경 성공",
                    content = @Content(schema = @Schema(implementation = IdleFarmlandResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 상태값",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "변경 권한 없음",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "유휴 농지를 찾을 수 없음",
                    content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<IdleFarmlandResponse> updateFarmlandStatus(
            @Parameter(description = "유휴 농지 ID") @PathVariable Long id,
            @Parameter(description = "변경할 농지 상태") @RequestParam IdleFarmland.FarmlandStatus status,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Updating farmland status for ID: {} to {}", id, status);
        IdleFarmlandResponse response = idleFarmlandService.updateFarmlandStatus(id, status, userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @Operation(summary = "내 농지 목록 조회", description = "현재 사용자가 등록한 농지 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내 농지 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = IdleFarmlandResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<IdleFarmlandResponse>> getMyIdleFarmlands(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Fetching my idle farmlands");
        List<IdleFarmlandResponse> response = idleFarmlandService.getMyIdleFarmlands(userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "농지 검색", description = "다양한 조건으로 농지를 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "농지 검색 성공",
                    content = @Content(schema = @Schema(implementation = IdleFarmlandResponse.class)))
    })
    public ResponseEntity<List<IdleFarmlandResponse>> searchIdleFarmlands(
            @Parameter(description = "주소") @RequestParam(required = false) String address,
            @Parameter(description = "이용 유형") @RequestParam(required = false) IdleFarmland.UsageType usageType,
            @Parameter(description = "토양 유형") @RequestParam(required = false) IdleFarmland.SoilType soilType,
            @Parameter(description = "최소 면적") @RequestParam(required = false) BigDecimal minArea,
            @Parameter(description = "최대 면적") @RequestParam(required = false) BigDecimal maxArea,
            @Parameter(description = "최소 임대료") @RequestParam(required = false) Integer minRent,
            @Parameter(description = "최대 임대료") @RequestParam(required = false) Integer maxRent) {
        
        log.info("Searching idle farmlands with filters");
        List<IdleFarmlandResponse> response = idleFarmlandService.searchIdleFarmlands(
                address, usageType, soilType, minArea, maxArea, minRent, maxRent);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/markers")
    @Operation(summary = "지도 마커용 데이터 조회", description = "지도에 표시할 농지 마커 데이터를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "지도 마커 데이터 조회 성공",
                    content = @Content(schema = @Schema(implementation = IdleFarmlandMarkerResponse.class)))
    })
    public ResponseEntity<List<IdleFarmlandMarkerResponse>> getIdleFarmlandMarkers() {
        log.info("Fetching idle farmland markers");
        List<IdleFarmlandMarkerResponse> response = idleFarmlandService.getIdleFarmlandMarkers();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/markers/region")
    @Operation(summary = "지역별 지도 마커용 데이터 조회", description = "특정 지역의 농지 마커 데이터를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "지역별 지도 마커 데이터 조회 성공",
                    content = @Content(schema = @Schema(implementation = IdleFarmlandMarkerResponse.class)))
    })
    public ResponseEntity<List<IdleFarmlandMarkerResponse>> getIdleFarmlandMarkersByRegion(
            @Parameter(description = "지역명") @RequestParam String region) {
        
        log.info("Fetching idle farmland markers for region: {}", region);
        List<IdleFarmlandMarkerResponse> response = idleFarmlandService.getIdleFarmlandMarkersByRegion(region);
        return ResponseEntity.ok(response);
    }

    // Enum 타입 목록 조회 API들
    @GetMapping("/usage-types")
    @Operation(summary = "이용 유형 목록 조회", description = "농지 이용 유형 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "이용 유형 목록 조회 성공")
    })
    public ResponseEntity<List<IdleFarmland.UsageType>> getUsageTypes() {
        return ResponseEntity.ok(Arrays.asList(IdleFarmland.UsageType.values()));
    }

    @GetMapping("/soil-types")
    @Operation(summary = "토양 유형 목록 조회", description = "토양 유형 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토양 유형 목록 조회 성공")
    })
    public ResponseEntity<List<IdleFarmland.SoilType>> getSoilTypes() {
        return ResponseEntity.ok(Arrays.asList(IdleFarmland.SoilType.values()));
    }

    @GetMapping("/farmland-statuses")
    @Operation(summary = "농지 상태 목록 조회", description = "농지 상태 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "농지 상태 목록 조회 성공")
    })
    public ResponseEntity<List<IdleFarmland.FarmlandStatus>> getFarmlandStatuses() {
        return ResponseEntity.ok(Arrays.asList(IdleFarmland.FarmlandStatus.values()));
    }
}
