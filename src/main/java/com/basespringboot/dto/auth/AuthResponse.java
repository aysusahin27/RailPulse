package com.basespringboot.dto.auth;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String username,
        String email,
        String fullName,
        String role
) {
    public AuthResponse(String token, Long userId, String username, String email, String fullName, String role) {
        this(token, "Bearer", userId, username, email, fullName, role);
    }
}
