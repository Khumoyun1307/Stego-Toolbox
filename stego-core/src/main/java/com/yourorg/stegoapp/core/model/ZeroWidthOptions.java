package com.yourorg.stegoapp.core.model;

/**
 * Options for the Zero-Width step.
 *
 * @param mode encoding mode (optional; default behavior is {@link ZeroWidthMode#RAW})
 * @param coverText cover text required for {@link ZeroWidthMode#EMBED_IN_COVER}
 */
public record ZeroWidthOptions(ZeroWidthMode mode, String coverText) implements StepOptions {
}
