package com.yourorg.stegoapp.api.text.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Text encode/decode request payload.
 *
 * @param text input text to encode/decode
 * @param pipeline ordered list of steps (must contain at least one step)
 */
public record TextTransformRequest(
        @NotNull String text,
        @NotNull @Size(min = 1) List<@Valid PipelineStepDto> pipeline
) {
}
