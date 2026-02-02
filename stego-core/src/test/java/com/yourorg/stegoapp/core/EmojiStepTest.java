package com.yourorg.stegoapp.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmojiStepTest {

    private final EmojiStep step = new EmojiStep();

    @Test
    void encodeThenDecodeReturnsOriginal() {
        String original = "Hi😊";
        String encoded  = step.encode(original);
        assertNotEquals(original, encoded);
        String decoded  = step.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void decodeInvalidEmojiThrows() {
        // use a non-mapped emoji
        String bad = "🚀🚀";
        assertThrows(IllegalArgumentException.class, () -> step.decode(bad));
    }

    @Test
    void decodeOddEmojiCountThrows() {
        assertThrows(IndexOutOfBoundsException.class, () -> step.decode("😀"));
    }

    @Test
    void emptyString() {
        assertEquals("", step.encode(""));
        assertEquals("", step.decode(""));
    }
}
