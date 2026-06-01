package com.earthworm.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;

@Component
public class JwtUtil {
    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expirationMs:604800000}") long expirationMs
    ) {
        String normalizedSecret = secret == null ? "" : secret.trim().toUpperCase(Locale.ROOT);
        if (secret == null || secret.isBlank()
                || normalizedSecret.startsWith("CHANGE_ME_")
                || normalizedSecret.startsWith("YOUR_RANDOM_")
                || normalizedSecret.startsWith("DEV-TEMP-")) {
            throw new IllegalStateException("JWT_SECRET must be set to a securely generated value.");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String userId, String username, String role, int tokenVersion) {
        return Jwts.builder()
                .subject(userId)
                .claim("username", username)
                .claim("role", role)
                .claim("tokenVersion", tokenVersion)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUserId(String token) {
        return parseToken(token).getSubject();
    }

    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public int getTokenVersion(String token) {
        Integer value = parseToken(token).get("tokenVersion", Integer.class);
        return value == null ? 0 : value;
    }
}
