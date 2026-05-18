package com.earthworm.controller;

import com.earthworm.model.User;
import com.earthworm.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Map<String, Object>> listUsers() {
        return userRepository.findAll().stream().map(u -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("nickname", u.getNickname() != null ? u.getNickname() : "");
            m.put("email", u.getEmail() != null ? u.getEmail() : "");
            m.put("avatar", u.getAvatar() != null ? u.getAvatar() : "");
            m.put("role", u.getRole() != null ? u.getRole() : "USER");
            m.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : "");
            return m;
        }).collect(Collectors.toList());
    }

    @PutMapping("/{id}/role")
    public Map<String, Object> updateRole(@PathVariable("id") String id, @RequestBody Map<String, String> body) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRole(body.get("role").toUpperCase());
        userRepository.save(user);
        return Map.of("id", user.getId(), "username", user.getUsername(), "role", user.getRole());
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateUser(@PathVariable("id") String id, @RequestBody Map<String, String> body) {
        User user = userRepository.findById(id).orElseThrow();
        if (body.containsKey("nickname")) user.setNickname(body.get("nickname"));
        if (body.containsKey("email")) {
            String email = body.get("email");
            user.setEmail(email != null && !email.isBlank() ? email : null);
        }
        if (body.containsKey("avatar")) user.setAvatar(body.get("avatar"));
        if (body.containsKey("password")) {
            String pwd = body.get("password");
            if (pwd != null && !pwd.isBlank()) {
                user.setPasswordHash(hashPassword(pwd));
            }
        }
        userRepository.save(user);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", user.getId());
        m.put("username", user.getUsername());
        m.put("nickname", user.getNickname() != null ? user.getNickname() : "");
        m.put("email", user.getEmail() != null ? user.getEmail() : "");
        m.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
        m.put("role", user.getRole() != null ? user.getRole() : "USER");
        return m;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteUser(@PathVariable("id") String id) {
        userRepository.deleteById(id);
        return Map.of("deleted", true);
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
