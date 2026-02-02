package com.yourorg.stegoapp.api.text;

import com.yourorg.stegoapp.api.text.dto.CapabilitiesResponse;
import com.yourorg.stegoapp.api.text.dto.StepCapabilityDto;
import com.yourorg.stegoapp.core.model.StepType;
import com.yourorg.stegoapp.core.model.ZeroWidthMode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes the API's supported text transformation capabilities.
 * <p>
 * This endpoint is intended for clients that want to render a step picker dynamically.
 * </p>
 */
@RestController
@RequestMapping("/api/v1")
public class CapabilitiesController {

    /**
     * Returns the list of supported step types and their supported modes/options.
     */
    @GetMapping("/capabilities")
    public CapabilitiesResponse capabilities() {
        return new CapabilitiesResponse(List.of(
                new StepCapabilityDto(StepType.BASE64, List.of()),
                new StepCapabilityDto(StepType.EMOJI, List.of()),
                new StepCapabilityDto(StepType.ZERO_WIDTH, List.of(ZeroWidthMode.RAW, ZeroWidthMode.EMBED_IN_COVER))
        ));
    }
}
