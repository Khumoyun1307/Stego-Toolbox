package com.yourorg.stegoapp.core;

import com.yourorg.stegoapp.core.error.StegoException;
import com.yourorg.stegoapp.core.model.CryptoOptions;
import com.yourorg.stegoapp.core.model.StepConfig;
import com.yourorg.stegoapp.core.model.StepType;
import com.yourorg.stegoapp.core.model.ZeroWidthMode;
import com.yourorg.stegoapp.core.model.ZeroWidthOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StegoFactoryTest {

    @Test
    void createEachType() {
        for (StepType type : StepType.values()) {
            StepConfig model = type == StepType.CRYPTO
                    ? new StepConfig(type, new CryptoOptions("pw"))
                    : StepConfig.of(type);
            StegoStep step = StegoFactory.create(model);
            assertNotNull(step);
            // class matches
            switch (type) {
                case ZERO_WIDTH -> assertTrue(step instanceof ZeroWidthStep);
                case BASE64     -> assertTrue(step instanceof Base64Step);
                case EMOJI      -> assertTrue(step instanceof EmojiStep);
                case CRYPTO     -> assertTrue(step instanceof CryptoStep);
            }
        }
    }

    @Test
    void zeroWidthCoverCreatesCoverStep() {
        StepConfig cfg = new StepConfig(
                StepType.ZERO_WIDTH,
                new ZeroWidthOptions(ZeroWidthMode.EMBED_IN_COVER, "Cover")
        );
        assertTrue(StegoFactory.create(cfg) instanceof ZeroWidthCoverStep);
    }

    @Test
    void invalidStepThrows() {
        assertThrows(StegoException.class, () -> StegoFactory.create((StepConfig) null));
        assertThrows(StegoException.class, () -> StegoFactory.create(new StepConfig(StepType.CRYPTO, null)));
    }
}
