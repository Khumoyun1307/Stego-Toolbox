package com.yourorg.stegoapp.core.model;

import java.util.List;
import java.util.Objects;

/**
 * Ordered list of configured steps.
 *
 * @param steps ordered list of step configs (required; copied defensively; must be non-empty)
 */
public record Pipeline(List<StepConfig> steps) {
    public Pipeline {
        Objects.requireNonNull(steps, "steps");
        steps = List.copyOf(steps);
        if (steps.isEmpty()) {
            throw new IllegalArgumentException("pipeline.steps must not be empty");
        }
        if (steps.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("pipeline.steps must not contain null entries");
        }
    }
}
