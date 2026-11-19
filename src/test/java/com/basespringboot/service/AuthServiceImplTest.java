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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity("voyageur", "voyageur@sncf.fr", "hashed_pwd", "Voyageur Demo", "ROLE_USER");
        userEntity.setId(1L);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest("voyageur", "voyageur@sncf.fr", "secret123", "Voyageur Demo");

        when(userRepository.existsByUsername("voyageur")).thenReturn(false);
        when(userRepository.existsByEmail("voyageur@sncf.fr")).thenReturn(false);
        when(passwordService.hashPassword("secret123")).thenReturn("hashed_pwd");
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(jwtService.generateToken(eq(1L), eq("voyageur"), eq("ROLE_USER"), eq("Voyageur Demo"))).thenReturn("mock.jwt.token");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("mock.jwt.token");
        assertThat(response.username()).isEqualTo("voyageur");
        assertThat(response.role()).isEqualTo("ROLE_USER");
    }

    @Test
    void register_DuplicateUsername_ThrowsException() {
        RegisterRequest request = new RegisterRequest("voyageur", "voyageur@sncf.fr", "secret123", "Voyageur Demo");
        when(userRepository.existsByUsername("voyageur")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("voyageur");
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("voyageur", "secret123");

        when(userRepository.findByUsername("voyageur")).thenReturn(Optional.of(userEntity));
        when(passwordService.verifyPassword("secret123", "hashed_pwd")).thenReturn(true);
        when(jwtService.generateToken(eq(1L), eq("voyageur"), eq("ROLE_USER"), eq("Voyageur Demo"))).thenReturn("valid.jwt.token");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("valid.jwt.token");
        assertThat(response.username()).isEqualTo("voyageur");
    }

    @Test
    void login_InvalidUser_ThrowsUnauthorized() {
        LoginRequest request = new LoginRequest("unknown", "secret123");
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void login_WrongPassword_ThrowsUnauthorized() {
        LoginRequest request = new LoginRequest("voyageur", "wrong");
        when(userRepository.findByUsername("voyageur")).thenReturn(Optional.of(userEntity));
        when(passwordService.verifyPassword("wrong", "hashed_pwd")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void getCurrentUserProfile_Success() {
        UserContext.setCurrentUser(userEntity);

        UserProfileResponse profile = authService.getCurrentUserProfile();

        assertThat(profile).isNotNull();
        assertThat(profile.username()).isEqualTo("voyageur");
        assertThat(profile.email()).isEqualTo("voyageur@sncf.fr");
    }

    @Test
    void getCurrentUserProfile_NotLoggedIn_ThrowsUnauthorized() {
        assertThatThrownBy(() -> authService.getCurrentUserProfile())
                .isInstanceOf(UnauthorizedException.class);
    }
}
