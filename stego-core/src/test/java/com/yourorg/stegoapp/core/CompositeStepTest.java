package com.yourorg.stegoapp.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompositeStepTest {

    @Test
    void encodeThenDecodeSingleStep() {
        Base64Step b64 = new Base64Step();
        CompositeStep comp = new CompositeStep(List.of(b64));
        String text = "Chain";
        assertEquals(text, comp.decode(comp.encode(text)));
    }

    @Test
    void encodeThenDecodeMultipleSteps() {
        Base64Step b64 = new Base64Step();
        ZeroWidthStep zw = new ZeroWidthStep();
        CompositeStep comp = new CompositeStep(List.of(b64, zw));
        String original = "XYZ";
        String encoded  = comp.encode(original);
        // should contain zero-width chars
        assertTrue(encoded.chars().anyMatch(c -> c == '\u200B' || c == '\u200C'));
        String decoded  = comp.decode(encoded);
        assertEquals(original, decoded);
    }
}
