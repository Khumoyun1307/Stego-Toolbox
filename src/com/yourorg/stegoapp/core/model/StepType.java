package com.yourorg.stegoapp.core.model;

/**
 * Types of stego/encryption steps supported.
 */
public enum StepType {
    /** Zero-width character encoding */
    ZERO_WIDTH,
    /** Base64 encoding */
    BASE64,
    /** Emoji encoding */
    EMOJI,
    /** Password-based encryption */
    CRYPTO
}