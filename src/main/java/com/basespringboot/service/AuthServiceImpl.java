package com.basespringboot.service;

import com.basespringboot.dto.auth.AuthResponse;
import com.basespringboot.dto.auth.LoginRequest;
import com.basespringboot.dto.auth.RegisterRequest;
import com.basespringboot.dto.auth.UserProfileResponse;
import com.basespringboot.entity.UserEntity;
import com.basespringboot.exception.DuplicateResourceException;
import com.basespringboot.exception.UnauthorizedException;
import com.basespringboot.repository.UserRepository;
import com.basespringboot.security.JwtService;
import com.basespringboot.security.PasswordService;
import com.basespringboot.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String trimmedUsername = request.getUsername().trim();
        String trimmedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByUsername(trimmedUsername)) {
            throw new DuplicateResourceException("Username '" + trimmedUsername + "' is already taken");
        }
        if (userRepository.existsByEmail(trimmedEmail)) {
            throw new DuplicateResourceException("Email '" + trimmedEmail + "' is already registered");
        }

        String passwordHash = passwordService.hashPassword(request.getPassword());
        UserEntity user = new UserEntity(
                trimmedUsername,
                trimmedEmail,
                passwordHash,
                request.getFullName() != null ? request.getFullName().trim() : trimmedUsername,
                "ROLE_USER"
        );

        UserEntity saved = userRepository.save(user);
        String token = jwtService.generateToken(saved.getId(), saved.getUsername(), saved.getRole(), saved.getFullName());

        return new AuthResponse(
                token,
                saved.getId(),
                saved.getUsername(),
                saved.getEmail(),
                saved.getFullName(),
                saved.getRole()
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String trimmedUsername = request.getUsername().trim();
        UserEntity user = userRepository.findByUsername(trimmedUsername)
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!passwordService.verifyPassword(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole(), user.getFullName());

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole()
        );
    }

    @Override
    public UserProfileResponse getCurrentUserProfile() {
        UserEntity current = UserContext.getCurrentUser();
        if (current == null) {
            throw new UnauthorizedException("No authenticated user found");
        }
        return new UserProfileResponse(
                current.getId(),
                current.getUsername(),
                current.getEmail(),
                current.getFullName(),
                current.getRole(),
                current.getCreatedAt()
        );
    }
}
