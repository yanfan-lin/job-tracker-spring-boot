package com.yanfan.jobtracker.repository;

import com.yanfan.jobtracker.model.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// JobApplication database access layer
// JpaRepository provides built-in CRUD methods
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    // JPQL query for the list endpoint
    // if status or search is null, status filter is ignored
    // iif search is null, the key word search is ignored
    @Query("""
            SELECT j FROM JobApplication j
            WHERE (:status IS NULL OR j.status = :status)
            AND (
                LOWER(j.company) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(j.title) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            """)
    Page<JobApplication> findWithFilters(
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable
    );

}
