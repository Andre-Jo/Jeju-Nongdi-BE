package com.jeju_nongdi.jeju_nongdi.controller;

import com.jeju_nongdi.jeju_nongdi.dto.ApiResponse;
import com.jeju_nongdi.jeju_nongdi.dto.JobPostingMarkerResponse;
import com.jeju_nongdi.jeju_nongdi.dto.JobPostingRequest;
import com.jeju_nongdi.jeju_nongdi.dto.JobPostingResponse;
import com.jeju_nongdi.jeju_nongdi.entity.JobPosting;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.service.JobPostingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            @PageableDefault(size = 20) Pageable pageable) {

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
}
