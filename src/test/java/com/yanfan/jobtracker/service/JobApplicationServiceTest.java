package com.yanfan.jobtracker.service;

import com.yanfan.jobtracker.dto.JobApplicationRequest;
import com.yanfan.jobtracker.repository.AppUserRepository;
import com.yanfan.jobtracker.repository.JobApplicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository repository;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private JobApplicationService service;

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
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND)
                .hasMessageContaining("User not found with id: 999");

        verifyNoInteractions(repository);
    }

    @Test
    void findById_shouldThrowExceptionWhenNotFound() {

        when(repository.findByIdAndUserId(999L, 42L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(42L, 999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND)
                .hasMessageContaining("Job application not found with id: 999");

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
