package com.yanfan.jobtracker.repository;

import com.yanfan.jobtracker.model.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// Provide database access for job applications
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    // Return the user's applications with optional status and text filters
    @Query("""
            SELECT j FROM JobApplication j 
            WHERE j.user.id = :userId
            AND (:status IS NULL OR j.status = :status)
            AND (
                 LOWER(j.company) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(j.title) LIKE LOWER(CONCAT('%', :search, '%'))
                )
            """)
    Page<JobApplication> findWithFiltersForUser(
            @Param("userId") Long userId,
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable

    );

    Optional<JobApplication> findByIdAndUserId(Long id, Long userId);

}
