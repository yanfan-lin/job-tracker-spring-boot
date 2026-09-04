package com.yanfan.jobtracker.service;

import com.yanfan.jobtracker.dto.AppUserResponse;
import com.yanfan.jobtracker.dto.LoginRequest;
import com.yanfan.jobtracker.dto.LoginResponse;
import com.yanfan.jobtracker.dto.RegisterRequest;
import com.yanfan.jobtracker.exception.DuplicateEmailException;
import com.yanfan.jobtracker.exception.InvalidCredentialsException;
import com.yanfan.jobtracker.model.AppUser;
import com.yanfan.jobtracker.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


// Test AuthService with mocked dependencies and no real database
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;


    // Verify successful user registration
    @Test
    void register_shouldNormalizeEmailHashPasswordAndReturnResponse() {

        RegisterRequest request = new RegisterRequest(
                " Person@Example.COM ",
                "password123"
        );

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");

        when(appUserRepository.saveAndFlush(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppUserResponse response = authService.register(request);

        assertThat(response.getEmail())
                .isEqualTo("person@example.com");

        // Capture the AppUser sent to the repository
        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);

        verify(appUserRepository)
                .saveAndFlush(userCaptor.capture());

        AppUser savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail())
                .isEqualTo("person@example.com");
        assertThat(savedUser.getPasswordHash())
                .isEqualTo("hashed-password");
        assertThat(savedUser.getPasswordHash())
                .isNotEqualTo("password123");

        verify(passwordEncoder)
                .encode("password123");

    }

    // Verify database duplicate errors become DuplicateEmailException
    @Test
    void register_shouldConvertDatabaseConflictToDuplicateEmailException() {

        RegisterRequest request = new RegisterRequest(
                "Person@Example.COM",
                "password123"
        );

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");

        when(appUserRepository.saveAndFlush(any(AppUser.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "Unique email constraint violated"
                ));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("Email is already registered");

        verify(passwordEncoder)
                .encode("password123");
        verify(appUserRepository)
                .saveAndFlush(any(AppUser.class));

    }

    // Verify successful login and JWT generation
    @Test
    void login_shouldReturnJwtWhenCredentialsAreValid() {

        LoginRequest request = new LoginRequest(
                " Person@Example.COM ",
                "password123"
        );

        AppUser user = new AppUser(
                "person@example.com",
                "hashed-password"
        );

        when(appUserRepository.findByEmail("person@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password123",
                "hashed-password"))
                .thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("signed-jwt-token");

        when(jwtService.getExpirationSeconds())
                .thenReturn(3600L);

        LoginResponse response = authService.login(request);

        assertThat(response.getAccessToken())
                .isEqualTo("signed-jwt-token");
        assertThat(response.getTokenType())
                .isEqualTo("Bearer");
        assertThat(response.getExpiresIn())
                .isEqualTo(3600L);

        verify(appUserRepository)
                .findByEmail("person@example.com");
        verify(passwordEncoder)
                .matches("password123", "hashed-password");
        verify(jwtService).generateToken(user);
    }

    // Verify login fails when the email does not exist
    @Test
    void login_shouldThrowExceptionWhenEmailDoesNotExist() {

        LoginRequest request = new LoginRequest(
                "blahblah@example.com",
                "password123"
        );

        when(appUserRepository.findByEmail("blahblah@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(appUserRepository)
                .findByEmail("blahblah@example.com");

        // Stop before password comparison or token generation
        verify(passwordEncoder, never())
                .matches(any(), any());

        verify(jwtService, never())
                .generateToken(any(AppUser.class));

    }

    // Verify login fails when the password is incorrect
    @Test
    void login_shouldThrowExceptionWhenPasswordIsIncorrect() {

        LoginRequest request = new LoginRequest(
                "blahblah@example.com",
                "wrong-password"
        );

        AppUser user = new AppUser(
                "blahblah@example.com",
                "hashed-password"
        );

        when(appUserRepository.findByEmail("blahblah@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong-password",
                "hashed-password"))
                .thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verify(appUserRepository)
                .findByEmail("blahblah@example.com");

        verify(passwordEncoder)
                .matches("wrong-password", "hashed-password");

        // Do not generate a JWT when the password is incorrect
        verify(jwtService, never())
                .generateToken(any(AppUser.class));
    }

}
