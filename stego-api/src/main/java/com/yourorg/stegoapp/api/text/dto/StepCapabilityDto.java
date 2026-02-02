package com.yourorg.stegoapp.api.text.dto;

import com.yourorg.stegoapp.core.model.StepType;
import com.yourorg.stegoapp.core.model.ZeroWidthMode;

import java.util.List;

/**
 * Capability information for a single step type.
 *
 * @param type step type
 * @param zeroWidthModes supported modes when {@code type == ZERO_WIDTH}; empty otherwise
 */
public record StepCapabilityDto(StepType type, List<ZeroWidthMode> zeroWidthModes) {
}
