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

    }

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

        verify(repository, never())
                .save(any(JobApplication.class));
    }

    @Test
    void findById_shouldThrowExceptionWhenNotFound() {

        when(repository.findByIdAndUserId(999L, 42L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(42L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Job application not found with id: 999");

    }

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
                        invocation.getArgument(0));

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

    }

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
                .delete(application);
    }

    @Test
    void findAll_shouldThrowExceptionWhenSortByIsInvalid() {

        assertThatThrownBy(() -> service.findAll(
                42L,
                null,
                null,
                "random",
                "asc",
                10,
                0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("sort_by must be one of: id, company, title, status, date_applied, created_at, updated_at");

    }

}
