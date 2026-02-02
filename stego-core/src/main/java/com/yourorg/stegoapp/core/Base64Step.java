package com.yourorg.stegoapp.core;

import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * Base64 encoding step.
 * <p>
 * Encodes and decodes strings using Base64 (RFC 4648) over UTF-8 bytes.
 * </p>
 */
public class Base64Step implements StegoStep {
    /**
     * Encodes the input string using Base64.
     *
     * @param input The string to encode
     * @return Base64-encoded string
     */
    @Override
    public String encode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Decodes a Base64-encoded string.
     *
     * @param input Base64-encoded string
     * @return Decoded original string
     */
    @Override
    public String decode(String input) {
        return new String(Base64.getDecoder().decode(input), StandardCharsets.UTF_8);
    }
}
