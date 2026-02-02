package com.yourorg.stegoapp.core;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class StepRoundTripTest {

    static Stream<String> samples() {
        return Stream.of(
                "",
                "ASCII",
                "Hello 👋",
                "Zażółć gęślą jaźń",
                "こんにちは世界"
        );
    }

    @ParameterizedTest
    @MethodSource("samples")
    void base64RoundTrip(String input) {
        Base64Step step = new Base64Step();
        assertEquals(input, step.decode(step.encode(input)));
    }

    @ParameterizedTest
    @MethodSource("samples")
    void emojiRoundTrip(String input) {
        EmojiStep step = new EmojiStep();
        assertEquals(input, step.decode(step.encode(input)));
    }

    @ParameterizedTest
    @MethodSource("samples")
    void zeroWidthRoundTrip(String input) {
        ZeroWidthStep step = new ZeroWidthStep();
        assertEquals(input, step.decode(step.encode(input)));
    }

    @ParameterizedTest
    @MethodSource("samples")
    void cryptoRoundTrip(String input) {
        CryptoStep step = new CryptoStep("test-password");
        assertEquals(input, step.decode(step.encode(input)));
    }
}

