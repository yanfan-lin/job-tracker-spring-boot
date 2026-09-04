package com.yanfan.jobtracker.controller;

import com.yanfan.jobtracker.dto.AppUserResponse;
import com.yanfan.jobtracker.dto.LoginRequest;
import com.yanfan.jobtracker.dto.LoginResponse;
import com.yanfan.jobtracker.dto.RegisterRequest;
import com.yanfan.jobtracker.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// Handles registration and login requests.
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AppUserResponse register(@Valid @RequestBody RegisterRequest request) {

        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {

        return authService.login(request);
    }

}
