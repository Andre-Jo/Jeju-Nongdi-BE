package com.jeju_nongdi.jeju_nongdi.service;

import com.jeju_nongdi.jeju_nongdi.dto.JobPostingMarkerResponse;
import com.jeju_nongdi.jeju_nongdi.dto.JobPostingRequest;
import com.jeju_nongdi.jeju_nongdi.dto.JobPostingResponse;
import com.jeju_nongdi.jeju_nongdi.entity.JobPosting;
import com.jeju_nongdi.jeju_nongdi.entity.User;
import com.jeju_nongdi.jeju_nongdi.repository.JobPostingRepository;
import com.jeju_nongdi.jeju_nongdi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;

    /**
     * 일손 모집 공고 생성
     */
    public JobPostingResponse createJobPosting(JobPostingRequest request, String userEmail) {
        User author = getUserByEmail(userEmail);

        JobPosting jobPosting = JobPosting.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .farmName(request.getFarmName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .cropType(request.getCropType())
                .workType(request.getWorkType())
                .wages(request.getWages())
                .wageType(request.getWageType())
                .workStartDate(request.getWorkStartDate())
                .workEndDate(request.getWorkEndDate())
                .recruitmentCount(request.getRecruitmentCount())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .author(author)
                .build();

        JobPosting savedJobPosting = jobPostingRepository.save(jobPosting);
        return JobPostingResponse.from(savedJobPosting);
    }

    /**
     * 일손 모집 공고 상세 조회
     */
    @Transactional(readOnly = true)
    public JobPostingResponse getJobPosting(Long id) {
        JobPosting jobPosting = getJobPostingById(id);
        return JobPostingResponse.from(jobPosting);
    }

    /**
     * 활성 일손 모집 공고 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<JobPostingResponse> getActiveJobPostings(Pageable pageable) {
        Page<JobPosting> jobPostings = jobPostingRepository.findByStatusOrderByCreatedAtDesc(
                JobPosting.JobStatus.ACTIVE, pageable);
        return jobPostings.map(JobPostingResponse::from);
    }

    /**
     * 필터링된 일손 모집 공고 목록 조회
     */
    @Transactional(readOnly = true)
    public List<JobPostingResponse> getFilteredJobPostings(
            JobPosting.CropType cropType,
            JobPosting.WorkType workType,
            String address) {
        
        List<JobPosting> jobPostings = jobPostingRepository.findWithFilters(
                JobPosting.JobStatus.ACTIVE,
                cropType,
                workType,
                address,
                LocalDate.now()
        );
        
        return jobPostings.stream()
                .map(JobPostingResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 확장된 필터링 일손 모집 공고 목록 조회 (지역별, 시즌별 포함)
     */
    @Transactional(readOnly = true)
    public List<JobPostingResponse> getJobPostingsWithAdvancedFilters(
            JobPosting.CropType cropType,
            JobPosting.WorkType workType,
            String region,        // 제주시, 서귀포시
            String district,      // 애월읍, 한림읍 등
            Integer month,        // 월별 검색 (1-12)
            String season) {      // 계절별 검색 (spring, summer, autumn, winter)
        
        // 주소 필터 조합
        String addressFilter = buildAddressFilter(region, district);
        
        // 시즌/월별 필터링을 위한 날짜 범위 설정
        LocalDate startDate = getSeasonStartDate(month, season);
        LocalDate endDate = getSeasonEndDate(month, season);
        
        List<JobPosting> jobPostings;
        
        if (startDate != null && endDate != null) {
            // 시즌/월별 필터링이 있는 경우
            jobPostings = jobPostingRepository.findWithFiltersAndDateRange(
                    JobPosting.JobStatus.ACTIVE,
                    cropType,
                    workType,
                    addressFilter,
                    startDate,
                    endDate
            );
        } else {
            // 기본 필터링
            jobPostings = jobPostingRepository.findWithFilters(
                    JobPosting.JobStatus.ACTIVE,
                    cropType,
                    workType,
                    addressFilter,
                    LocalDate.now()
            );
        }
        
        return jobPostings.stream()
                .map(JobPostingResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 지역별 필터링된 지도 마커용 데이터 조회
     */
    @Transactional(readOnly = true)
    public List<JobPostingMarkerResponse> getJobPostingMarkersByRegion(String region, String district) {
        String addressFilter = buildAddressFilter(region, district);
        
        List<JobPosting> jobPostings;
        if (addressFilter != null) {
            jobPostings = jobPostingRepository.findByAddressContainingAndStatusOrderByCreatedAtDesc(
                    addressFilter, JobPosting.JobStatus.ACTIVE);
        } else {
            jobPostings = jobPostingRepository.findAllForMap(JobPosting.JobStatus.ACTIVE);
        }
        
        return jobPostings.stream()
                .map(JobPostingMarkerResponse::from)
                .collect(Collectors.toList());
    }

    // === Private Helper Methods for Advanced Filtering ===

    private String buildAddressFilter(String region, String district) {
        if (district != null && !district.trim().isEmpty()) {
            return district.trim();
        } else if (region != null && !region.trim().isEmpty()) {
            return region.trim();
        }
        return null;
    }

    private LocalDate getSeasonStartDate(Integer month, String season) {
        if (month != null && month >= 1 && month <= 12) {
            return LocalDate.of(LocalDate.now().getYear(), month, 1);
        }
        
        if (season != null) {
            int currentYear = LocalDate.now().getYear();
            return switch (season.toLowerCase()) {
                case "spring" -> LocalDate.of(currentYear, 3, 1);
                case "summer" -> LocalDate.of(currentYear, 6, 1);
                case "autumn", "fall" -> LocalDate.of(currentYear, 9, 1);
                case "winter" -> LocalDate.of(currentYear, 12, 1);
                default -> null;
            };
        }
        
        return null;
    }

    private LocalDate getSeasonEndDate(Integer month, String season) {
        if (month != null && month >= 1 && month <= 12) {
            return LocalDate.of(LocalDate.now().getYear(), month, 1).plusMonths(1).minusDays(1);
        }
        
        if (season != null) {
            int currentYear = LocalDate.now().getYear();
            return switch (season.toLowerCase()) {
                case "spring" -> LocalDate.of(currentYear, 5, 31);
                case "summer" -> LocalDate.of(currentYear, 8, 31);
                case "autumn", "fall" -> LocalDate.of(currentYear, 11, 30);
                case "winter" -> LocalDate.of(currentYear + 1, 2, 28); // 다음해 2월까지
                default -> null;
            };
        }
        
        return null;
    }

    /**
     * 지도 마커용 데이터 조회
     */
    @Transactional(readOnly = true)
    public List<JobPostingMarkerResponse> getJobPostingMarkers() {
        List<JobPosting> jobPostings = jobPostingRepository.findAllForMap(JobPosting.JobStatus.ACTIVE);
        return jobPostings.stream()
                .map(JobPostingMarkerResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자별 일손 모집 공고 조회
     */
    @Transactional(readOnly = true)
    public List<JobPostingResponse> getJobPostingsByUser(String userEmail) {
        User user = getUserByEmail(userEmail);
        List<JobPosting> jobPostings = jobPostingRepository.findByAuthorOrderByCreatedAtDesc(user);
        return jobPostings.stream()
                .map(JobPostingResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 일손 모집 공고 수정
     */
    public JobPostingResponse updateJobPosting(Long id, JobPostingRequest request, String userEmail) {
        JobPosting jobPosting = getJobPostingById(id);
        User currentUser = getUserByEmail(userEmail);

        // 작성자 권한 확인
        validateAuthor(jobPosting, currentUser);

        // 공고 정보 업데이트
        updateJobPostingFields(jobPosting, request);

        JobPosting updatedJobPosting = jobPostingRepository.save(jobPosting);
        return JobPostingResponse.from(updatedJobPosting);
    }

    /**
     * 일손 모집 공고 삭제
     */
    public void deleteJobPosting(Long id, String userEmail) {
        JobPosting jobPosting = getJobPostingById(id);
        User currentUser = getUserByEmail(userEmail);

        // 작성자 권한 확인
        validateAuthor(jobPosting, currentUser);

        jobPostingRepository.delete(jobPosting);
    }

    /**
     * 일손 모집 공고 상태 변경 (모집완료/취소)
     */
    public JobPostingResponse updateJobPostingStatus(Long id, JobPosting.JobStatus status, String userEmail) {
        JobPosting jobPosting = getJobPostingById(id);
        User currentUser = getUserByEmail(userEmail);

        // 작성자 권한 확인
        validateAuthor(jobPosting, currentUser);

        jobPosting.setStatus(status);
        JobPosting updatedJobPosting = jobPostingRepository.save(jobPosting);
        return JobPostingResponse.from(updatedJobPosting);
    }

    // === Private Helper Methods ===

    private JobPosting getJobPostingById(Long id) {
        return jobPostingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("일손 모집 공고를 찾을 수 없습니다. ID: " + id));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. 이메일: " + email));
    }

    private void validateAuthor(JobPosting jobPosting, User currentUser) {
        if (!jobPosting.getAuthor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("해당 공고의 작성자만 수정/삭제할 수 있습니다.");
        }
    }

    private void updateJobPostingFields(JobPosting jobPosting, JobPostingRequest request) {
        jobPosting.setTitle(request.getTitle());
        jobPosting.setDescription(request.getDescription());
        jobPosting.setFarmName(request.getFarmName());
        jobPosting.setAddress(request.getAddress());
        jobPosting.setLatitude(request.getLatitude());
        jobPosting.setLongitude(request.getLongitude());
        jobPosting.setCropType(request.getCropType());
        jobPosting.setWorkType(request.getWorkType());
        jobPosting.setWages(request.getWages());
        jobPosting.setWageType(request.getWageType());
        jobPosting.setWorkStartDate(request.getWorkStartDate());
        jobPosting.setWorkEndDate(request.getWorkEndDate());
        jobPosting.setRecruitmentCount(request.getRecruitmentCount());
        jobPosting.setContactPhone(request.getContactPhone());
        jobPosting.setContactEmail(request.getContactEmail());
    }
}
