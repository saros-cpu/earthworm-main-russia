package com.earthworm.service;

import com.earthworm.config.JwtUtil;
import com.earthworm.model.User;
import com.earthworm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtUtil jwtUtil;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, jwtUtil, new BCryptPasswordEncoder());
    }

    @Test
    void register_shouldSucceed() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(jwtUtil.generateToken(any(), any(), any(), anyInt())).thenReturn("test-token");

        Map<String, Object> result = authService.register("testuser", "password123", "Test");

        assertEquals("test-token", result.get("token"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldFail_whenUsernameTooShort() {
        Map<String, Object> result = authService.register("a", "password123", null);
        assertEquals("Username must be at least 2 characters", result.get("error"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldFail_whenPasswordTooShort() {
        Map<String, Object> result = authService.register("testuser", "abc", null);
        assertEquals("Password must be at least 6 characters", result.get("error"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldFail_whenPasswordTooLong() {
        Map<String, Object> result = authService.register("testuser", "a1" + "x".repeat(127), null);
        assertEquals("Password must be no more than 128 characters", result.get("error"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldFail_whenUsernameExists() {
        when(userRepository.existsByUsername("existing")).thenReturn(true);

        Map<String, Object> result = authService.register("existing", "password123", null);
        assertEquals("Username already exists", result.get("error"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldSucceed() {
        User user = new User();
        user.setId("user-1");
        user.setUsername("testuser");
        user.setPasswordHash(new BCryptPasswordEncoder().encode("password123"));
        user.setRole("USER");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("user-1", "testuser", "USER", 0)).thenReturn("test-token");

        Map<String, Object> result = authService.login("testuser", "password123");

        assertEquals("test-token", result.get("token"));
        assertEquals("user-1", result.get("userId"));
        assertEquals("testuser", result.get("nickname"));
    }

    @Test
    void login_shouldFail_whenUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Map<String, Object> result = authService.login("unknown", "password123");
        assertEquals("Invalid username or password", result.get("error"));
    }

    @Test
    void login_shouldNotTrackRandomUnregisteredUsernamesInAccountProtectionBuckets() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        IntStream.range(0, 10_025).parallel().forEach(attempt -> {
            Map<String, Object> result = authService.login("unknown-" + attempt, "password123");
            assertEquals("Invalid username or password", result.get("error"));
        });

        assertEquals(0, authService.trackedAttemptBucketCount());
        verify(userRepository, times(10_025)).findByUsername(anyString());
    }

    @Test
    void login_shouldFail_whenPasswordIncorrect() {
        User user = new User();
        user.setPasswordHash(new BCryptPasswordEncoder().encode("correct-password"));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        Map<String, Object> result = authService.login("testuser", "wrong-password");
        assertEquals("Invalid username or password", result.get("error"));
    }

    @Test
    void login_shouldRejectOversizedPasswordWithoutRepositoryLookup() {
        Map<String, Object> result = authService.login("testuser", "x".repeat(129));

        assertEquals("Invalid username or password", result.get("error"));
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void login_shouldRejectOversizedUsernameWithoutRepositoryLookup() {
        Map<String, Object> result = authService.login("x".repeat(65), "password123");

        assertEquals("Invalid username or password", result.get("error"));
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void login_shouldRateLimitCaseAndWhitespaceVariantsTogether() {
        User user = new User();
        user.setPasswordHash(new BCryptPasswordEncoder().encode("correct-password"));
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));

        for (String username : new String[]{"Target", "target", "TARGET", "TaRgEt", " target "}) {
            Map<String, Object> result = authService.login(username, "wrong-password");
            assertEquals("Invalid username or password", result.get("error"));
        }

        Map<String, Object> result = authService.login("TARGET", "wrong-password");
        assertEquals("Invalid username or password", result.get("error"));
        verify(userRepository, times(5)).findByUsername(anyString());
    }

    @Test
    void login_shouldNotRevealExistingUsernameThroughRateLimitResponse() {
        User user = new User();
        user.setPasswordHash(new BCryptPasswordEncoder().encode("correct-password"));
        when(userRepository.findByUsername("known")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        for (int attempt = 0; attempt < 5; attempt++) {
            authService.login("known", "wrong-password");
        }

        Map<String, Object> lockedKnown = authService.login("known", "wrong-password");
        Map<String, Object> unknown = authService.login("unknown", "wrong-password");

        assertEquals(unknown.get("error"), lockedKnown.get("error"));
        assertEquals("Invalid username or password", lockedKnown.get("error"));
    }
}
