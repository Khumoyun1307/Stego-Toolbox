package com.yourorg.stegoapp.core.validation;

import com.yourorg.stegoapp.core.error.StegoErrorCode;
import com.yourorg.stegoapp.core.error.StegoException;
import com.yourorg.stegoapp.core.model.CryptoOptions;
import com.yourorg.stegoapp.core.model.NoOptions;
import com.yourorg.stegoapp.core.model.Pipeline;
import com.yourorg.stegoapp.core.model.StepConfig;
import com.yourorg.stegoapp.core.model.StepOptions;
import com.yourorg.stegoapp.core.model.StepType;
import com.yourorg.stegoapp.core.model.ZeroWidthMode;
import com.yourorg.stegoapp.core.model.ZeroWidthOptions;

/**
 * Validates a {@link Pipeline} independent of UI/API concerns.
 * <p>
 * The validator enforces:
 * </p>
 * <ul>
 *   <li>Pipeline and step types are present</li>
 *   <li>CRYPTO requires a non-blank password</li>
 *   <li>ZERO_WIDTH options are validated when provided (e.g., cover text required for
 *       {@link ZeroWidthMode#EMBED_IN_COVER})</li>
 * </ul>
 * <p>
 * Failures are reported as {@link StegoException} with stable {@link StegoErrorCode}s suitable for
 * mapping to API/UI error responses.
 * </p>
 */
public final class PipelineValidator {
    private PipelineValidator() {}

    /**
     * Validates the provided pipeline.
     *
     * @param pipeline pipeline to validate (required)
     * @throws StegoException when validation fails
     */
    public static void validate(Pipeline pipeline) {
        if (pipeline == null) {
            throw new StegoException(StegoErrorCode.INVALID_PIPELINE, "pipeline is required");
        }

        for (int i = 0; i < pipeline.steps().size(); i++) {
            StepConfig step = pipeline.steps().get(i);
            if (step == null || step.type() == null) {
                throw new StegoException(StegoErrorCode.INVALID_PIPELINE, "pipeline.steps[" + i + "].type is required");
            }

            StepOptions options = step.options();
            if (options == null) {
                options = NoOptions.INSTANCE;
            }

            validateStep(i, step.type(), options);
        }
    }

    private static void validateStep(int index, StepType type, StepOptions options) {
        switch (type) {
            case BASE64, EMOJI -> {
                // no options
            }
            case ZERO_WIDTH -> validateZeroWidth(index, options);
            case CRYPTO -> validateCrypto(index, options);
        }
    }

    private static void validateCrypto(int index, StepOptions options) {
        if (!(options instanceof CryptoOptions crypto)) {
            throw new StegoException(StegoErrorCode.INVALID_OPTIONS, "pipeline.steps[" + index + "].options must be CryptoOptions");
        }
        if (crypto.password().isBlank()) {
            throw new StegoException(StegoErrorCode.INVALID_OPTIONS, "pipeline.steps[" + index + "].options.password is required");
        }
    }

    private static void validateZeroWidth(int index, StepOptions options) {
        if (!(options instanceof ZeroWidthOptions zw)) {
            return; // default RAW
        }
        if (zw.mode() == null) {
            throw new StegoException(StegoErrorCode.INVALID_OPTIONS, "pipeline.steps[" + index + "].options.mode is required");
        }
        if (zw.mode() == ZeroWidthMode.EMBED_IN_COVER && (zw.coverText() == null || zw.coverText().isBlank())) {
            throw new StegoException(StegoErrorCode.INVALID_OPTIONS, "pipeline.steps[" + index + "].options.coverText is required");
        }
    }
}
