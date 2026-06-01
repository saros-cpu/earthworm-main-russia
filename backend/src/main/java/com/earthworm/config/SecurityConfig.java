package com.earthworm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;

    @Value("${cors.allowedOrigins}")
    private String allowedOrigins;

    @Value("${auth.session-cookie-secure:true}")
    private boolean sessionCookieSecure;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookieCustomizer(cookie -> cookie
                .path("/")
                .secure(sessionCookieSecure)
                .sameSite("Strict"));
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .ignoringRequestMatchers("/auth/login", "/auth/register"))
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("base-uri 'self'; frame-ancestors 'self'; object-src 'none'"))
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .frameOptions(frame -> frame.sameOrigin())
                        .contentTypeOptions(Customizer.withDefaults()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login", "/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/session", "/auth/csrf").permitAll()
                        .requestMatchers("/", "/tool/**", "/tts/**", "/media/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/course-pack", "/course-pack/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/admin/stats", "/actuator/health").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(
                java.util.Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .toList()
        );
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
