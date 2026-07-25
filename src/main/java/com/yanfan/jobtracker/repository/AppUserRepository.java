package com.yanfan.jobtracker.repository;

import com.yanfan.jobtracker.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// database access layer for AppUser
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    // finds a registered user by email address if exists
    Optional<AppUser> findByEmail(String email);

    // verifies whether an email address is already registered
    boolean existsByEmail(String email);

}
