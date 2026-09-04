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

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);

        verify(appUserRepository)
                .saveAndFlush(userCaptor.capture());

        AppUser savedUser = userCaptor.getValue();

        assertThat(savedUser.getPasswordHash())
                .isEqualTo("hashed-password");
    }

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
    }

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
    }

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

        verifyNoInteractions(passwordEncoder, jwtService);
    }

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

        verifyNoInteractions(jwtService);
    }

}
