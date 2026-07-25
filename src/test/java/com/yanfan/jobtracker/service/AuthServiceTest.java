package com.yanfan.jobtracker.service;

import com.yanfan.jobtracker.dto.AppUserResponse;
import com.yanfan.jobtracker.dto.RegisterRequest;
import com.yanfan.jobtracker.exception.DuplicateEmailException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


// unit tests for AuthService
// repository is mocked, so a real database is not needed
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;


    // test successful user registration
    @Test
    void register_shouldNormalizeEmailHashPasswordAndReturnResponse() {
        RegisterRequest request = new RegisterRequest(
                " Person@Example.COM ",
                "password123"
        );

        when(appUserRepository.existsByEmail("person@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");

        when(appUserRepository.saveAndFlush(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AppUserResponse response = authService.register(request);

        assertThat(response.getEmail()).isEqualTo("person@example.com");

        // capture the AppUser sent to the repository
        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);

        verify(appUserRepository).saveAndFlush(userCaptor.capture());

        AppUser savedUser = userCaptor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("person@example.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedUser.getPasswordHash()).isNotEqualTo("password123");

        verify(appUserRepository).existsByEmail("person@example.com");
        verify(passwordEncoder).encode("password123");

    }

    // test registration when the email already exists
    @Test
    void register_shouldThrowExceptionWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                " Person@Example.COM ",
                "password123"
        );

        when(appUserRepository.existsByEmail("person@example.com"))
                .thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("Email is already registered");

        verify(appUserRepository).existsByEmail("person@example.com");

        // password hashing and saving will not happen after a duplicate is found
        verify(passwordEncoder, never()).encode(any());
        verify(appUserRepository, never()).saveAndFlush(any(AppUser.class));


    }

    // test when the database rejects a duplicate email during save
    @Test
    void register_shouldConvertDatabaseConflictToDuplicateEmailException() {
        RegisterRequest request = new RegisterRequest(
                "Person@Example.COM",
                "password123"
        );

        when(appUserRepository.existsByEmail("person@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed-password");

        when(appUserRepository.saveAndFlush(any(AppUser.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "Unique email constraint violated"
                ));

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("Email is already registered");

        verify(appUserRepository).existsByEmail("person@example.com");
        verify(passwordEncoder).encode("password123");
        verify(appUserRepository).saveAndFlush(any(AppUser.class));

    }


}
