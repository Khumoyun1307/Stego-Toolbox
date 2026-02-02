package com.yourorg.stegoapp.core.model;

/**
 * Types of steganography/encryption steps supported by the core engine.
 * <p>
 * Encode applies steps in the configured order; decode applies them in reverse order.
 * </p>
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
