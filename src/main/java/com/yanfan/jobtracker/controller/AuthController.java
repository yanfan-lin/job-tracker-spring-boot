package com.yanfan.jobtracker.controller;

import com.yanfan.jobtracker.dto.AppUserResponse;
import com.yanfan.jobtracker.dto.RegisterRequest;
import com.yanfan.jobtracker.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// handles authentication-related requests
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    // constructor injection
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /auth/register
    // registers a new user account
    @PostMapping("/register")
    public ResponseEntity<AppUserResponse> register(@Valid @RequestBody RegisterRequest request) {

        AppUserResponse theUser = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(theUser);
    }


}
