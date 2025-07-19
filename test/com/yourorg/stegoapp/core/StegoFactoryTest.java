package com.yourorg.stegoapp.core;

import com.yourorg.stegoapp.core.model.Step;
import com.yourorg.stegoapp.core.model.StepType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StegoFactoryTest {

    @Test
    void createEachType() {
        for (StepType type : StepType.values()) {
            Step model = new Step(type, "pw");
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
    void unsupportedTypeThrows() {
        Step bad = new Step(null, null);
        assertThrows(IllegalArgumentException.class, () -> StegoFactory.create(bad));
    }
}
