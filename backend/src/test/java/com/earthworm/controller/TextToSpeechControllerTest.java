package com.earthworm.controller;

import com.earthworm.service.PublicRequestRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TextToSpeechControllerTest {

    @Test
    void readBoundedAudio_shouldRejectOversizedRemoteAudio() {
        TextToSpeechController controller = new TextToSpeechController(mock(PublicRequestRateLimiter.class));
        ReflectionTestUtils.setField(controller, "maxAudioBytes", 10L);

        assertThrows(
                IOException.class,
                () -> controller.readBoundedAudio(new ByteArrayInputStream(new byte[11]))
        );
    }

    @Test
    void readBoundedAudio_shouldAllowAudioWithinLimit() throws IOException {
        TextToSpeechController controller = new TextToSpeechController(mock(PublicRequestRateLimiter.class));
        ReflectionTestUtils.setField(controller, "maxAudioBytes", 10L);

        assertArrayEquals(
                new byte[10],
                controller.readBoundedAudio(new ByteArrayInputStream(new byte[10]))
        );
    }

    @Test
    void russianTextToSpeech_shouldRejectRateLimitedRequestBeforeAnyAudioRead() throws Exception {
        PublicRequestRateLimiter rateLimiter = mock(PublicRequestRateLimiter.class);
        TextToSpeechController controller = new TextToSpeechController(rateLimiter);
        ReflectionTestUtils.setField(controller, "maxTextLength", 500);
        ReflectionTestUtils.setField(controller, "requestsPerMinute", 20);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("198.51.100.20");
        when(rateLimiter.allow("tts", "198.51.100.20", 20)).thenReturn(false);

        assertEquals(
                HttpStatus.TOO_MANY_REQUESTS,
                controller.russianTextToSpeech("safe uncached text", request).getStatusCode()
        );
    }
}
