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

    // 일손 모집 공고 생성
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
    public List<JobPostingResponse> getJobPostingsByBounds(
            double minLat, double maxLat, double minLng, double maxLng, Pageable pageable) {

        List<JobPosting> jobPostings = jobPostingRepository.findByBounds(
                minLat, maxLat, minLng, maxLng, pageable);

        return jobPostings.stream()
                .map(JobPostingResponse::from)
                .collect(Collectors.toList());
    }
}
