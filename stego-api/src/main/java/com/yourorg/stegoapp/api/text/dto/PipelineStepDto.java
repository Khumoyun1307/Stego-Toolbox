package com.yourorg.stegoapp.api.text.dto;

import com.yourorg.stegoapp.core.model.StepType;
import com.yourorg.stegoapp.core.model.ZeroWidthMode;
import jakarta.validation.constraints.NotNull;

/**
 * Single pipeline step provided by the API client.
 *
 * @param type step type (required)
 * @param zeroWidthMode optional mode for {@code ZERO_WIDTH}
 * @param coverText optional cover text for {@code ZERO_WIDTH} when using {@code EMBED_IN_COVER}
 */
public record PipelineStepDto(
        @NotNull StepType type,
        ZeroWidthMode zeroWidthMode,
        String coverText
) {
}
