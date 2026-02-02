package com.yourorg.stegoapp.core.model;

/**
 * Encoding mode for zero-width characters.
 */
public enum ZeroWidthMode {
    /**
     * Output contains only zero-width characters.
     */
    RAW,
    /**
     * Output contains visible cover text with zero-width characters appended.
     */
    EMBED_IN_COVER
}

