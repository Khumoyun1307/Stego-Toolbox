package com.yourorg.stegoapp.core.error;

import java.util.Objects;

/**
 * Domain-level exception for predictable failures (validation, unsupported steps, crypto errors).
 * <p>
 * This exception is meant to be safely surfaced to callers (UI/API) alongside a stable
 * {@link StegoErrorCode}. Use it for "expected" failures that a user can typically correct.
 * </p>
 */
public final class StegoException extends RuntimeException {
    private final StegoErrorCode code;

    public StegoException(StegoErrorCode code, String message) {
        super(message);
        this.code = Objects.requireNonNull(code, "code");
    }

    public StegoException(StegoErrorCode code, String message, Throwable cause) {
        super(message, cause);
        this.code = Objects.requireNonNull(code, "code");
    }

    public StegoErrorCode getCode() {
        return code;
    }
}
