package com.jeju_nongdi.jeju_nongdi.controller;

import com.jeju_nongdi.jeju_nongdi.dto.ApiResponse;
import com.jeju_nongdi.jeju_nongdi.dto.JobPostingMarkerResponse;
import com.jeju_nongdi.jeju_nongdi.dto.JobPostingRequest;
import com.jeju_nongdi.jeju_nongdi.dto.JobPostingResponse;
import com.jeju_nongdi.jeju_nongdi.entity.JobPosting;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.service.JobPostingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-postings")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService jobPostingService;

    /**
     * 일손 모집 공고 생성
     */
    @PostMapping
    public ResponseEntity<JobPostingResponse> createJobPosting(
            @Valid @RequestBody JobPostingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        JobPostingResponse response = jobPostingService.createJobPosting(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 일손 모집 공고 목록 조회 (페이징)
     */
    @GetMapping
    public ResponseEntity<Page<JobPostingResponse>> getJobPostings(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<JobPostingResponse> jobPostings = jobPostingService.getActiveJobPostings(pageable);
        return ResponseEntity.ok(jobPostings);
    }

    /**
     * 일손 모집 공고 필터링 조회
     */
    @GetMapping("/filter")
    public ResponseEntity<List<JobPostingResponse>> getFilteredJobPostings(
            @RequestParam(required = false) JobPosting.CropType cropType,
            @RequestParam(required = false) JobPosting.WorkType workType,
            @RequestParam(required = false) String address) {

        List<JobPostingResponse> jobPostings = jobPostingService.getFilteredJobPostings(
                cropType, workType, address);
        return ResponseEntity.ok(jobPostings);
    }

    /**
     * 지도 마커용 일손 모집 공고 데이터 조회
     */
    @GetMapping("/markers")
    public ResponseEntity<List<JobPostingMarkerResponse>> getJobPostingMarkers() {
        List<JobPostingMarkerResponse> markers = jobPostingService.getJobPostingMarkers();
        return ResponseEntity.ok(markers);
    }

    /**
     * 일손 모집 공고 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<JobPostingResponse> getJobPosting(@PathVariable Long id) {
        JobPostingResponse jobPosting = jobPostingService.getJobPosting(id);
        return ResponseEntity.ok(jobPosting);
    }

    /**
     * 일손 모집 공고 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<JobPostingResponse> updateJobPosting(
            @PathVariable Long id,
            @Valid @RequestBody JobPostingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        JobPostingResponse response = jobPostingService.updateJobPosting(id, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * 일손 모집 공고 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJobPosting(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        jobPostingService.deleteJobPosting(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("공고가 성공적으로 삭제되었습니다.", null));
    }

    /**
     * 일손 모집 공고 상태 변경
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<JobPostingResponse> updateJobPostingStatus(
            @PathVariable Long id,
            @RequestParam JobPosting.JobStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        JobPostingResponse response = jobPostingService.updateJobPostingStatus(id, status, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * 내가 작성한 일손 모집 공고 조회
     */
    @GetMapping("/my")
    public ResponseEntity<List<JobPostingResponse>> getMyJobPostings(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<JobPostingResponse> jobPostings = jobPostingService.getJobPostingsByUser(userDetails.getUsername());
        return ResponseEntity.ok(jobPostings);
    }

    /**
     * 작물 타입 목록 조회
     */
    @GetMapping("/crop-types")
    public ResponseEntity<JobPosting.CropType[]> getCropTypes() {
        return ResponseEntity.ok(JobPosting.CropType.values());
    }

    /**
     * 작업 타입 목록 조회
     */
    @GetMapping("/work-types")
    public ResponseEntity<JobPosting.WorkType[]> getWorkTypes() {
        return ResponseEntity.ok(JobPosting.WorkType.values());
    }

    /**
     * 급여 타입 목록 조회
     */
    @GetMapping("/wage-types")
    public ResponseEntity<JobPosting.WageType[]> getWageTypes() {
        return ResponseEntity.ok(JobPosting.WageType.values());
    }

    /**
     * 공고 상태 목록 조회
     */
    @GetMapping("/job-statuses")
    public ResponseEntity<JobPosting.JobStatus[]> getJobStatuses() {
        return ResponseEntity.ok(JobPosting.JobStatus.values());
    }

    @Operation(summary = "지도 영역 내 일손 모집 공고 조회",
            description = "지정된 위도/경도 범위 내의 일손 모집 공고를 조회합니다.\n\n" +
                    "**예시 좌표:**\n" +
                    "- 전체 제주도: minLat=33.25, maxLat=33.50, minLng=126.26, maxLng=126.72\n" +
                    "- 제주시 북부: minLat=33.39, maxLat=33.49, minLng=126.26, maxLng=126.53\n" +
                    "- 서귀포시 남부: minLat=33.25, maxLat=33.33, minLng=126.40, maxLng=126.72")
    @GetMapping("/bounds")
    public ResponseEntity<List<JobPostingResponse>> getJobPostingsByBounds(
            @Parameter(description = "최소 위도 (제주도 전체 조회시: 33.25)",
                    example = "33.25", required = true)
            @RequestParam double minLat,

            @Parameter(description = "최대 위도 (제주도 전체 조회시: 33.50)",
                    example = "33.50", required = true)
            @RequestParam double maxLat,

            @Parameter(description = "최소 경도 (제주도 전체 조회시: 126.26)",
                    example = "126.26", required = true)
            @RequestParam double minLng,

            @Parameter(description = "최대 경도 (제주도 전체 조회시: 126.72)",
                    example = "126.72", required = true)
            @RequestParam double maxLng,

            @Parameter(description = "페이징 정보 (기본: 20개씩, 생성일 내림차순)")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        List<JobPostingResponse> jobPostings = jobPostingService.getJobPostingsByBounds(
                minLat, maxLat, minLng, maxLng, pageable);

        return ResponseEntity.ok(jobPostings);
    }

}
