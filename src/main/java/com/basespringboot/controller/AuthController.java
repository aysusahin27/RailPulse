package com.basespringboot.controller;

import com.basespringboot.dto.auth.AuthResponse;
import com.basespringboot.dto.auth.LoginRequest;
import com.basespringboot.dto.auth.RegisterRequest;
import com.basespringboot.dto.auth.UserProfileResponse;
import com.basespringboot.security.RequireAuth;
import com.basespringboot.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @RequireAuth
    public UserProfileResponse getCurrentUser() {
        return authService.getCurrentUserProfile();
    }
}
