package com.yourorg.stegoapp.service;

import com.yourorg.stegoapp.core.model.CryptoOptions;
import com.yourorg.stegoapp.core.model.StepConfig;
import com.yourorg.stegoapp.core.model.StepType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StegoServiceTest {

    private final StegoService svc = new StegoService();

    @Test
    void singleStepEncodeDecode() {
        StepConfig s = StepConfig.of(StepType.BASE64);
        String msg = "ServiceTest";
        String enc = svc.encode(msg, List.of(s));
        String dec = svc.decode(enc, List.of(s));
        assertEquals(msg, dec);
    }

    @Test
    void multiStepPipeline() {
        StepConfig b64   = StepConfig.of(StepType.BASE64);
        StepConfig emoji = StepConfig.of(StepType.EMOJI);
        String original = "Pipeline";
        String enc = svc.encode(original, List.of(b64, emoji));
        String dec = svc.decode(enc, List.of(b64, emoji));
        assertEquals(original, dec);
    }

    @Test
    void wrongPasswordLeadsToException() {
        StepConfig good    = new StepConfig(StepType.CRYPTO, new CryptoOptions("correct"));
        StepConfig service = new StepConfig(StepType.CRYPTO, new CryptoOptions("wrong"));
        String secret = "PwdTest";
        // first encode with good, then decode specifying wrong password
        String cipher = svc.encode(secret, List.of(good));
        assertThrows(RuntimeException.class, () -> svc.decode(cipher, List.of(service)));
    }
}
