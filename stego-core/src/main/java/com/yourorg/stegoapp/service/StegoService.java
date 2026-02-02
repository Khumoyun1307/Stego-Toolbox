package com.yourorg.stegoapp.service;

import com.yourorg.stegoapp.core.StegoFactory;
import com.yourorg.stegoapp.core.StegoStep;
import com.yourorg.stegoapp.core.model.Pipeline;
import com.yourorg.stegoapp.core.model.StepConfig;
import com.yourorg.stegoapp.core.validation.PipelineValidator;

import java.util.List;
import java.util.Objects;

/**
 * Central processing service for encoding/decoding using stego steps.
 * <p>
 * Provides methods to encode and decode messages using a sequence of transformation steps.
 * This type is stateless; it may be reused safely across calls.
 * </p>
 */
public class StegoService {

    /**
     * Encodes a message using one or more steps.
     *
     * @param input The original message
     * @param pipeline The ordered transformation pipeline
     * @return Encoded message
     * @throws com.yourorg.stegoapp.core.error.StegoException if the pipeline is invalid
     */
    public String encode(String input, Pipeline pipeline) {
        Objects.requireNonNull(input, "input");
        PipelineValidator.validate(pipeline);

        String result = input;
        for (StepConfig step : pipeline.steps()) {
            StegoStep strategy = StegoFactory.create(step);
            result = strategy.encode(result);
        }
        return result;
    }

    /**
     * Decodes a message using one or more steps (in reverse).
     *
     * @param input The encoded message
     * @param pipeline The ordered transformation pipeline
     * @return Decoded original message
     * @throws com.yourorg.stegoapp.core.error.StegoException if the pipeline is invalid
     */
    public String decode(String input, Pipeline pipeline) {
        Objects.requireNonNull(input, "input");
        PipelineValidator.validate(pipeline);

        String result = input;
        List<StepConfig> steps = pipeline.steps();
        for (int i = steps.size() - 1; i >= 0; i--) {
            StegoStep strategy = StegoFactory.create(steps.get(i));
            result = strategy.decode(result);
        }
        return result;
    }

    /**
     * Convenience overload for callers that already have a step list.
     */
    public String encode(String input, List<StepConfig> steps) {
        return encode(input, new Pipeline(steps));
    }

    /**
     * Convenience overload for callers that already have a step list.
     */
    public String decode(String input, List<StepConfig> steps) {
        return decode(input, new Pipeline(steps));
    }
}
