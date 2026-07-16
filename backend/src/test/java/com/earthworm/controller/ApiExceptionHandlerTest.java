package com.earthworm.controller;

import com.earthworm.model.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiExceptionHandlerTest {

    @Test
    void handleIoError_shouldNotReturnInternalFileDetails() {
        ApiExceptionHandler handler = new ApiExceptionHandler();

        ResponseEntity<ApiResponse<Void>> response = handler.handleIoError(
                new IOException("D:\\private\\media\\missing.mp4")
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Request processing failed", response.getBody().getMessage());
    }
}
