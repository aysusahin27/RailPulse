package com.basespringboot.service;

import com.basespringboot.dto.auth.AuthResponse;
import com.basespringboot.dto.auth.LoginRequest;
import com.basespringboot.dto.auth.RegisterRequest;
import com.basespringboot.dto.auth.UserProfileResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserProfileResponse getCurrentUserProfile();
}
