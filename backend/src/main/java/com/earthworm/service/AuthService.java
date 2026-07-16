package com.earthworm.service;

import com.earthworm.config.JwtUtil;
import com.earthworm.model.User;
import com.earthworm.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    // Login throttling is keyed case-insensitively to match common MySQL username collations.
    private final ConcurrentHashMap<String, Deque<Long>> loginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 900_000; // 15 分钟
    private static final int MAX_PASSWORD_LENGTH = 128;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, Object> register(String username, String password, String nickname) {
        String normalizedUsername = username == null ? "" : username.trim();
        if (normalizedUsername.length() < 2) {
            return Map.of("error", "Username must be at least 2 characters");
        }
        if (normalizedUsername.length() > 64) {
            return Map.of("error", "Username must be no more than 64 characters");
        }
        if (password == null || password.length() < 6) {
            return Map.of("error", "Password must be at least 6 characters");
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return Map.of("error", "Password must be no more than 128 characters");
        }
        if (!password.matches(".*[a-zA-Z].*") || !password.matches(".*\\d.*")) {
            return Map.of("error", "Password must contain at least one letter and one digit");
        }
        String normalizedNickname = nickname == null || nickname.isBlank() ? normalizedUsername : nickname.trim();
        if (normalizedNickname.length() > 64) {
            return Map.of("error", "Nickname must be no more than 64 characters");
        }
        if (userRepository.existsByUsername(normalizedUsername)) {
            return Map.of("error", "Username already exists");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(normalizedUsername);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setNickname(normalizedNickname);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), normalizedRole(user), tokenVersion(user));
        return authResponse(user, token);
    }

    public Map<String, Object> login(String username, String password) {
        String loginUsername = username == null ? "" : username.trim();
        if (loginUsername.isBlank() || loginUsername.length() > 64
                || password == null || password.length() > MAX_PASSWORD_LENGTH) {
            return Map.of("error", "Invalid username or password");
        }
        String attemptKey = loginUsername.toLowerCase(Locale.ROOT);
        if (!checkRateLimit(attemptKey)) {
            return invalidCredentials();
        }

        User user = userRepository.findByUsername(loginUsername).orElse(null);
        if (user == null) {
            return invalidCredentials();
        }
        if (!checkPassword(password, user.getPasswordHash())) {
            recordAttempt(attemptKey);
            return invalidCredentials();
        }

        loginAttempts.remove(attemptKey);

        // 升级旧 SHA-256 密码到 bcrypt
        if (isOldSha256Hash(user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(password));
            userRepository.save(user);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), normalizedRole(user), tokenVersion(user));
        return authResponse(user, token);
    }

    private Map<String, Object> invalidCredentials() {
        return Map.of("error", "Invalid username or password");
    }

    public Map<String, Object> getCurrentUser(String userId) {
        return userRepository.findById(userId)
                .<Map<String, Object>>map(u -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("userId", u.getId());
                    m.put("username", u.getUsername());
                    m.put("nickname", u.getNickname() != null ? u.getNickname() : u.getUsername());
                    m.put("avatar", u.getAvatar() != null ? u.getAvatar() : "");
                    m.put("role", normalizedRole(u));
                    return m;
                })
                .orElse(Map.of("error", "not found"));
    }

    private Map<String, Object> authResponse(User user, String token) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("token", token);
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("nickname", user.getNickname() != null ? user.getNickname() : user.getUsername());
        response.put("role", normalizedRole(user));
        return response;
    }

    private String normalizedRole(User user) {
        return user.getRole() == null || user.getRole().isBlank() ? "USER" : user.getRole().toUpperCase();
    }

    private int tokenVersion(User user) {
        return user.getTokenVersion() == null ? 0 : user.getTokenVersion();
    }

    private boolean checkRateLimit(String username) {
        Deque<Long> attempts = loginAttempts.get(username);
        if (attempts == null) return true;
        long now = Instant.now().toEpochMilli();
        synchronized (attempts) {
            while (!attempts.isEmpty() && now - attempts.peekFirst() > WINDOW_MS) {
                attempts.pollFirst();
            }
            return attempts.size() < MAX_ATTEMPTS;
        }
    }

    private void recordAttempt(String username) {
        Deque<Long> attempts = loginAttempts.computeIfAbsent(username, ignored -> new ArrayDeque<>());
        synchronized (attempts) {
            attempts.addLast(Instant.now().toEpochMilli());
        }
    }

    int trackedAttemptBucketCount() {
        return loginAttempts.size();
    }

    private boolean checkPassword(String rawPassword, String storedHash) {
        if (passwordEncoder.matches(rawPassword, storedHash)) {
            return true;
        }
        return checkSha256Password(rawPassword, storedHash);
    }

    private boolean checkSha256Password(String rawPassword, String storedHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String sha256hash = HexFormat.of().formatHex(md.digest(rawPassword.getBytes()));
            return sha256hash.equals(storedHash);
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
    }

    private boolean isOldSha256Hash(String hash) {
        return hash != null && hash.length() == 64
                && !hash.startsWith("$2a$") && !hash.startsWith("$2b$") && !hash.startsWith("$2y$");
    }
}
