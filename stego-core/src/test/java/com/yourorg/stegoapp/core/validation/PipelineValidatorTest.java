package com.yourorg.stegoapp.core.validation;

import com.yourorg.stegoapp.core.error.StegoErrorCode;
import com.yourorg.stegoapp.core.error.StegoException;
import com.yourorg.stegoapp.core.model.CryptoOptions;
import com.yourorg.stegoapp.core.model.Pipeline;
import com.yourorg.stegoapp.core.model.StepConfig;
import com.yourorg.stegoapp.core.model.StepType;
import com.yourorg.stegoapp.core.model.ZeroWidthMode;
import com.yourorg.stegoapp.core.model.ZeroWidthOptions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PipelineValidatorTest {

    @Test
    void nullPipelineThrows() {
        StegoException ex = assertThrows(StegoException.class, () -> PipelineValidator.validate(null));
        assertEquals(StegoErrorCode.INVALID_PIPELINE, ex.getCode());
    }

    @Test
    void cryptoRequiresCryptoOptions() {
        Pipeline pipeline = new Pipeline(List.of(new StepConfig(StepType.CRYPTO, null)));
        StegoException ex = assertThrows(StegoException.class, () -> PipelineValidator.validate(pipeline));
        assertEquals(StegoErrorCode.INVALID_OPTIONS, ex.getCode());
    }

    @Test
    void cryptoRequiresNonBlankPassword() {
        Pipeline pipeline = new Pipeline(List.of(new StepConfig(StepType.CRYPTO, new CryptoOptions("  "))));
        StegoException ex = assertThrows(StegoException.class, () -> PipelineValidator.validate(pipeline));
        assertEquals(StegoErrorCode.INVALID_OPTIONS, ex.getCode());
    }

    @Test
    void zeroWidthEmbedInCoverRequiresCoverText() {
        Pipeline pipeline = new Pipeline(List.of(
                new StepConfig(StepType.ZERO_WIDTH, new ZeroWidthOptions(ZeroWidthMode.EMBED_IN_COVER, "  "))
        ));
        StegoException ex = assertThrows(StegoException.class, () -> PipelineValidator.validate(pipeline));
        assertEquals(StegoErrorCode.INVALID_OPTIONS, ex.getCode());
    }

    @Test
    void zeroWidthWithNullModeIsRejected() {
        Pipeline pipeline = new Pipeline(List.of(
                new StepConfig(StepType.ZERO_WIDTH, new ZeroWidthOptions(null, "cover"))
        ));
        StegoException ex = assertThrows(StegoException.class, () -> PipelineValidator.validate(pipeline));
        assertEquals(StegoErrorCode.INVALID_OPTIONS, ex.getCode());
    }
}

