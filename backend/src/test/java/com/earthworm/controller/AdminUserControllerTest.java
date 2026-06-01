package com.earthworm.controller;

import com.earthworm.config.UserContext;
import com.earthworm.model.User;
import com.earthworm.repository.UserRepository;
import com.earthworm.service.AdminAuditService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private AdminAuditService adminAuditService;

    private AdminUserController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminUserController(userRepository, passwordEncoder, adminAuditService);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void updateUser_shouldRejectOversizedPasswordBeforeHashingOrSaving() {
        User user = new User();
        user.setId("user-1");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.updateUser("user-1", Map.of("password", "a1" + "x".repeat(127)))
        );

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateRole_shouldRejectRemovingCurrentAdminRole() {
        User user = new User();
        user.setId("admin-1");
        when(userRepository.findById("admin-1")).thenReturn(Optional.of(user));
        UserContext.setUserId("admin-1");

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.updateRole("admin-1", Map.of("role", "USER"))
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_shouldRejectDeletingCurrentAdmin() {
        UserContext.setUserId("admin-1");

        assertThrows(IllegalArgumentException.class, () -> controller.deleteUser("admin-1"));

        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_shouldRejectPhysicalDeletionEvenForAnotherUser() {
        UserContext.setUserId("admin-1");

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> controller.deleteUser("user-2"));

        verify(adminAuditService).record("user.delete.blocked", "user", "user-2");
        verify(userRepository, never()).delete(any());
        verify(userRepository, never()).findById(any());
    }

    @Test
    void updateRole_shouldAuditSuccessfulPrivilegeChange() {
        User user = new User();
        user.setId("user-1");
        user.setUsername("reader");
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        controller.updateRole("user-1", Map.of("role", "ADMIN"));

        verify(adminAuditService).record("user.role.update", "user", "user-1");
    }
}
