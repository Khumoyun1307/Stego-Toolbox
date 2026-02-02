package com.yourorg.stegoapp.core.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StepConfigTest {

    @Test
    void nullOptionsDefaultToNoOptions() {
        StepConfig cfg = new StepConfig(StepType.BASE64, null);
        assertSame(NoOptions.INSTANCE, cfg.options());
    }

    @Test
    void ofUsesNoOptions() {
        StepConfig cfg = StepConfig.of(StepType.EMOJI);
        assertSame(NoOptions.INSTANCE, cfg.options());
    }
}

