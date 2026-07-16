package com.earthworm.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {

    @Test
    void constructor_shouldRejectPredictablePlaceholderSecrets() {
        assertThrows(IllegalStateException.class,
                () -> new JwtUtil("CHANGE_ME_ON_TARGET_MACHINE_RUN_POWERSHELL", 60_000));
        assertThrows(IllegalStateException.class,
                () -> new JwtUtil("YOUR_RANDOM_256_BIT_STRING_REPLACE_THIS", 60_000));
        assertThrows(IllegalStateException.class,
                () -> new JwtUtil("dev-temp-secret-long-enough-to-be-accepted-by-hmac", 60_000));
    }

    @Test
    void generatedStrengthSecret_shouldStillSignAndParseTokens() {
        JwtUtil jwtUtil = new JwtUtil("1c0527407e3441d8980179b8fb42b5f14b017a936ca5497a850f068e3d131a77", 60_000);

        String token = jwtUtil.generateToken("user-1", "reader", "USER", 3);

        assertEquals("user-1", jwtUtil.getUserId(token));
        assertEquals(3, jwtUtil.getTokenVersion(token));
    }
}
