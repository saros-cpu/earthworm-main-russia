package com.earthworm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id @Column(length = 128) private String id;
    @Column(nullable = false, unique = true, length = 64) private String username;
    @Column(name = "password_hash", nullable = false) private String passwordHash;
    @Column(length = 64) private String nickname;
    @Column(columnDefinition = "TEXT") private String avatar;
    @Column(length = 128) private String email;
    @Column(length = 32) private String role = "USER";
    @Column(name = "token_version", nullable = false) private Integer tokenVersion = 0;
    @Column(name = "created_at", insertable = false, updatable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", insertable = false, updatable = false) private LocalDateTime updatedAt;

    public String getId() { return id; } public void setId(String v) { this.id = v; }
    public String getUsername() { return username; } public void setUsername(String v) { this.username = v; }
    public String getPasswordHash() { return passwordHash; } public void setPasswordHash(String v) { this.passwordHash = v; }
    public String getNickname() { return nickname; } public void setNickname(String v) { this.nickname = v; }
    public String getAvatar() { return avatar; } public void setAvatar(String v) { this.avatar = v; }
    public String getEmail() { return email; } public void setEmail(String v) { this.email = v; }
    public String getRole() { return role; } public void setRole(String v) { this.role = v; }
    public Integer getTokenVersion() { return tokenVersion; } public void setTokenVersion(Integer v) { this.tokenVersion = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
