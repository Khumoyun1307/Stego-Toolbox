package com.yourorg.stegoapp.core;

/**
 * Interface for a single steganography or encryption step.
 * <p>
 * Implementations should provide reversible encode/decode transformations.
 * Unless otherwise documented, implementations are expected to be stateless or effectively
 * immutable (safe to reuse across calls).
 * </p>
 */
public interface StegoStep {
    /**
     * Encodes the input string, returning a new string containing hidden data.
     *
     * @param input The string to encode
     * @return Encoded string
     */
    String encode(String input);

    /**
     * Decodes the input string, extracting the hidden payload or reversing the step.
     *
     * @param input The encoded string
     * @return Decoded original string
     */
    String decode(String input);
}
