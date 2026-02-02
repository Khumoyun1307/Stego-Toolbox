package com.yourorg.stegoapp.api.text;

import com.yourorg.stegoapp.api.text.dto.PipelineStepDto;
import com.yourorg.stegoapp.api.text.dto.TextTransformRequest;
import com.yourorg.stegoapp.core.error.StegoErrorCode;
import com.yourorg.stegoapp.core.error.StegoException;
import com.yourorg.stegoapp.core.model.Pipeline;
import com.yourorg.stegoapp.core.model.StepConfig;
import com.yourorg.stegoapp.core.model.StepType;
import com.yourorg.stegoapp.core.model.ZeroWidthMode;
import com.yourorg.stegoapp.core.model.ZeroWidthOptions;
import com.yourorg.stegoapp.service.StegoService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps API DTOs to the core engine pipeline model and executes encode/decode.
 * <p>
 * The API intentionally rejects {@link StepType#CRYPTO} so passwords and keys never transit the server.
 * </p>
 */
@Service
public class TextTransformService {
    private final StegoService engine = new StegoService();

    /**
     * Encodes request text using the configured pipeline.
     */
    public String encode(TextTransformRequest request) {
        Pipeline pipeline = toPipeline(request.pipeline());
        return engine.encode(request.text(), pipeline);
    }

    /**
     * Decodes request text using the configured pipeline (applied in reverse order).
     */
    public String decode(TextTransformRequest request) {
        Pipeline pipeline = toPipeline(request.pipeline());
        return engine.decode(request.text(), pipeline);
    }

    private static Pipeline toPipeline(List<PipelineStepDto> steps) {
        List<StepConfig> mapped = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            PipelineStepDto s = steps.get(i);
            if (s.type() == StepType.CRYPTO) {
                throw new StegoException(StegoErrorCode.UNSUPPORTED_STEP, "CRYPTO is client-side only. Encrypt/decrypt before calling the API.");
            }

            if (s.type() == StepType.ZERO_WIDTH && s.zeroWidthMode() == ZeroWidthMode.EMBED_IN_COVER) {
                mapped.add(new StepConfig(StepType.ZERO_WIDTH, new ZeroWidthOptions(ZeroWidthMode.EMBED_IN_COVER, s.coverText())));
                continue;
            }

            mapped.add(StepConfig.of(s.type()));
        }
        return new Pipeline(mapped);
    }
}
