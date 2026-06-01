package com.earthworm.controller;

import com.earthworm.config.UserContext;
import com.earthworm.model.User;
import com.earthworm.repository.UserRepository;
import com.earthworm.service.AdminAuditService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AdminAuditService adminAuditService;

    public AdminUserController(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder,
                               AdminAuditService adminAuditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminAuditService = adminAuditService;
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
        String role = body.get("role") == null ? "" : body.get("role").toUpperCase();
        if (!List.of("USER", "ADMIN").contains(role)) {
            throw new IllegalArgumentException("Role must be USER or ADMIN");
        }
        if (!"ADMIN".equals(role) && UserContext.getUserIdOptional().filter(id::equals).isPresent()) {
            throw new IllegalArgumentException("You cannot remove your own admin role");
        }
        user.setRole(role);
        userRepository.save(user);
        adminAuditService.record("user.role.update", "user", id);
        return Map.of("id", user.getId(), "username", user.getUsername(), "role", user.getRole());
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateUser(@PathVariable("id") String id, @RequestBody Map<String, String> body) {
        User user = userRepository.findById(id).orElseThrow();
        if (body.containsKey("nickname")) {
            String nickname = body.get("nickname");
            if (nickname != null && nickname.length() > 64) {
                throw new IllegalArgumentException("Nickname must be no more than 64 characters");
            }
            user.setNickname(nickname);
        }
        if (body.containsKey("email")) {
            String email = body.get("email");
            if (email != null && email.length() > 128) {
                throw new IllegalArgumentException("Email must be no more than 128 characters");
            }
            user.setEmail(email != null && !email.isBlank() ? email : null);
        }
        if (body.containsKey("avatar")) {
            String avatar = body.get("avatar");
            if (avatar != null && avatar.length() > 4096) {
                throw new IllegalArgumentException("Avatar URL is too long");
            }
            user.setAvatar(avatar);
        }
        if (body.containsKey("password")) {
            String pwd = body.get("password");
            if (pwd != null && !pwd.isBlank()) {
                validatePassword(pwd);
                user.setPasswordHash(passwordEncoder.encode(pwd));
                user.setTokenVersion((user.getTokenVersion() == null ? 0 : user.getTokenVersion()) + 1);
            }
        }
        userRepository.save(user);
        adminAuditService.record("user.profile.update", "user", id);
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
        if (UserContext.getUserIdOptional().filter(id::equals).isPresent()) {
            throw new IllegalArgumentException("You cannot delete your own admin account");
        }
        adminAuditService.record("user.delete.blocked", "user", id);
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "User deletion is disabled until dependent learning records can be preserved.");
    }

    private void validatePassword(String password) {
        if (password.length() > 128) {
            throw new IllegalArgumentException("Password must be no more than 128 characters");
        }
        if (password.length() < 6
                || !password.matches(".*[a-zA-Z].*")
                || !password.matches(".*\\d.*")) {
            throw new IllegalArgumentException(
                    "Password must be at least 6 characters and contain a letter and digit");
        }
    }
}
