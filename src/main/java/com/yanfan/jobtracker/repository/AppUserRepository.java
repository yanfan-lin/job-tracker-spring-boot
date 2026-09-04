package com.yanfan.jobtracker.repository;

import com.yanfan.jobtracker.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Stores and retrieves registered users.
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);

}
