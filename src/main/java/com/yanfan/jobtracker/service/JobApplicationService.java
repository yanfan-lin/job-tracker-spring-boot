package com.yanfan.jobtracker.service;

import com.yanfan.jobtracker.dto.JobApplicationPatchRequest;
import com.yanfan.jobtracker.dto.JobApplicationRequest;
import com.yanfan.jobtracker.dto.JobApplicationResponse;
import com.yanfan.jobtracker.exception.ResourceNotFoundException;
import com.yanfan.jobtracker.model.AppUser;
import com.yanfan.jobtracker.model.JobApplication;
import com.yanfan.jobtracker.repository.AppUserRepository;
import com.yanfan.jobtracker.repository.JobApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// service layer for job application business logic
@Service
public class JobApplicationService {

    private final JobApplicationRepository repository;

    private final AppUserRepository appUserRepository;

    // constructor injection
    @Autowired
    public JobApplicationService(JobApplicationRepository repository, AppUserRepository appUserRepository) {
        this.repository = repository;
        this.appUserRepository = appUserRepository;
    }

    // create a new job application record
    @Transactional
    public JobApplicationResponse create(
            Long userId,
            JobApplicationRequest request
    ) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + userId
                ));

        JobApplication application = new JobApplication(
                request.getCompany(),
                request.getTitle(),
                request.getStatus(),
                request.getDateApplied(),
                request.getNotes()
        );

        application.assignToUser(user);

        JobApplication savedApplication = repository.save(application);

        return mapToResponse(savedApplication);


    }

    // return only the authenticated user's job applications
    public List<JobApplicationResponse> findAll(
            Long userId,
            String status,
            String search,
            String sortBy,
            String order,
            int limit,
            int page
    ) {
        Pageable pageable =
                buildPageable(sortBy, order, limit, page);

        Page<JobApplication> thePage =
                repository.findWithFiltersForUser(
                        userId,
                        normalizeFilter(status),
                        normalizeSearch(search),
                        pageable
                );

        return thePage.getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

    }

    // find one application that belongs to the authenticated user
    public JobApplicationResponse findById(
            Long userId,
            Long applicationId
    ) {
        JobApplication application = repository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Job application not found with id: " + applicationId
                        )
                );

        return mapToResponse(application);

    }

    // partially update an existing job application
    // all fields are optional
    @Transactional
    public JobApplicationResponse patch(Long id, JobApplicationPatchRequest request) {

        JobApplication theApplication = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Job application not found with id: " + id
                ));

        if (request.getCompany() != null) {
            theApplication.setCompany(request.getCompany());
        }
        if (request.getTitle() != null) {
            theApplication.setTitle(request.getTitle());
        }
        if (request.getStatus() != null) {
            theApplication.setStatus(request.getStatus());
        }
        if (request.getDateApplied() != null) {
            theApplication.setDateApplied(request.getDateApplied());
        }
        if (request.getNotes() != null) {
            theApplication.setNotes(request.getNotes());
        }

        JobApplication updatedApplication = repository.saveAndFlush(theApplication);

        return mapToResponse(updatedApplication);

    }

    // delete an existing job application
    @Transactional
    public void delete(Long id) {

        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Job application not found with id: " + id);
        }

        repository.deleteById(id);
    }

    // convert a database entity into the response DTO returned by the API
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

    // convert empty query parameters into null
    private String normalizeFilter(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }

        return str;
    }

    // convert empty search values into an empty string
    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return "";
        }

        return search;
    }

    // pagination and sorting rules
    private Pageable buildPageable(String sortBy, String order, int limit, int page) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be greater than 0");
        }

        if (page < 0) {
            throw new IllegalArgumentException("page cannot be negative");
        }

        String sortField = mapSortField(sortBy);
        Sort.Direction direction = mapSortDirection(order);

        return PageRequest.of(page, limit, Sort.by(direction, sortField));

    }

    // convert API query parameter names into entity field names
    private String mapSortField(String sortBy) {

        // sort by dateApplied by default
        if (sortBy == null || sortBy.isBlank()) {
            return "dateApplied";
        }

        return switch (sortBy) {
            case "id" -> "id";
            case "company" -> "company";
            case "title" -> "title";
            case "status" -> "status";
            case "date_applied" -> "dateApplied";
            case "created_at" -> "createdAt";
            case "updated_at" -> "updatedAt";
            default -> throw new IllegalArgumentException(
                    "sort_by must be one of: id, company, title, status, date_applied, created_at, updated_at"
            );
        };

    }

    // convert asc/desc query parameter into Spring's Sort.Direction
    private Sort.Direction mapSortDirection(String order) {
        if (order == null || order.isBlank() || order.equalsIgnoreCase("desc")) {
            return Sort.Direction.DESC;
        }

        if (order.equalsIgnoreCase("asc")) {
            return Sort.Direction.ASC;
        }

        throw new IllegalArgumentException("order must be either asc or desc");
    }


}
