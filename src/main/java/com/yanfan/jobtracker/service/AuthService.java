package com.yanfan.jobtracker.service;

import com.yanfan.jobtracker.dto.AppUserResponse;
import com.yanfan.jobtracker.dto.LoginRequest;
import com.yanfan.jobtracker.dto.LoginResponse;
import com.yanfan.jobtracker.dto.RegisterRequest;
import com.yanfan.jobtracker.exception.DuplicateEmailException;
import com.yanfan.jobtracker.exception.InvalidCredentialsException;
import com.yanfan.jobtracker.model.AppUser;
import com.yanfan.jobtracker.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

// service layer for user registration and authentication logic
@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static final String INVALID_CREDENTIALS_MESSAGE =
            "Invalid email or password";


    // constructor injection
    @Autowired
    public AuthService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // register a new user with normalized email and BCrypt password hash
    @Transactional
    public AppUserResponse register(RegisterRequest request) {

        String normalizedEmail = normalizeEmail(request.getEmail());

        // reject the registration if the email already exists
        if (appUserRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException("Email is already registered");
        }

        // encode raw password
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        AppUser theUser = new AppUser(normalizedEmail, encodedPassword);

        try {
            AppUser savedUser = appUserRepository.saveAndFlush(theUser);

            return mapToResponse(savedUser);

        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEmailException("Email is already registered");
        }

    }

    // authenticates the user and returns a signed JWT access token
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        String normalizedEmail = normalizeEmail(request.getEmail());

        // find the account
        AppUser user = appUserRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE));

        // compare submitted password with stored password
        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            throw new InvalidCredentialsException(INVALID_CREDENTIALS_MESSAGE);
        }

        // generate JWT
        String accessToken = jwtService.generateToken(user);

        return new LoginResponse(
                accessToken,
                "Bearer",
                jwtService.getExpirationSeconds()
        );

    }

    // convert the entity into a safe response without the password hash
    private AppUserResponse mapToResponse(AppUser user) {

        return new AppUserResponse(
                user.getId(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }

    private String normalizeEmail(String email) {

        return email.trim().toLowerCase(Locale.ROOT);
    }


}
