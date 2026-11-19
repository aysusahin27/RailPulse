package com.basespringboot.dto.auth;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String role,
        LocalDateTime createdAt
) {
}
