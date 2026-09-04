package com.yanfan.jobtracker.service;

import com.yanfan.jobtracker.dto.AppUserResponse;
import com.yanfan.jobtracker.dto.LoginRequest;
import com.yanfan.jobtracker.dto.LoginResponse;
import com.yanfan.jobtracker.dto.RegisterRequest;
import com.yanfan.jobtracker.model.AppUser;
import com.yanfan.jobtracker.repository.AppUserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

// Handles user registration and login.
@Service
public class AuthService {

    private final AppUserRepository appUserRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    private static final String INVALID_CREDENTIALS_MESSAGE =
            "Invalid email or password";

    public AuthService(AppUserRepository appUserRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService)
    {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AppUserResponse register(RegisterRequest request) {

        AppUser user = new AppUser(
                normalizeEmail(request.email()),
                passwordEncoder.encode(request.password())
        );

        try {
            return mapToResponse(appUserRepository.saveAndFlush(user));
        }
        catch (DataIntegrityViolationException e) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered");
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        AppUser user = appUserRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                INVALID_CREDENTIALS_MESSAGE));

        if (!passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()))
        {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    INVALID_CREDENTIALS_MESSAGE);
        }

        return new LoginResponse(
                jwtService.generateToken(user),
                "Bearer",
                jwtService.getExpirationSeconds());
    }

    private AppUserResponse mapToResponse(AppUser user) {

        return new AppUserResponse(
                user.getId(),
                user.getEmail(),
                user.getCreatedAt());
    }

    private String normalizeEmail(String email) {

        return email.trim().toLowerCase(Locale.ROOT);
    }

}
