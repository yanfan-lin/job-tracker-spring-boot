package com.yanfan.jobtracker.repository;

import com.yanfan.jobtracker.model.AppUser;
import com.yanfan.jobtracker.model.JobApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

// Test JPA mappings and ownership-scoped repository queries
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

        // Match the requested status and search for the first user
        saveApplication(
                firstUser,
                "Amazon",
                "Software Developer",
                "applied"
        );

        // Belong to the first user but fail the status filter
        saveApplication(
                firstUser,
                "Shopify",
                "Data Engineer",
                "rejected"
        );

        // Match the filters but belong to the second user
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

    // Save a user so the database generates a real user ID
    private AppUser saveUser(String email) {

        AppUser appUser = new AppUser(
                email,
                "hashed-password");

        return appUserRepository.saveAndFlush(appUser);
    }

    // Save an application with a real foreign-key relationship
    private JobApplication saveApplication(
            AppUser owner,
            String company,
            String title,
            String status)
    {
        JobApplication application = new JobApplication(
                owner,
                company,
                title,
                status,
                LocalDate.of(2026, 7, 6),
                null
        );

        return jobApplicationRepository.saveAndFlush(application);
    }

}
