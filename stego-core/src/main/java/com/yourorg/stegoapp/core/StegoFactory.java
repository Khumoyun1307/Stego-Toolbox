package com.yourorg.stegoapp.core;

import com.yourorg.stegoapp.core.error.StegoErrorCode;
import com.yourorg.stegoapp.core.error.StegoException;
import com.yourorg.stegoapp.core.model.CryptoOptions;
import com.yourorg.stegoapp.core.model.StepConfig;
import com.yourorg.stegoapp.core.model.Step;
import com.yourorg.stegoapp.core.model.ZeroWidthMode;
import com.yourorg.stegoapp.core.model.ZeroWidthOptions;

/**
 * Factory to create {@link StegoStep} instances from typed pipeline models.
 * <p>
 * This class is the single mapping point between {@link StepConfig}/{@code StepOptions} and the
 * concrete step implementations.
 * </p>
 */
public final class StegoFactory {
    private StegoFactory() {}

    /**
     * Creates a {@link StegoStep} implementation for a configured pipeline step.
     *
     * @param step configured step (required)
     * @return concrete {@link StegoStep} implementation
     * @throws StegoException if the step/type/options are invalid
     */
    public static StegoStep create(StepConfig step) {
        if (step == null || step.type() == null) {
            throw new StegoException(StegoErrorCode.INVALID_PIPELINE, "Unsupported step: " + step);
        }

        return switch (step.type()) {
            case ZERO_WIDTH -> createZeroWidth(step);
            case BASE64 -> new Base64Step();
            case EMOJI -> new EmojiStep();
            case CRYPTO -> createCrypto(step);
        };
    }

    private static StegoStep createZeroWidth(StepConfig step) {
        if (step.options() instanceof ZeroWidthOptions zw && zw.mode() == ZeroWidthMode.EMBED_IN_COVER) {
            return new ZeroWidthCoverStep(zw.coverText());
        }
        return new ZeroWidthStep();
    }

    private static StegoStep createCrypto(StepConfig step) {
        if (!(step.options() instanceof CryptoOptions crypto)) {
            throw new StegoException(StegoErrorCode.INVALID_OPTIONS, "Crypto step requires password");
        }
        return new CryptoStep(crypto.password());
    }

    /**
     * Legacy factory for the pre-typed {@link Step} model.
     * <p>
     * Prefer {@link #create(StepConfig)} for new code so options are validated and represented in a
     * type-safe way.
     * </p>
     *
     * @param step The step model containing type and options
     * @return StegoStep implementation
     * @throws IllegalArgumentException if the step type is unsupported or null
     */
    @Deprecated(since = "1.0.0")
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
        };
    }
}
