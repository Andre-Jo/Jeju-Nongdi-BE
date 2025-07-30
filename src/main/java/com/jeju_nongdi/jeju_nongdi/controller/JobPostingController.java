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
@Tag(name = "일손 모집 공고", description = "농장 일손 모집 공고 관련 API")
public class JobPostingController {

    private final JobPostingService jobPostingService;

    @PostMapping
    @Operation(
            summary = "일손 모집 공고 등록",
            description = "새로운 일손 모집 공고를 등록합니다. 인증된 사용자만 등록할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "공고 등록 성공",
                    content = @Content(schema = @Schema(implementation = JobPostingResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 데이터",
                    content = @Content(schema = @Schema(implementation = com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.class))
            )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<JobPostingResponse> createJobPosting(
            @Valid @RequestBody @Parameter(description = "일손 모집 공고 등록 요청") JobPostingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        JobPostingResponse response = jobPostingService.createJobPosting(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "일손 모집 공고 목록 조회",
            description = "활성 상태의 일손 모집 공고 목록을 페이징하여 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "공고 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    public ResponseEntity<Page<JobPostingResponse>> getJobPostings(
            @Parameter(
                description = "페이징 정보", 
                example = "{\"page\": 0, \"size\": 20, \"sort\": \"createdAt,desc\"}"
            ) @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<JobPostingResponse> jobPostings = jobPostingService.getActiveJobPostings(pageable);
        return ResponseEntity.ok(jobPostings);
    }

    @GetMapping("/filter")
    @Operation(
            summary = "일손 모집 공고 필터링 조회", 
            description = "작물별, 작업별, 주소별로 필터링하여 공고를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "필터링 조회 성공",
                    content = @Content(schema = @Schema(implementation = JobPostingResponse.class))
            )
    })
    public ResponseEntity<List<JobPostingResponse>> getFilteredJobPostings(
            @Parameter(description = "작물 종류") @RequestParam(required = false) JobPosting.CropType cropType,
            @Parameter(description = "작업 종류") @RequestParam(required = false) JobPosting.WorkType workType,
            @Parameter(description = "주소 (일부 포함 검색)") @RequestParam(required = false) String address) {

        List<JobPostingResponse> jobPostings = jobPostingService.getFilteredJobPostings(
                cropType, workType, address);
        return ResponseEntity.ok(jobPostings);
    }

    @GetMapping("/search")
    @Operation(
            summary = "확장된 일손 모집 공고 검색", 
            description = "작물별, 작업별, 지역별, 시즌별로 종합 검색합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "검색 성공",
                    content = @Content(schema = @Schema(implementation = JobPostingResponse.class))
            )
    })
    public ResponseEntity<List<JobPostingResponse>> searchJobPostings(
            @Parameter(description = "작물 종류") 
            @RequestParam(required = false) JobPosting.CropType cropType,
            
            @Parameter(description = "작업 종류") 
            @RequestParam(required = false) JobPosting.WorkType workType,
            
            @Parameter(description = "지역 (예: 제주시, 서귀포시)") 
            @RequestParam(required = false) String region,
            
            @Parameter(description = "세부 지역 (예: 애월읍, 한림읍, 성산읍)") 
            @RequestParam(required = false) String district,
            
            @Parameter(description = "월별 검색 (1-12)") 
            @RequestParam(required = false) Integer month,
            
            @Parameter(description = "계절별 검색 (spring, summer, autumn/fall, winter)") 
            @RequestParam(required = false) String season) {

        List<JobPostingResponse> jobPostings = jobPostingService.getJobPostingsWithAdvancedFilters(
                cropType, workType, region, district, month, season);
        return ResponseEntity.ok(jobPostings);
    }

    @GetMapping("/markers")
    @Operation(
            summary = "지도 마커용 데이터 조회", 
            description = "지도에 표시할 간소화된 공고 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "마커 데이터 조회 성공",
                    content = @Content(schema = @Schema(implementation = JobPostingMarkerResponse.class))
            )
    })
    public ResponseEntity<List<JobPostingMarkerResponse>> getJobPostingMarkers() {
        List<JobPostingMarkerResponse> markers = jobPostingService.getJobPostingMarkers();
        return ResponseEntity.ok(markers);
    }

    @GetMapping("/markers/region")
    @Operation(
            summary = "지역별 지도 마커 데이터 조회", 
            description = "특정 지역의 지도 마커용 공고 데이터를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "지역별 마커 데이터 조회 성공",
                    content = @Content(schema = @Schema(implementation = JobPostingMarkerResponse.class))
            )
    })
    public ResponseEntity<List<JobPostingMarkerResponse>> getJobPostingMarkersByRegion(
            @Parameter(description = "지역 (예: 제주시, 서귀포시)") 
            @RequestParam(required = false) String region,
            
            @Parameter(description = "세부 지역 (예: 애월읍, 한림읍, 성산읍)") 
            @RequestParam(required = false) String district) {

        List<JobPostingMarkerResponse> markers = jobPostingService.getJobPostingMarkersByRegion(region, district);
        return ResponseEntity.ok(markers);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "일손 모집 공고 상세 조회",
            description = "특정 일손 모집 공고의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "공고 상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = JobPostingResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "공고를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.class))
            )
    })
    public ResponseEntity<JobPostingResponse> getJobPosting(
            @Parameter(description = "공고 ID") @PathVariable Long id) {
        JobPostingResponse jobPosting = jobPostingService.getJobPosting(id);
        return ResponseEntity.ok(jobPosting);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "일손 모집 공고 수정",
            description = "기존 일손 모집 공고를 수정합니다. 작성자만 수정할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "공고 수정 성공",
                    content = @Content(schema = @Schema(implementation = JobPostingResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 데이터",
                    content = @Content(schema = @Schema(implementation = com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "수정 권한 없음",
                    content = @Content(schema = @Schema(implementation = com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "공고를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.class))
            )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<JobPostingResponse> updateJobPosting(
            @Parameter(description = "공고 ID") @PathVariable Long id,
            @Valid @RequestBody @Parameter(description = "일손 모집 공고 수정 요청") JobPostingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        JobPostingResponse response = jobPostingService.updateJobPosting(id, request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "일손 모집 공고 삭제",
            description = "일손 모집 공고를 삭제합니다. 작성자만 삭제할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "공고 삭제 성공",
                    content = @Content(schema = @Schema(implementation = com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "삭제 권한 없음",
                    content = @Content(schema = @Schema(implementation = com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "공고를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.class))
            )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<com.jeju_nongdi.jeju_nongdi.dto.ApiResponse<Void>> deleteJobPosting(
            @Parameter(description = "공고 ID") @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        jobPostingService.deleteJobPosting(id, userDetails.getUsername());
        return ResponseEntity.ok(com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.success("공고가 성공적으로 삭제되었습니다.", (Void) null));
    }

    @PatchMapping("/{id}/status")
    @Operation(
            summary = "일손 모집 공고 상태 변경",
            description = "일손 모집 공고의 상태를 변경합니다. 작성자만 변경할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "공고 상태 변경 성공",
                    content = @Content(schema = @Schema(implementation = JobPostingResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 상태값",
                    content = @Content(schema = @Schema(implementation = com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "변경 권한 없음",
                    content = @Content(schema = @Schema(implementation = com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "공고를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.class))
            )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<JobPostingResponse> updateJobPostingStatus(
            @Parameter(description = "공고 ID") @PathVariable Long id,
            @Parameter(description = "변경할 공고 상태") @RequestParam JobPosting.JobStatus status,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        JobPostingResponse response = jobPostingService.updateJobPostingStatus(id, status, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @Operation(
            summary = "내가 작성한 일손 모집 공고 조회",
            description = "현재 로그인한 사용자가 작성한 일손 모집 공고 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "내 공고 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = JobPostingResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.class))
            )
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<JobPostingResponse>> getMyJobPostings(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<JobPostingResponse> jobPostings = jobPostingService.getJobPostingsByUser(userDetails.getUsername());
        return ResponseEntity.ok(jobPostings);
    }

    @GetMapping("/crop-types")
    @Operation(
            summary = "작물 타입 목록 조회",
            description = "일손 모집 공고에서 사용 가능한 작물 타입 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "작물 타입 목록 조회 성공"
            )
    })
    public ResponseEntity<JobPosting.CropType[]> getCropTypes() {
        return ResponseEntity.ok(JobPosting.CropType.values());
    }

    @GetMapping("/work-types")
    @Operation(
            summary = "작업 타입 목록 조회",
            description = "일손 모집 공고에서 사용 가능한 작업 타입 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "작업 타입 목록 조회 성공"
            )
    })
    public ResponseEntity<JobPosting.WorkType[]> getWorkTypes() {
        return ResponseEntity.ok(JobPosting.WorkType.values());
    }

    @GetMapping("/wage-types")
    @Operation(
            summary = "급여 타입 목록 조회",
            description = "일손 모집 공고에서 사용 가능한 급여 타입 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "급여 타입 목록 조회 성공"
            )
    })
    public ResponseEntity<JobPosting.WageType[]> getWageTypes() {
        return ResponseEntity.ok(JobPosting.WageType.values());
    }

    @GetMapping("/job-statuses")
    @Operation(
            summary = "공고 상태 목록 조회",
            description = "일손 모집 공고에서 사용 가능한 공고 상태 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "공고 상태 목록 조회 성공"
            )
    })
    public ResponseEntity<JobPosting.JobStatus[]> getJobStatuses() {
        return ResponseEntity.ok(JobPosting.JobStatus.values());
    }




    @Operation(summary = "지도 영역 내 일손 모집 공고 조회",
            description = "지정된 위도/경도 범위 내의 일손 모집 공고를 조회합니다.\n\n" +
                    "**예시 좌표:**\n" +
                    "- 전체 제주도: minLat=33.25, maxLat=33.50, minLng=126.26, maxLng=126.72\n" +
                    "- 제주시 북부: minLat=33.39, maxLat=33.49, minLng=126.26, maxLng=126.53\n" +
                    "- 서귀포시 남부: minLat=33.25, maxLat=33.33, minLng=126.40, maxLng=126.72")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "영역 내 공고 조회 성공",
                    content = @Content(schema = @Schema(implementation = JobPostingResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 좌표값",
                    content = @Content(schema = @Schema(implementation = com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.class))
            )
    })
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

            @Parameter(
                    description = "페이징 정보",
                    example = "{\"page\": 0, \"size\": 20, \"sort\": \"createdAt,desc\"}"
            )
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        // 좌표 유효성 검증
        if (minLat >= maxLat || minLng >= maxLng) {
            throw new IllegalArgumentException("잘못된 좌표 범위입니다. 최소값은 최대값보다 작아야 합니다.");
        }

        List<JobPostingResponse> jobPostings = jobPostingService.getJobPostingsByBounds(
                minLat, maxLat, minLng, maxLng, pageable);

        return ResponseEntity.ok(jobPostings);
    }

    @Operation(summary = "지도 영역 내 일손 모집 공고 조회 (필터링 포함)",
            description = "지정된 위도/경도 범위 내의 일손 모집 공고를 필터링과 함께 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "필터링된 영역 내 공고 조회 성공",
                    content = @Content(schema = @Schema(implementation = JobPostingResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 좌표값 또는 필터값",
                    content = @Content(schema = @Schema(implementation = com.jeju_nongdi.jeju_nongdi.dto.ApiResponse.class))
            )
    })
    @GetMapping("/bounds/filter")
    public ResponseEntity<List<JobPostingResponse>> getJobPostingsByBoundsWithFilters(
            @Parameter(description = "최소 위도", example = "33.25", required = true)
            @RequestParam double minLat,

            @Parameter(description = "최대 위도", example = "33.50", required = true)
            @RequestParam double maxLat,

            @Parameter(description = "최소 경도", example = "126.26", required = true)
            @RequestParam double minLng,

            @Parameter(description = "최대 경도", example = "126.72", required = true)
            @RequestParam double maxLng,

            @Parameter(description = "작물 종류") 
            @RequestParam(required = false) JobPosting.CropType cropType,

            @Parameter(description = "작업 종류") 
            @RequestParam(required = false) JobPosting.WorkType workType,

            @Parameter(description = "주소 (일부 포함 검색)") 
            @RequestParam(required = false) String address) {

        // 좌표 유효성 검증
        if (minLat >= maxLat || minLng >= maxLng) {
            throw new IllegalArgumentException("잘못된 좌표 범위입니다. 최소값은 최대값보다 작아야 합니다.");
        }

        List<JobPostingResponse> jobPostings = jobPostingService.getJobPostingsByBoundsWithFilters(
                minLat, maxLat, minLng, maxLng, cropType, workType, address);

        return ResponseEntity.ok(jobPostings);
    }

    @Operation(summary = "지도 영역 내 마커용 데이터 조회",
            description = "지정된 위도/경도 범위 내의 지도 마커용 간소화된 공고 데이터를 조회합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "영역 내 마커 데이터 조회 성공",
                    content = @Content(schema = @Schema(implementation = JobPostingMarkerResponse.class))
            )
    })
    @GetMapping("/bounds/markers")
    public ResponseEntity<List<JobPostingMarkerResponse>> getJobPostingMarkersByBounds(
            @Parameter(description = "최소 위도", example = "33.25", required = true)
            @RequestParam double minLat,

            @Parameter(description = "최대 위도", example = "33.50", required = true)
            @RequestParam double maxLat,

            @Parameter(description = "최소 경도", example = "126.26", required = true)
            @RequestParam double minLng,

            @Parameter(description = "최대 경도", example = "126.72", required = true)
            @RequestParam double maxLng) {

        // 좌표 유효성 검증
        if (minLat >= maxLat || minLng >= maxLng) {
            throw new IllegalArgumentException("잘못된 좌표 범위입니다. 최소값은 최대값보다 작아야 합니다.");
        }

        List<JobPostingMarkerResponse> markers = jobPostingService.getJobPostingMarkersByBounds(
                minLat, maxLat, minLng, maxLng);

        return ResponseEntity.ok(markers);
    }

}
