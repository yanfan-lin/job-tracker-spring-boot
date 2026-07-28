package com.yanfan.jobtracker.repository;

import com.yanfan.jobtracker.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Provide database access for registered users
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    // Find a registered user by normalized email
    Optional<AppUser> findByEmail(String email);

    // Check whether an email address is already registered
    boolean existsByEmail(String email);

}
