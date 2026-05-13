package com.earthworm.service;

import com.earthworm.config.JwtUtil;
import com.earthworm.model.User;
import com.earthworm.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, Object> register(String username, String password) {
        if (username == null || username.length() < 2) {
            return Map.of("error", "Username must be at least 2 characters");
        }
        if (password == null || password.length() < 4) {
            return Map.of("error", "Password must be at least 4 characters");
        }
        if (userRepository.existsByUsername(username)) {
            return Map.of("error", "Username already exists");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username);
        user.setPasswordHash(hashPassword(password));
        user.setNickname(username);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return Map.of("token", token, "userId", user.getId(), "username", user.getUsername(), "nickname", user.getNickname());
    }

    public Map<String, Object> login(String username, String password) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return Map.of("error", "User not found");
        }
        if (!user.getPasswordHash().equals(hashPassword(password))) {
            return Map.of("error", "Incorrect password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return Map.of("token", token, "userId", user.getId(), "username", user.getUsername(), "nickname", user.getNickname());
    }

    public Map<String, Object> getCurrentUser(String userId) {
        return userRepository.findById(userId)
                .<Map<String, Object>>map(u -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("userId", u.getId());
                    m.put("username", u.getUsername());
                    m.put("nickname", u.getNickname() != null ? u.getNickname() : u.getUsername());
                    m.put("avatar", u.getAvatar() != null ? u.getAvatar() : "");
                    return m;
                })
                .orElse(Map.of("error", "not found"));
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(password.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
