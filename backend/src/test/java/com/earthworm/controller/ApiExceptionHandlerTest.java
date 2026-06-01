package com.earthworm.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ApiExceptionHandlerTest {

    @Test
    void handleIoError_shouldNotReturnInternalFileDetails() {
        ApiExceptionHandler handler = new ApiExceptionHandler();

        ResponseEntity<Map<String, Object>> response = handler.handleIoError(
                new IOException("D:\\private\\media\\missing.mp4")
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Request processing failed", response.getBody().get("error"));
        assertFalse(response.getBody().get("error").toString().contains("private"));
    }
}
