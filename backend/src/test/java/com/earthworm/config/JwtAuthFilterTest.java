package com.earthworm.config;

import com.earthworm.model.User;
import com.earthworm.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(jwtUtil, userRepository);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void authMe_shouldRejectMissingTokenBecauseItIsNotPublic() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/me");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        verifyNoInteractions(filterChain);
    }

    @Test
    void login_shouldRemainPublicWithoutToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void register_shouldRemainPublicWithoutToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/register");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void login_shouldIgnoreExpiredSessionCookieSoItCanBeReplaced() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setCookies(new Cookie("EW_SESSION", "expired-token"));
        when(jwtUtil.getUserId("expired-token")).thenThrow(new IllegalStateException("expired"));

        filter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void logout_shouldRemainReachableWithoutAValidSessionCookie() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/logout");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void authMe_shouldRejectRevokedTokenBeforeController() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/me");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer revoked-token");

        when(jwtUtil.getUserId("revoked-token")).thenReturn("user-1");
        when(jwtUtil.getTokenVersion("revoked-token")).thenReturn(1);
        User user = new User();
        user.setId("user-1");
        user.setTokenVersion(2);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        filter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
        verifyNoInteractions(filterChain);
    }

    @Test
    void authMe_shouldAuthenticateHttpOnlySessionCookie() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/me");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setCookies(new Cookie("EW_SESSION", "cookie-token"));

        when(jwtUtil.getUserId("cookie-token")).thenReturn("user-1");
        when(jwtUtil.getTokenVersion("cookie-token")).thenReturn(0);
        User user = new User();
        user.setId("user-1");
        user.setRole("USER");
        user.setTokenVersion(0);
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        filter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void publicSessionProbe_shouldRemainAccessibleWithoutCookie() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/session");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }
}
