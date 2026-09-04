package com.yanfan.jobtracker.service;

import com.yanfan.jobtracker.dto.JobApplicationPatchRequest;
import com.yanfan.jobtracker.dto.JobApplicationRequest;
import com.yanfan.jobtracker.dto.JobApplicationResponse;
import com.yanfan.jobtracker.model.AppUser;
import com.yanfan.jobtracker.model.JobApplication;
import com.yanfan.jobtracker.repository.AppUserRepository;
import com.yanfan.jobtracker.repository.JobApplicationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Manages each user's job applications.
@Service
public class JobApplicationService {

    private final JobApplicationRepository repository;

    private final AppUserRepository appUserRepository;

    public JobApplicationService(JobApplicationRepository repository,
                                 AppUserRepository appUserRepository)
    {
        this.repository = repository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public JobApplicationResponse create(
            Long userId,
            JobApplicationRequest request)
    {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with id: " + userId));

        JobApplication application = new JobApplication(
                user,
                request.company(),
                request.title(),
                request.status(),
                request.dateApplied(),
                request.notes());

        return mapToResponse(repository.save(application));
    }

    public List<JobApplicationResponse> findAll(
            Long userId,
            String status,
            String search,
            String sortBy,
            String order,
            int limit,
            int page)
    {
        return repository.findWithFiltersForUser(
                        userId,
                        StringUtils.hasText(status) ? status : null,
                        StringUtils.hasText(search) ? search : "",
                        buildPageable(sortBy, order, limit, page)
                )
                .getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public JobApplicationResponse findById(
            Long userId,
            Long applicationId) {

        return mapToResponse(findOwnedApplication(userId, applicationId));
    }

    @Transactional
    public JobApplicationResponse patch(
            Long userId,
            Long applicationId,
            JobApplicationPatchRequest request)
    {
        JobApplication application = findOwnedApplication(userId, applicationId);

        if (request.company() != null) {
            application.setCompany(request.company());
        }
        if (request.title() != null) {
            application.setTitle(request.title());
        }
        if (request.status() != null) {
            application.setStatus(request.status());
        }
        if (request.dateApplied() != null) {
            application.setDateApplied(request.dateApplied());
        }
        if (request.notes() != null) {
            application.setNotes(request.notes());
        }

        return mapToResponse(repository.saveAndFlush(application));
    }

    @Transactional
    public void delete(
            Long userId,
            Long applicationId) {

        repository.delete(findOwnedApplication(userId, applicationId));
    }

    private JobApplication findOwnedApplication(Long userId, Long applicationId) {

        return repository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Job application not found with id: " + applicationId));
    }

    private JobApplicationResponse mapToResponse(JobApplication application) {

        return new JobApplicationResponse(
                application.getId(),
                application.getCompany(),
                application.getTitle(),
                application.getStatus(),
                application.getDateApplied(),
                application.getNotes(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }

    private Pageable buildPageable(String sortBy, String order, int limit, int page) {

        return PageRequest.of(
                page,
                limit,
                Sort.by(
                        StringUtils.hasText(order)
                                ? Sort.Direction.fromString(order)
                                : Sort.Direction.DESC,
                        mapSortField(sortBy)
                )
        );
    }

    private String mapSortField(String sortBy) {

        if (sortBy == null || sortBy.isBlank()) {
            return "dateApplied";
        }

        return switch (sortBy) {
            case "id", "company", "title", "status" -> sortBy;
            case "date_applied" -> "dateApplied";
            case "created_at" -> "createdAt";
            case "updated_at" -> "updatedAt";
            default -> throw new IllegalArgumentException(
                    "sort_by must be one of: id, company, title, status, date_applied, created_at, updated_at");
        };
    }

}
