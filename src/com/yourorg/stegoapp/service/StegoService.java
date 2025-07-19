package com.yourorg.stegoapp.service;

import com.yourorg.stegoapp.core.*;
import com.yourorg.stegoapp.core.model.Step;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Central processing service for encoding/decoding using stego steps.
 * <p>
 * Provides methods to encode and decode messages using a sequence of transformation steps.
 * </p>
 */
public class StegoService {

    /**
     * Encodes a message using one or more steps.
     *
     * @param input The original message
     * @param steps The list of transformation steps
     * @return Encoded message
     */
    public String encode(String input, List<Step> steps) {
        StegoStep strategy = resolveStrategy(steps);
        return strategy.encode(input);
    }

    /**
     * Decodes a message using one or more steps (in reverse).
     *
     * @param input The encoded message
     * @param steps The list of transformation steps
     * @return Decoded original message
     */
    public String decode(String input, List<Step> steps) {
        StegoStep strategy = resolveStrategy(steps);
        return strategy.decode(input);
    }

    /**
     * Converts a list of Step models into a single StegoStep (composite if >1).
     *
     * @param steps List of Step models
     * @return StegoStep (composite if multiple steps)
     */
    private StegoStep resolveStrategy(List<Step> steps) {
        if (steps.size() == 1) {
            return StegoFactory.create(steps.get(0));
        }
        List<StegoStep> strategyList = steps.stream()
                .map(StegoFactory::create)
                .collect(Collectors.toList());
        return new CompositeStep(strategyList);
    }
}
