package com.yourorg.stegoapp.core.model;

import java.util.Objects;

/**
 * A single configured step in a pipeline.
 *
 * @param type step type to apply (required)
 * @param options step-specific options; defaults to {@link NoOptions#INSTANCE} when {@code null}
 */
public record StepConfig(StepType type, StepOptions options) {
    public StepConfig {
        Objects.requireNonNull(type, "type");
        options = (options == null) ? NoOptions.INSTANCE : options;
    }

    /**
     * Convenience factory for steps without options.
     *
     * @param type step type to apply
     * @return a {@link StepConfig} with {@link NoOptions#INSTANCE}
     */
    public static StepConfig of(StepType type) {
        return new StepConfig(type, NoOptions.INSTANCE);
    }
}
