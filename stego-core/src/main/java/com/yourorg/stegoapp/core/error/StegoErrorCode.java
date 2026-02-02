package com.yourorg.stegoapp.core.error;

/**
 * Stable error codes for predictable, client-facing failures.
 * <p>
 * These are intended to be mapped to API responses and UI messages. Values are part of the public
 * contract; avoid renaming existing constants.
 * </p>
 */
public enum StegoErrorCode {
    /** Input payload is invalid (e.g., malformed encoded string). */
    INVALID_INPUT,
    /** Pipeline is missing/invalid (e.g., null pipeline, null step/type). */
    INVALID_PIPELINE,
    /** Requested step is not supported in the current context. */
    UNSUPPORTED_STEP,
    /** Step options are missing or invalid for the selected step. */
    INVALID_OPTIONS,
    /** Crypto operation failed (wrong password, tampered data, unsupported algorithm, etc.). */
    CRYPTO_ERROR
}
