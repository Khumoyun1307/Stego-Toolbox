package com.yourorg.stegoapp.api.text.dto;

import java.util.List;

/**
 * Response listing supported step types and modes.
 *
 * @param steps supported steps
 */
public record CapabilitiesResponse(List<StepCapabilityDto> steps) {
}
