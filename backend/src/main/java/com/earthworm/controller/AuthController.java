package com.earthworm.controller;

import com.earthworm.config.UserContext;
import com.earthworm.service.AuthService;
import com.earthworm.service.PublicRequestRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class AuthController {
    private final AuthService authService;
    private final PublicRequestRateLimiter publicRequestRateLimiter;

    @Value("${auth.registration-requests-per-minute:5}")
    private int registrationRequestsPerMinute;

    @Value("${auth.login-requests-per-minute:30}")
    private int loginRequestsPerMinute;

    @Value("${auth.session-cookie-name:EW_SESSION}")
    private String sessionCookieName = "EW_SESSION";

    @Value("${auth.session-cookie-secure:true}")
    private boolean sessionCookieSecure = true;

    @Value("${auth.session-cookie-same-site:Strict}")
    private String sessionCookieSameSite = "Strict";

    @Value("${jwt.expirationMs:604800000}")
    private long sessionExpirationMs = 604_800_000L;

    public AuthController(AuthService authService, PublicRequestRateLimiter publicRequestRateLimiter) {
        this.authService = authService;
        this.publicRequestRateLimiter = publicRequestRateLimiter;
    }

    @PostMapping("/auth/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body, HttpServletRequest request,
                                        HttpServletResponse response) {
        requirePublicRequestAllowed("auth-register", request, registrationRequestsPerMinute);
        return createBrowserSession(
                authService.register(body.get("username"), body.get("password"), body.get("nickname")),
                response
        );
    }

    @PostMapping("/auth/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body, HttpServletRequest request,
                                     HttpServletResponse response) {
        requirePublicRequestAllowed("auth-login", request, loginRequestsPerMinute);
        return createBrowserSession(authService.login(body.get("username"), body.get("password")), response);
    }

    @GetMapping("/auth/me")
    public Map<String, Object> me() {
        return authService.getCurrentUser(UserContext.getUserId());
    }

    @GetMapping("/auth/session")
    public Map<String, Object> session() {
        return UserContext.getUserIdOptional()
                .map(userId -> {
                    Map<String, Object> session = new LinkedHashMap<>(authService.getCurrentUser(userId));
                    session.put("authenticated", true);
                    return session;
                })
                .orElseGet(() -> Map.of("authenticated", false));
    }

    @GetMapping("/auth/csrf")
    public Map<String, Object> csrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }

    @PostMapping("/auth/logout")
    public Map<String, Object> logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie("", Duration.ZERO).toString());
        return Map.of("loggedOut", true);
    }

    private void requirePublicRequestAllowed(String feature, HttpServletRequest request, int limit) {
        if (!publicRequestRateLimiter.allow(feature, request.getRemoteAddr(), limit)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Try again later.");
        }
    }

    private Map<String, Object> createBrowserSession(Map<String, Object> authentication, HttpServletResponse response) {
        Object token = authentication.get("token");
        if (!(token instanceof String value) || value.isBlank()) {
            return authentication;
        }
        response.addHeader(HttpHeaders.SET_COOKIE, sessionCookie(value, Duration.ofMillis(sessionExpirationMs)).toString());
        Map<String, Object> browserResponse = new LinkedHashMap<>(authentication);
        browserResponse.remove("token");
        return browserResponse;
    }

    private ResponseCookie sessionCookie(String value, Duration maxAge) {
        return ResponseCookie.from(sessionCookieName, value)
                .httpOnly(true)
                .secure(sessionCookieSecure)
                .sameSite(sessionCookieSameSite)
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
