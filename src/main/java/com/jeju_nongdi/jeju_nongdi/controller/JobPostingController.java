package com.jeju_nongdi.jeju_nongdi.controller;

import com.jeju_nongdi.jeju_nongdi.dto.JobPostingMarkerResponse;
import com.jeju_nongdi.jeju_nongdi.dto.JobPostingRequest;
import com.jeju_nongdi.jeju_nongdi.dto.JobPostingResponse;
import com.jeju_nongdi.jeju_nongdi.entity.JobPosting;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.service.JobPostingService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-postings")
@RequiredArgsConstructor
@Tag(name = "일손 모집 공고", description = "일손 모집 공고 관련 API")
public class JobPostingController {

    private final JobPostingService jobPostingService;

    @PostMapping
    @Operation(summary = "일손 모집 공고 생성", description = "새로운 일손 모집 공고를 생성합니다. 로그인된 사용자만 이용 가능합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "공고 생성 성공",
                    content = @Content(schema = @Schema(implementation = JobPostingResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content)
    })
    public ResponseEntity<JobPostingResponse> createJobPosting(
            @Parameter(description = "일손 모집 공고 생성 요청 데이터", required = true)
            @Valid @RequestBody JobPostingRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        JobPostingResponse response = jobPostingService.createJobPosting(request, user.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "일손 모집 공고 목록 조회", description = "활성화된 일손 모집 공고 목록을 페이징으로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공고 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<JobPostingResponse>> getJobPostings(
            @Parameter(description = "페이징 정보 (기본값: size=20)")
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<JobPostingResponse> jobPostings = jobPostingService.getActiveJobPostings(pageable);
        return ResponseEntity.ok(jobPostings);
    }

    @GetMapping("/filter")
    @Operation(summary = "일손 모집 공고 필터링 조회", description = "작물 타입, 작업 타입, 주소로 일손 모집 공고를 필터링하여 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "필터링된 공고 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = JobPostingResponse.class)))
    })
    public ResponseEntity<List<JobPostingResponse>> getFilteredJobPostings(
            @Parameter(description = "작물 타입 필터")
            @RequestParam(required = false) JobPosting.CropType cropType,
            @Parameter(description = "작업 타입 필터")
            @RequestParam(required = false) JobPosting.WorkType workType,
            @Parameter(description = "주소 필터")
            @RequestParam(required = false) String address) {
        
        List<JobPostingResponse> jobPostings = jobPostingService.getFilteredJobPostings(
                cropType, workType, address);
        return ResponseEntity.ok(jobPostings);
    }

    @GetMapping("/markers")
    @Operation(summary = "지도 마커용 일손 모집 공고 데이터 조회", description = "지도에 표시할 마커 정보를 포함한 일손 모집 공고 데이터를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "마커 데이터 조회 성공",
                    content = @Content(schema = @Schema(implementation = JobPostingMarkerResponse.class)))
    })
    public ResponseEntity<List<JobPostingMarkerResponse>> getJobPostingMarkers() {
        List<JobPostingMarkerResponse> markers = jobPostingService.getJobPostingMarkers();
        return ResponseEntity.ok(markers);
    }

    @GetMapping("/{id}")
    @Operation(summary = "일손 모집 공고 상세 조회", description = "특정 일손 모집 공고의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공고 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = JobPostingResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 공고",
                    content = @Content)
    })
    public ResponseEntity<JobPostingResponse> getJobPosting(
            @Parameter(description = "조회할 공고 ID", required = true, example = "1")
            @PathVariable Long id) {
        JobPostingResponse jobPosting = jobPostingService.getJobPosting(id);
        return ResponseEntity.ok(jobPosting);
    }

    @PutMapping("/{id}")
    @Operation(summary = "일손 모집 공고 수정", description = "기존 일손 모집 공고를 수정합니다. 공고 작성자만 수정할 수 있습니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공고 수정 성공",
                    content = @Content(schema = @Schema(implementation = JobPostingResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 공고",
                    content = @Content)
    })
    public ResponseEntity<JobPostingResponse> updateJobPosting(
            @Parameter(description = "수정할 공고 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "일손 모집 공고 수정 요청 데이터", required = true)
            @Valid @RequestBody JobPostingRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        JobPostingResponse response = jobPostingService.updateJobPosting(id, request, user.getEmail());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "일손 모집 공고 삭제", description = "기존 일손 모집 공고를 삭제합니다. 공고 작성자만 삭제할 수 있습니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공고 삭제 성공",
                    content = @Content(schema = @Schema(implementation = com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 공고",
                    content = @Content)
    })
    public ResponseEntity<com.jeju_nongdi.jeju_nongdi.dto.ApiResponse<Void>> deleteJobPosting(
            @Parameter(description = "삭제할 공고 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        jobPostingService.deleteJobPosting(id, user.getEmail());
        return ResponseEntity.ok(com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.success("공고가 성공적으로 삭제되었습니다.", null));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "일손 모집 공고 상태 변경", description = "기존 일손 모집 공고의 상태를 변경합니다. 공고 작성자만 변경할 수 있습니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공고 상태 변경 성공",
                    content = @Content(schema = @Schema(implementation = JobPostingResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "상태 변경 권한 없음",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 공고",
                    content = @Content)
    })
    public ResponseEntity<JobPostingResponse> updateJobPostingStatus(
            @Parameter(description = "상태를 변경할 공고 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "변경할 공고 상태", required = true)
            @RequestParam JobPosting.JobStatus status,
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        JobPostingResponse response = jobPostingService.updateJobPostingStatus(id, status, user.getEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @Operation(summary = "내가 작성한 일손 모집 공고 조회", description = "현재 로그인한 사용자가 작성한 모든 일손 모집 공고를 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내 공고 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = JobPostingResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                    content = @Content)
    })
    public ResponseEntity<List<JobPostingResponse>> getMyJobPostings(
            @Parameter(hidden = true) @AuthenticationPrincipal User user) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        List<JobPostingResponse> jobPostings = jobPostingService.getJobPostingsByUser(user.getEmail());
        return ResponseEntity.ok(jobPostings);
    }

    @GetMapping("/crop-types")
    @Operation(summary = "작물 타입 목록 조회", description = "사용 가능한 모든 작물 타입 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "작물 타입 목록 조회 성공",
                    content = @Content)
    })
    public ResponseEntity<JobPosting.CropType[]> getCropTypes() {
        return ResponseEntity.ok(JobPosting.CropType.values());
    }

    @GetMapping("/work-types")
    @Operation(summary = "작업 타입 목록 조회", description = "사용 가능한 모든 작업 타입 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "작업 타입 목록 조회 성공",
                    content = @Content)
    })
    public ResponseEntity<JobPosting.WorkType[]> getWorkTypes() {
        return ResponseEntity.ok(JobPosting.WorkType.values());
    }

    @GetMapping("/wage-types")
    @Operation(summary = "급여 타입 목록 조회", description = "사용 가능한 모든 급여 타입 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "급여 타입 목록 조회 성공",
                    content = @Content)
    })
    public ResponseEntity<JobPosting.WageType[]> getWageTypes() {
        return ResponseEntity.ok(JobPosting.WageType.values());
    }

    @GetMapping("/job-statuses")
    @Operation(summary = "공고 상태 목록 조회", description = "사용 가능한 모든 공고 상태 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "공고 상태 목록 조회 성공",
                    content = @Content)
    })
    public ResponseEntity<JobPosting.JobStatus[]> getJobStatuses() {
        return ResponseEntity.ok(JobPosting.JobStatus.values());
    }
}
