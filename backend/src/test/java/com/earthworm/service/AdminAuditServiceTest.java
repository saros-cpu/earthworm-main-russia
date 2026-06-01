package com.earthworm.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminAuditServiceTest {
    @Test
    void safeValue_shouldRemoveLineBreaksAndBoundLoggedIdentifiers() {
        AdminAuditService service = new AdminAuditService();

        assertEquals("user_delete_attempt", service.safeValue("user\ndelete attempt"));
        assertEquals(128, service.safeValue("x".repeat(140)).length());
    }
}
