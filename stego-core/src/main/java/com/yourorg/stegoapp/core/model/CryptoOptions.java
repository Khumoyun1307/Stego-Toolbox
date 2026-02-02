package com.yourorg.stegoapp.core.model;

import java.util.Objects;

/**
 * Options for the Crypto step.
 *
 * @param password password used to derive the encryption key (required; validated as non-blank by
 *                 {@link com.yourorg.stegoapp.core.validation.PipelineValidator})
 */
public record CryptoOptions(String password) implements StepOptions {
    public CryptoOptions {
        Objects.requireNonNull(password, "password");
    }
}
