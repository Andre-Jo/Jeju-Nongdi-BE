package com.jeju_nongdi.jeju_nongdi.controller;

import com.jeju_nongdi.jeju_nongdi.dto.MentoringRequest;
import com.jeju_nongdi.jeju_nongdi.dto.MentoringResponse;
import com.jeju_nongdi.jeju_nongdi.entity.Mentoring;
import com.jeju_nongdi.jeju_nongdi.service.MentoringService;
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

import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/mentorings")
@RequiredArgsConstructor
@Tag(name = "멘토링", description = "멘토-멘티 매칭 관련 API")
public class MentoringController {

    private final MentoringService mentoringService;

    @PostMapping
    @Operation(summary = "멘토링 글 작성", description = "새로운 멘토링 글을 작성합니다. 인증된 사용자만 작성할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "멘토링 글 작성 성공",
                    content = @Content(schema = @Schema(implementation = MentoringResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<MentoringResponse> createMentoring(
            @Valid @RequestBody @Parameter(description = "멘토링 글 작성 요청") MentoringRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Creating mentoring post: {}", request.getTitle());
        MentoringResponse response = mentoringService.createMentoring(request, userDetails);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "멘토링 글 목록 조회", description = "페이징된 멘토링 글 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "멘토링 글 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = MentoringResponse.class)))
    })
    public ResponseEntity<Page<MentoringResponse>> getMentorings(
            @Parameter(description = "페이징 정보") @PageableDefault(size = 20) Pageable pageable) {
        
        log.info("Fetching mentoring posts with pagination");
        Page<MentoringResponse> response = mentoringService.getMentorings(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "멘토링 글 상세 조회", description = "특정 멘토링 글의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "멘토링 글 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = MentoringResponse.class))),
            @ApiResponse(responseCode = "404", description = "멘토링 글을 찾을 수 없음",
                    content = @Content)
    })
    public ResponseEntity<MentoringResponse> getMentoring(
            @Parameter(description = "멘토링 글 ID") @PathVariable Long id) {
        log.info("Fetching mentoring post with ID: {}", id);
        MentoringResponse response = mentoringService.getMentoring(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "멘토링 글 수정", description = "기존 멘토링 글을 수정합니다. 작성자만 수정할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "멘토링 글 수정 성공",
                    content = @Content(schema = @Schema(implementation = MentoringResponse.class))),
            @ApiResponse(responseCode = "400", description = "입력값 검증 실패",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "멘토링 글을 찾을 수 없음",
                    content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<MentoringResponse> updateMentoring(
            @Parameter(description = "멘토링 글 ID") @PathVariable Long id,
            @Valid @RequestBody @Parameter(description = "멘토링 글 수정 요청") MentoringRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Updating mentoring post with ID: {}", id);
        MentoringResponse response = mentoringService.updateMentoring(id, request, userDetails);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "멘토링 글 삭제", description = "멘토링 글을 삭제합니다. 작성자만 삭제할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "멘토링 글 삭제 성공",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "멘토링 글을 찾을 수 없음",
                    content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<com.jeju_nongdi.jeju_nongdi.dto.ApiResponse<Void>> deleteMentoring(
            @Parameter(description = "멘토링 글 ID") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Deleting mentoring post with ID: {}", id);
        mentoringService.deleteMentoring(id, userDetails);
        
        com.jeju_nongdi.jeju_nongdi.dto.ApiResponse<Void> response = 
                new com.jeju_nongdi.jeju_nongdi.dto.ApiResponse<>(true, "멘토링 글이 성공적으로 삭제되었습니다.", null);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "멘토링 상태 변경", description = "멘토링 글의 상태를 변경합니다. 작성자만 변경할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "멘토링 상태 변경 성공",
                    content = @Content(schema = @Schema(implementation = MentoringResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 상태값",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "변경 권한 없음",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "멘토링 글을 찾을 수 없음",
                    content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<MentoringResponse> updateMentoringStatus(
            @Parameter(description = "멘토링 글 ID") @PathVariable Long id,
            @Parameter(description = "변경할 멘토링 상태") @RequestParam Mentoring.MentoringStatus status,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Updating mentoring status for ID: {} to {}", id, status);
        MentoringResponse response = mentoringService.updateMentoringStatus(id, status, userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @Operation(summary = "내가 작성한 멘토링 글 목록", description = "현재 사용자가 작성한 멘토링 글 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내 멘토링 글 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = MentoringResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content)
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<MentoringResponse>> getMyMentorings(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("Fetching my mentoring posts");
        List<MentoringResponse> response = mentoringService.getMyMentorings(userDetails);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "멘토링 글 검색", description = "다양한 조건으로 멘토링 글을 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "멘토링 글 검색 성공",
                    content = @Content(schema = @Schema(implementation = MentoringResponse.class)))
    })
    public ResponseEntity<List<MentoringResponse>> searchMentorings(
            @Parameter(description = "멘토링 타입") @RequestParam(required = false) Mentoring.MentoringType mentoringType,
            @Parameter(description = "카테고리") @RequestParam(required = false) Mentoring.Category category,
            @Parameter(description = "경험 수준") @RequestParam(required = false) Mentoring.ExperienceLevel experienceLevel,
            @Parameter(description = "지역") @RequestParam(required = false) String location,
            @Parameter(description = "검색 키워드") @RequestParam(required = false) String keyword) {
        
        log.info("Searching mentoring posts with filters");
        List<MentoringResponse> response = mentoringService.searchMentorings(
                mentoringType, category, experienceLevel, location, keyword);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{mentoringType}")
    @Operation(summary = "멘토링 타입별 조회", description = "특정 멘토링 타입의 글들을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "멘토링 타입별 조회 성공",
                    content = @Content(schema = @Schema(implementation = MentoringResponse.class)))
    })
    public ResponseEntity<List<MentoringResponse>> getMentoringsByType(
            @Parameter(description = "멘토링 타입") @PathVariable Mentoring.MentoringType mentoringType) {
        
        log.info("Fetching mentoring posts by type: {}", mentoringType);
        List<MentoringResponse> response = mentoringService.getMentoringsByType(mentoringType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "카테고리별 조회", description = "특정 카테고리의 멘토링 글들을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리별 조회 성공",
                    content = @Content(schema = @Schema(implementation = MentoringResponse.class)))
    })
    public ResponseEntity<List<MentoringResponse>> getMentoringsByCategory(
            @Parameter(description = "멘토링 카테고리") @PathVariable Mentoring.Category category) {
        
        log.info("Fetching mentoring posts by category: {}", category);
        List<MentoringResponse> response = mentoringService.getMentoringsByCategory(category);
        return ResponseEntity.ok(response);
    }

    // Enum 타입 목록 조회 API들
    @GetMapping("/mentoring-types")
    @Operation(summary = "멘토링 타입 목록 조회", description = "멘토링 타입 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "멘토링 타입 목록 조회 성공")
    })
    public ResponseEntity<List<Mentoring.MentoringType>> getMentoringTypes() {
        return ResponseEntity.ok(Arrays.asList(Mentoring.MentoringType.values()));
    }

    @GetMapping("/categories")
    @Operation(summary = "카테고리 목록 조회", description = "멘토링 카테고리 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공")
    })
    public ResponseEntity<List<Mentoring.Category>> getCategories() {
        return ResponseEntity.ok(Arrays.asList(Mentoring.Category.values()));
    }

    @GetMapping("/experience-levels")
    @Operation(summary = "경험 수준 목록 조회", description = "경험 수준 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "경험 수준 목록 조회 성공")
    })
    public ResponseEntity<List<Mentoring.ExperienceLevel>> getExperienceLevels() {
        return ResponseEntity.ok(Arrays.asList(Mentoring.ExperienceLevel.values()));
    }

    @GetMapping("/statuses")
    @Operation(summary = "멘토링 상태 목록 조회", description = "멘토링 상태 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "멘토링 상태 목록 조회 성공")
    })
    public ResponseEntity<List<Mentoring.MentoringStatus>> getMentoringStatuses() {
        return ResponseEntity.ok(Arrays.asList(Mentoring.MentoringStatus.values()));
    }
}
