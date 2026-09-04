package com.yanfan.jobtracker.service;

import com.yanfan.jobtracker.dto.JobApplicationPatchRequest;
import com.yanfan.jobtracker.dto.JobApplicationRequest;
import com.yanfan.jobtracker.dto.JobApplicationResponse;
import com.yanfan.jobtracker.exception.ResourceNotFoundException;
import com.yanfan.jobtracker.model.AppUser;
import com.yanfan.jobtracker.model.JobApplication;
import com.yanfan.jobtracker.repository.AppUserRepository;
import com.yanfan.jobtracker.repository.JobApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


// Test JobApplicationService with mocked repositories and no real database
@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository repository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private JobApplicationService service;

    // Verify creation assigns the authenticated user as the owner
    @Test
    void create_shouldSaveApplicationAndReturnResponse() {

        JobApplicationRequest request = new JobApplicationRequest(
                "Amazon",
                "Backend Developer",
                "applied",
                LocalDate.of(2026, 7, 6),
                "Applied through LinkedIn"
        );

        AppUser user = new AppUser(
                "person@example.com",
                "hashed-password"
        );

        when(appUserRepository.findById(42L))
                .thenReturn(Optional.of(user));

        // Return the saved entity from the mocked repository
        when(repository.save(any(JobApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        JobApplicationResponse response = service.create(42L, request);

        assertThat(response.getCompany())
                .isEqualTo("Amazon");
        assertThat(response.getTitle())
                .isEqualTo("Backend Developer");
        assertThat(response.getStatus())
                .isEqualTo("applied");
        assertThat(response.getDateApplied())
                .isEqualTo(LocalDate.of(2026, 7, 6));
        assertThat(response.getNotes())
                .isEqualTo("Applied through LinkedIn");

        ArgumentCaptor<JobApplication> applicationCaptor = ArgumentCaptor.forClass(JobApplication.class);

        verify(repository)
                .save(applicationCaptor.capture());

        JobApplication savedApplication = applicationCaptor.getValue();

        assertThat(savedApplication.getUser())
                .isSameAs(user);

        verify(appUserRepository)
                .findById(42L);

    }

    // Verify creation fails when the JWT user no longer exists
    @Test
    void create_shouldThrowExceptionWhenUserDoesNotExist() {

        JobApplicationRequest request = new JobApplicationRequest(
                "Amazon",
                "Backend Developer",
                "applied",
                LocalDate.of(2026, 7, 6),
                "Applied through LinkedIn"
        );

        when(appUserRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 999");

        verify(appUserRepository).findById(999L);

        // Do not save an application without a valid owner
        verify(repository, never())
                .save(any(JobApplication.class));
    }

    // Verify a missing or unowned application returns the same not-found error
    @Test
    void findById_shouldThrowExceptionWhenNotFound() {

        when(repository.findByIdAndUserId(999L, 42L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(42L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Job application not found with id: 999");

        verify(repository)
                .findByIdAndUserId(999L, 42L);

    }

    // Verify only the provided application fields are updated
    @Test
    void patch_shouldPatchApplicationAndReturnResponse() {

        JobApplication savedApplication = new JobApplication(
                "Amazon",
                "Backend Developer",
                "applied",
                LocalDate.of(2026, 7, 6),
                "Applied through LinkedIn"
        );

        JobApplicationPatchRequest request = new JobApplicationPatchRequest(
                null,
                null,
                "interview",
                null,
                "Recruiter screen scheduled"
        );

        when(repository.findByIdAndUserId(1L, 42L))
                .thenReturn(Optional.of(savedApplication));

        when(repository.saveAndFlush(any(JobApplication.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        JobApplicationResponse response = service.patch(42L, 1L, request);

        assertThat(response.getCompany())
                .isEqualTo("Amazon");
        assertThat(response.getTitle())
                .isEqualTo("Backend Developer");
        assertThat(response.getStatus())
                .isEqualTo("interview");
        assertThat(response.getDateApplied())
                .isEqualTo(LocalDate.of(2026, 7, 6));
        assertThat(response.getNotes())
                .isEqualTo("Recruiter screen scheduled");

        verify(repository)
                .findByIdAndUserId(1L, 42L);
        verify(repository)
                .saveAndFlush(any(JobApplication.class));
    }

    // Verify deleting an owned application succeeds
    @Test
    void delete_shouldDeleteApplicationWhenFound() {

        JobApplication application = new JobApplication(
                "Amazon",
                "Backend Developer",
                "applied",
                LocalDate.of(2026, 7, 6),
                "Applied through LinkedIn"
        );

        when(repository.findByIdAndUserId(1L, 42L))
                .thenReturn(Optional.of(application));

        service.delete(42L, 1L);

        verify(repository)
                .findByIdAndUserId(1L, 42L);
        verify(repository)
                .delete(application);
    }

    // Verify a non-positive limit is rejected
    @Test
    void findAll_shouldThrowExceptionWhenLimitIsInvalid() {

        assertThatThrownBy(() -> service.findAll(
                42L,
                null,
                null,
                "date_applied",
                "asc",
                0,
                0
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("limit must be greater than 0");

    }

    // Verify a negative page number is rejected
    @Test
    void findAll_shouldThrowExceptionWhenPageIsInvalid() {

        assertThatThrownBy(() -> service.findAll(
                42L,
                null,
                null,
                "date_applied",
                "asc",
                10,
                -1
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("page cannot be negative");

    }

    // Verify an unsupported sort field is rejected
    @Test
    void findAll_shouldThrowExceptionWhenSortByIsInvalid() {

        assertThatThrownBy(() -> service.findAll(
                42L,
                null,
                null,
                "random",
                "asc",
                10,
                0
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sort_by must be one of: id, company, title, status, date_applied, created_at, updated_at");

    }

    // Verify an unsupported sort direction is rejected
    @Test
    void findAll_shouldThrowExceptionWhenOrderIsInvalid() {

        assertThatThrownBy(() -> service.findAll(
                42L,
                null,
                null,
                "date_applied",
                "random",
                10,
                0
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("order must be either asc or desc");
    }

}
