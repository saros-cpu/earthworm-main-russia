package com.earthworm.controller;

import com.earthworm.config.UserContext;
import com.earthworm.service.AuthService;
import com.earthworm.service.PublicRequestRateLimiter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private PublicRequestRateLimiter publicRequestRateLimiter;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(authService, publicRequestRateLimiter);
        ReflectionTestUtils.setField(controller, "registrationRequestsPerMinute", 5);
        ReflectionTestUtils.setField(controller, "loginRequestsPerMinute", 30);
        ReflectionTestUtils.setField(controller, "sessionCookieSecure", true);
        UserContext.setUserId("user-1");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void me_shouldUseAuthenticatedContextUser() {
        Map<String, Object> expected = Map.of("userId", "user-1");
        when(authService.getCurrentUser("user-1")).thenReturn(expected);

        assertEquals(expected, controller.me());
        verify(authService).getCurrentUser("user-1");
    }

    @Test
    void register_shouldStopBeforePasswordHashingWhenSourceIsRateLimited() {
        MockHttpServletRequest request = requestFrom("198.51.100.10");
        when(publicRequestRateLimiter.allow("auth-register", "198.51.100.10", 5)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.register(
                        Map.of("username", "newuser", "password", "password123"),
                        request,
                        new MockHttpServletResponse()
                )
        );

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exception.getStatusCode());
        verifyNoInteractions(authService);
    }

    @Test
    void login_shouldDelegateWhenSourceIsWithinLimit() {
        MockHttpServletRequest request = requestFrom("198.51.100.10");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Map<String, String> body = Map.of("username", "user", "password", "password123");
        Map<String, Object> authentication = Map.of("token", "token", "userId", "user-1");
        when(publicRequestRateLimiter.allow("auth-login", "198.51.100.10", 30)).thenReturn(true);
        when(authService.login("user", "password123")).thenReturn(authentication);

        Map<String, Object> result = controller.login(body, request, response);

        assertEquals(Map.of("userId", "user-1"), result);
        assertFalse(result.containsKey("token"));
        String cookie = response.getHeader("Set-Cookie");
        assertTrue(cookie.contains("EW_SESSION=token"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("Secure"));
        assertTrue(cookie.contains("SameSite=Strict"));
        verify(authService).login("user", "password123");
    }

    @Test
    void session_shouldRemainAnonymousWithoutAuthenticatedContext() {
        UserContext.clear();

        assertEquals(Map.of("authenticated", false), controller.session());
        verifyNoInteractions(authService);
    }

    @Test
    void csrf_shouldReturnTokenForBrowserWrites() {
        Map<String, Object> result = controller.csrf(new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "csrf-token"));

        assertEquals("X-XSRF-TOKEN", result.get("headerName"));
        assertEquals("csrf-token", result.get("token"));
    }

    @Test
    void logout_shouldExpireSessionCookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertEquals(Map.of("loggedOut", true), controller.logout(response));
        String cookie = response.getHeader("Set-Cookie");
        assertTrue(cookie.contains("EW_SESSION="));
        assertTrue(cookie.contains("Max-Age=0"));
        assertTrue(cookie.contains("HttpOnly"));
    }

    private MockHttpServletRequest requestFrom(String address) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(address);
        return request;
    }
}
