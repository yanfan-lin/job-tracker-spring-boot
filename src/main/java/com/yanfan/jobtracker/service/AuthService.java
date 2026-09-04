package com.yanfan.jobtracker.service;

import com.yanfan.jobtracker.dto.AppUserResponse;
import com.yanfan.jobtracker.dto.LoginRequest;
import com.yanfan.jobtracker.dto.LoginResponse;
import com.yanfan.jobtracker.dto.RegisterRequest;
import com.yanfan.jobtracker.exception.DuplicateEmailException;
import com.yanfan.jobtracker.exception.InvalidCredentialsException;
import com.yanfan.jobtracker.model.AppUser;
import com.yanfan.jobtracker.repository.AppUserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

// Handle user registration and login logic
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

    // Register a new user with a normalized email and BCrypt password hash
    @Transactional
    public AppUserResponse register(RegisterRequest request) {

        String normalizedEmail = normalizeEmail(request.getEmail());

        // Hash the raw password before storing it
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        AppUser theUser = new AppUser(normalizedEmail, encodedPassword);

        try {
            AppUser savedUser = appUserRepository.saveAndFlush(theUser);

            return mapToResponse(savedUser);
        }
        catch (DataIntegrityViolationException e) {
            throw new DuplicateEmailException("Email is already registered");
        }
    }

    // Authenticate the user and return a signed JWT access token
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        String normalizedEmail = normalizeEmail(request.getEmail());

        // Find the account by its normalized email
        AppUser user = appUserRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE));

        // Compare the submitted password with the stored BCrypt hash
        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        // Generate a signed JWT after the credentials are verified
        String accessToken = jwtService.generateToken(user);

        return new LoginResponse(
                accessToken,
                "Bearer",
                jwtService.getExpirationSeconds()
        );
    }

    // Map the entity to a response without exposing the password hash
    private AppUserResponse mapToResponse(AppUser user) {

        return new AppUserResponse(
                user.getId(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }

    // Trim and lowercase emails so registration and login use the same format
    private String normalizeEmail(String email) {

        return email.trim().toLowerCase(Locale.ROOT);
    }

}
