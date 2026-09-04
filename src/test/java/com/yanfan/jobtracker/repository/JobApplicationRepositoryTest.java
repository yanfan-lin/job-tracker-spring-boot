package com.yanfan.jobtracker.repository;

import com.yanfan.jobtracker.model.AppUser;
import com.yanfan.jobtracker.model.JobApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

// Verifies ownership and filtering in database queries.
@DataJpaTest(showSql = false)
class JobApplicationRepositoryTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;


    @Test
    void findByIdAndUserId_shouldNotReturnAnotherUsersApplication() {

        AppUser owner = saveUser("owner@example.com");

        AppUser otherUser = saveUser("other@example.com");

        JobApplication application = saveApplication(
                owner,
                "Amazon",
                "Software Developer",
                "applied"
        );

        assertThat(jobApplicationRepository.findByIdAndUserId(
                application.getId(),
                owner.getId()))
                .isPresent();

        assertThat(jobApplicationRepository.findByIdAndUserId(
                application.getId(),
                otherUser.getId()))
                .isEmpty();
    }

    @Test
    void findWithFiltersForUser_shouldReturnOnlyMatchingOwnedApplications() {

        AppUser firstUser = saveUser("first@example.com");

        AppUser secondUser = saveUser("second@example.com");

        // Matches both filters for the first user.
        saveApplication(
                firstUser,
                "Amazon",
                "Software Developer",
                "applied"
        );

        // Matches the user but not the requested status.
        saveApplication(
                firstUser,
                "Shopify",
                "Data Engineer",
                "rejected"
        );

        // Matches the filters but belongs to another user.
        saveApplication(
                secondUser,
                "Microsoft",
                "Java Developer",
                "applied"
        );

        assertThat(jobApplicationRepository.findWithFiltersForUser(
                        firstUser.getId(),
                        "applied",
                        "developer",
                        PageRequest.of(0, 10))
                .getContent())
                .hasSize(1)
                .extracting(JobApplication::getCompany)
                .containsExactly("Amazon");

    }

    private AppUser saveUser(String email) {

        return appUserRepository.saveAndFlush(new AppUser(
                email,
                "hashed-password"));
    }

    private JobApplication saveApplication(
            AppUser owner,
            String company,
            String title,
            String status) {

        return jobApplicationRepository.saveAndFlush(new JobApplication(
                owner,
                company,
                title,
                status,
                LocalDate.of(2026, 7, 6),
                null
        ));
    }

}
