package com.yanfan.jobtracker.repository;

import com.yanfan.jobtracker.model.AppUser;
import com.yanfan.jobtracker.model.JobApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

// tests the JPA mappings and ownership-scoped repository queries
@DataJpaTest(showSql = false)
class JobApplicationRepositoryTest {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    // a user must not retrieve another user's application by its database ID
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

        Optional<JobApplication> ownerResult =
                jobApplicationRepository.findByIdAndUserId(
                        application.getId(),
                        owner.getId()
                );

        Optional<JobApplication> otherUserResult =
                jobApplicationRepository.findByIdAndUserId(
                        application.getId(),
                        otherUser.getId()
                );

        assertThat(ownerResult).isPresent();
        assertThat(otherUserResult).isEmpty();

    }

    // queries must apply both filtering and user ownership
    @Test
    void findWithFiltersForUser_shouldReturnOnlyMatchingOwnedApplications() {

        AppUser firstUser = saveUser("first@example.com");

        AppUser secondUser = saveUser("second@example.com");

        // matches the requested status and search for the firstUser
        saveApplication(
                firstUser,
                "Amazon",
                "Software Developer",
                "applied"
        );

        // belongs to the firstUser but does not match the status filter
        saveApplication(
                firstUser,
                "Shopify",
                "Data Engineer",
                "rejected"
        );

        // matches the filters but belongs to the secondUser
        saveApplication(
                secondUser,
                "Microsoft",
                "Java Developer",
                "applied"
        );

        Pageable pageable = PageRequest.of(0, 10);

        Page<JobApplication> result =
                jobApplicationRepository.findWithFiltersForUser(
                        firstUser.getId(),
                        "applied",
                        "developer",
                        pageable
                );

        assertThat(result.getContent())
                .hasSize(1)
                .extracting(JobApplication::getCompany)
                .containsExactly("Amazon");

    }

    // saves a user so the database generates a real user ID
    private AppUser saveUser(String email) {
        AppUser appUser = new AppUser(
                email,
                "hashed-password"
        );

        return appUserRepository.saveAndFlush(appUser);

    }

    // saves an application with a foreign key relationship
    private JobApplication saveApplication(
            AppUser owner,
            String company,
            String title,
            String status
    ) {
        JobApplication application = new JobApplication(
                company,
                title,
                status,
                LocalDate.of(2026, 7, 6),
                null
        );

        application.assignToUser(owner);

        return jobApplicationRepository.saveAndFlush(application);

    }


}
