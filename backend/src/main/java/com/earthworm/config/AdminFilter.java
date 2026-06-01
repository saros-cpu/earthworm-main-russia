package com.earthworm.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component @Order(2)
public class AdminFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/admin") && !path.equals("/admin/stats")) {
            String role = UserContext.getRole();
            if (!"ADMIN".equalsIgnoreCase(role)) {
                response.setStatus(403);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Forbidden: admin access required\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
