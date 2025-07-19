package com.yourorg.stegoapp.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CryptoStepTest {

    private final String password = "p@ssw0rd";
    private final CryptoStep step = new CryptoStep(password);

    @Test
    void encodeProducesThreeParts() {
        String cipher = step.encode("TopSecret");
        String[] parts = cipher.split(":");
        assertEquals(3, parts.length, "Expected salt:iv:ciphertext");
        // each part must be valid Base64
        for (String p : parts) {
            assertDoesNotThrow(() -> java.util.Base64.getDecoder().decode(p));
        }
    }

    @Test
    void encodeThenDecodeReturnsOriginal() {
        String original = "HiddenMessage";
        String cipher   = step.encode(original);
        String plain    = step.decode(cipher);
        assertEquals(original, plain);
    }

    @Test
    void decodeBadFormatThrows() {
        assertThrows(IllegalArgumentException.class, () -> step.decode("too:many:colons:here"));
    }

    @Test
    void decodeTamperedDataThrows() {
        String cipher = step.encode("Test");
        // flip a character
        StringBuilder sb = new StringBuilder(cipher);
        sb.setCharAt(sb.length() - 1, sb.charAt(sb.length() - 1) == 'A' ? 'B' : 'A');
        assertThrows(RuntimeException.class, () -> step.decode(sb.toString()));
    }
}
