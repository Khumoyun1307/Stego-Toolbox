package com.yourorg.stegoapp.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ZeroWidthStepTest {

    private final ZeroWidthStep step = new ZeroWidthStep();

    @Test
    void encodeProducesOnlyZeroWidthChars() {
        String in = "AB";
        String out = step.encode(in);
        for (char c : out.toCharArray()) {
            assertTrue(c == '\u200B' || c == '\u200C');
        }
    }

    @Test
    void encodeThenDecodeReturnsOriginal() {
        String original = "Secret!";
        String encoded  = step.encode(original);
        String decoded  = step.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void decodeBadLengthThrows() {
        // odd number of chars cannot form full bytes
        String bad = "\u200B\u200C\u200B";
        assertThrows(IllegalArgumentException.class, () -> step.decode(bad));
    }

    @Test
    void decodeIgnoresNonZeroWidthCharacters() {
        String original = "A";
        String encoded = step.encode(original);
        String wrapped = "cover:" + encoded + ":suffix";
        assertEquals(original, step.decode(wrapped));
    }
}
