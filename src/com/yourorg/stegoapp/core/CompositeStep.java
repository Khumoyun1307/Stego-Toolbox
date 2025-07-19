package com.yourorg.stegoapp.core;

import java.util.List;

/**
 * Composite step that chains multiple StegoStep implementations in a specified order.
 * <p>
 * The encode() method applies each step in order; decode() applies in reverse order.
 * </p>
 */
public class CompositeStep implements StegoStep {
    private final List<StegoStep> steps;

    /**
     * Constructs a composite step from a list of StegoStep implementations.
     *
     * @param steps Ordered list of steps to apply. encode() applies in order; decode() in reverse order.
     */
    public CompositeStep(List<StegoStep> steps) {
        this.steps = steps;
    }

    /**
     * Applies all steps in order to encode the input.
     *
     * @param input The string to encode
     * @return Encoded string after all steps
     */
    @Override
    public String encode(String input) {
        String result = input;
        for (StegoStep step : steps) {
            result = step.encode(result);
        }
        return result;
    }

    /**
     * Applies all steps in reverse order to decode the input.
     *
     * @param input The encoded string
     * @return Decoded original string
     */
    @Override
    public String decode(String input) {
        String result = input;
        for (int i = steps.size() - 1; i >= 0; i--) {
            result = steps.get(i).decode(result);
        }
        return result;
    }
}
