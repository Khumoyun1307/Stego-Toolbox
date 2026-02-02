package com.yourorg.stegoapp.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ZeroWidthCoverStepTest {

    @Test
    void encodePrependsCoverText() {
        ZeroWidthCoverStep step = new ZeroWidthCoverStep("Cover: ");
        String encoded = step.encode("secret");
        assertTrue(encoded.startsWith("Cover: "), "Expected cover text prefix");
        assertTrue(encoded.chars().anyMatch(c -> c == 0x200B || c == 0x200C), "Expected zero-width payload");
    }

    @Test
    void encodeThenDecodeReturnsOriginal() {
        ZeroWidthCoverStep step = new ZeroWidthCoverStep("Cover: ");
        String original = "Hello 👋";
        String encoded = step.encode(original);
        assertEquals(original, step.decode(encoded));
    }

    @Test
    void decodeIgnoresNonZeroWidthCharacters() {
        ZeroWidthCoverStep step = new ZeroWidthCoverStep("Cover: ");
        String original = "Payload";
        String encoded = step.encode(original);

        String wrapped = "prefix-" + encoded + "-suffix";
        assertEquals(original, step.decode(wrapped));
    }
}

