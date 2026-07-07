package com.yanfan.jobtracker.service;

import com.yanfan.jobtracker.exception.ResourceNotFoundException;

import java.util.Optional;

import com.yanfan.jobtracker.dto.JobApplicationRequest;
import com.yanfan.jobtracker.dto.JobApplicationResponse;
import com.yanfan.jobtracker.dto.JobApplicationPatchRequest;
import com.yanfan.jobtracker.model.JobApplication;
import com.yanfan.jobtracker.repository.JobApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

// unit tests for JobApplicationService
// repository is mocked, so a real database is not needed
@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository repository;

    @InjectMocks
    private JobApplicationService service;

    // test for create job application
    @Test
    void create_shouldSaveApplicationAndReturnResponse() {
        JobApplicationRequest request = new JobApplicationRequest(
                "Amazon",
                "Backend Developer",
                "applied",
                LocalDate.of(2026, 7, 6),
                "Applied through LinkedIn"
        );

        // return the same entity passed into repository.save()
        when(repository.save(any(JobApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        JobApplicationResponse response = service.create(request);

        assertThat(response.getCompany()).isEqualTo("Amazon");
        assertThat(response.getTitle()).isEqualTo("Backend Developer");
        assertThat(response.getStatus()).isEqualTo("applied");
        assertThat(response.getDateApplied()).isEqualTo(LocalDate.of(2026, 7, 6));
        assertThat(response.getNotes()).isEqualTo("Applied through LinkedIn");

        verify(repository).save(any(JobApplication.class));

    }

    // test for findById() when found
    @Test
    void findById_shouldReturnApplicationWhenFound() {
        JobApplication theApplication = new JobApplication(
                "Amazon",
                "Backend Developer",
                "applied",
                LocalDate.of(2026, 7, 6),
                "Applied through LinkedIn"
        );

        when(repository.findById(1L)).thenReturn(Optional.of(theApplication));

        JobApplicationResponse response = service.findById(1L);

        assertThat(response.getCompany()).isEqualTo("Amazon");
        assertThat(response.getTitle()).isEqualTo("Backend Developer");
        assertThat(response.getStatus()).isEqualTo("applied");
        assertThat(response.getDateApplied()).isEqualTo(LocalDate.of(2026, 7, 6));
        assertThat(response.getNotes()).isEqualTo("Applied through LinkedIn");

        verify(repository).findById(1L);

    }

    // test for findById() when application not found
    @Test
    void findById_shouldThrowExceptionWhenNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Job application not found with id: 999");

        verify(repository).findById(999L);

    }

    // test for patch() when application exists
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

        when(repository.findById(1L)).thenReturn(Optional.of(savedApplication));
        when(repository.saveAndFlush(any(JobApplication.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        JobApplicationResponse response = service.patch(1L, request);

        assertThat(response.getCompany()).isEqualTo("Amazon");
        assertThat(response.getTitle()).isEqualTo("Backend Developer");
        assertThat(response.getStatus()).isEqualTo("interview");
        assertThat(response.getDateApplied()).isEqualTo(LocalDate.of(2026, 7, 6));
        assertThat(response.getNotes()).isEqualTo("Recruiter screen scheduled");

        verify(repository).findById(1L);
        verify(repository).saveAndFlush(any(JobApplication.class));

    }

    // test for patch() when application does not exist
    @Test
    void patch_shouldThrowExceptionWhenNotFound() {
        JobApplicationPatchRequest request = new JobApplicationPatchRequest(
                null,
                null,
                "interview",
                null,
                "This should fail"
        );

        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.patch(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Job application not found with id: 999");

        verify(repository).findById(999L);

    }

    // test for delete() when application exists
    @Test
    void delete_shouldDeleteApplicationWhenFound() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    // test for delete() when application does not exist
    @Test
    void delete_shouldThrowExceptionWhenNotFound() {
        when(repository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Job application not found with id: 999");

        verify(repository).existsById(999L);
        verify(repository, never()).deleteById(999L);
    }

}