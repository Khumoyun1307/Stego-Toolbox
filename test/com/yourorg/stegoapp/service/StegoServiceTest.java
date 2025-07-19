package com.yourorg.stegoapp.service;

import com.yourorg.stegoapp.core.model.Step;
import com.yourorg.stegoapp.core.model.StepType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StegoServiceTest {

    private final StegoService svc = new StegoService();

    @Test
    void singleStepEncodeDecode() {
        Step s = new Step(StepType.BASE64);
        String msg = "ServiceTest";
        String enc = svc.encode(msg, List.of(s));
        String dec = svc.decode(enc, List.of(s));
        assertEquals(msg, dec);
    }

    @Test
    void multiStepPipeline() {
        Step b64   = new Step(StepType.BASE64);
        Step emoji = new Step(StepType.EMOJI);
        String original = "Pipeline";
        String enc = svc.encode(original, List.of(b64, emoji));
        String dec = svc.decode(enc, List.of(b64, emoji));
        assertEquals(original, dec);
    }

    @Test
    void wrongPasswordLeadsToException() {
        Step good    = new Step(StepType.CRYPTO, "correct");
        Step service = new Step(StepType.CRYPTO, "wrong");
        String secret = "PwdTest";
        // first encode with good, then decode specifying wrong password
        String cipher = svc.encode(secret, List.of(good));
        assertThrows(RuntimeException.class, () -> svc.decode(cipher, List.of(service)));
    }
}
