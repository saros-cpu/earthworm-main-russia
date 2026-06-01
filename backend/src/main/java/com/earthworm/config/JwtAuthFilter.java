package com.earthworm.config;

import com.earthworm.model.User;
import com.earthworm.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Component
@Order(1)
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final Set<String> PUBLIC_PREFIXES = Set.of(
            "/tool/", "/tts/", "/media/",
            "/swagger-ui/", "/v3/api-docs", "/error", "/actuator/health"
    );

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Value("${auth.session-cookie-name:EW_SESSION}")
    private String sessionCookieName = "EW_SESSION";

    public JwtAuthFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = sessionToken(request);
            boolean publicRequest = isPublicRequest(request);
            if (token == null) {
                if (publicRequest) {
                    filterChain.doFilter(request, response);
                    return;
                }
                sendUnauthorized(response, "Missing or invalid token");
                return;
            }

            try {
                String userId = jwtUtil.getUserId(token);
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalStateException("User no longer exists"));
                int currentTokenVersion = user.getTokenVersion() == null ? 0 : user.getTokenVersion();
                if (jwtUtil.getTokenVersion(token) != currentTokenVersion) {
                    throw new IllegalStateException("Session has been revoked");
                }
                String role = user.getRole() == null || user.getRole().isBlank()
                        ? "USER"
                        : user.getRole().toUpperCase();
                UserContext.setUserId(userId);
                UserContext.setRole(role);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                if (publicRequest) {
                    SecurityContextHolder.clearContext();
                    UserContext.clear();
                    filterChain.doFilter(request, response);
                    return;
                }
                sendUnauthorized(response, "Invalid or expired token");
                return;
            }

            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (path.equals("/") || path.equals("/swagger-ui.html")) {
            return true;
        }
        if ("POST".equalsIgnoreCase(request.getMethod())
                && (path.equals("/auth/register") || path.equals("/auth/login") || path.equals("/auth/logout"))) {
            return true;
        }
        if ("GET".equalsIgnoreCase(request.getMethod())
                && (path.equals("/course-pack") || path.startsWith("/course-pack/")
                || path.equals("/admin/stats") || path.equals("/auth/session") || path.equals("/auth/csrf"))) {
            return true;
        }
        return PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private String sessionToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (sessionCookieName.equals(cookie.getName()) && !cookie.getValue().isBlank()) {
                    return cookie.getValue();
                }
            }
        }
        String authHeader = request.getHeader("Authorization");
        return authHeader != null && authHeader.startsWith("Bearer ")
                ? authHeader.substring(7)
                : null;
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
