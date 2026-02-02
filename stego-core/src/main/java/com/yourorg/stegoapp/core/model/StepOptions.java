package com.yourorg.stegoapp.core.model;

/**
 * Marker interface for step options.
 */
public sealed interface StepOptions permits NoOptions, CryptoOptions, ZeroWidthOptions {
}

