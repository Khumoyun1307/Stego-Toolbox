package com.yourorg.stegoapp.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Base64StepTest {

    private final Base64Step step = new Base64Step();

    @Test
    void encodeThenDecodeReturnsOriginal() {
        String original = "Hello, Stego!";
        String encoded  = step.encode(original);
        assertNotEquals(original, encoded);
        String decoded  = step.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void decodeInvalidInputThrows() {
        assertThrows(IllegalArgumentException.class, () -> step.decode("!!!notBase64!!!"));
    }

    @Test
    void emptyString() {
        assertEquals("", step.encode(""));
        assertEquals("", step.decode(""));
    }
}
