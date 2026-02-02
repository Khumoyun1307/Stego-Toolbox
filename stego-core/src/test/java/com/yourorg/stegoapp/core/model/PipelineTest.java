package com.yourorg.stegoapp.core.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PipelineTest {

    @Test
    void stepsMustNotBeEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new Pipeline(List.of()));
    }

    @Test
    void stepsMustNotContainNullEntries() {
        List<StepConfig> steps = new ArrayList<>();
        steps.add(null);
        assertThrows(NullPointerException.class, () -> new Pipeline(steps));
    }

    @Test
    void constructorDefensivelyCopiesSteps() {
        List<StepConfig> mutable = new ArrayList<>();
        mutable.add(StepConfig.of(StepType.BASE64));

        Pipeline pipeline = new Pipeline(mutable);
        mutable.add(StepConfig.of(StepType.EMOJI));

        assertEquals(1, pipeline.steps().size(), "Pipeline should not reflect mutations of the input list");
    }
}
