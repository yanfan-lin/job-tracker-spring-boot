package com.yanfan.jobtracker.repository;

import com.yanfan.jobtracker.model.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

// JobApplication database access layer
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    // finds the authenticated user's applications with optional filters
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

    // finds one application that belongs to the specified user
    @Query("""
            SELECT j FROM JobApplication j
            WHERE j.id = :id
            AND j.user.id = :userId
            """)
    Optional<JobApplication> findByIdAndUserId(
            @Param("id") Long id,
            @Param("userId") Long userId
    );


}
