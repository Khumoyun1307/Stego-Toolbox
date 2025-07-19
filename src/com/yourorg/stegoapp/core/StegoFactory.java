package com.yourorg.stegoapp.core;

import com.yourorg.stegoapp.core.model.Step;

/**
 * Factory to create StegoStep instances from Step models.
 */
public class StegoFactory {
    /**
     * Creates a StegoStep implementation based on the provided Step model.
     *
     * @param step The step model containing type and options
     * @return StegoStep implementation
     * @throws IllegalArgumentException if the step type is unsupported or null
     */
    public static StegoStep create(Step step) {
        // Validate input
        if (step == null || step.getType() == null) {
            throw new IllegalArgumentException("Unsupported step type: " + step);
        }

        return switch (step.getType()) {
            case ZERO_WIDTH -> new ZeroWidthStep();
            case BASE64    -> new Base64Step();
            case EMOJI     -> new EmojiStep();
            case CRYPTO    -> new CryptoStep(step.getPassword());
            default        -> throw new IllegalArgumentException("Unsupported step type: " + step.getType());
        };
    }
}
