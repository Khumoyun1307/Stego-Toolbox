package com.yourorg.stegoapp.api.text;

import com.yourorg.stegoapp.api.text.dto.TextTransformRequest;
import com.yourorg.stegoapp.api.text.dto.TextTransformResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Text transformation endpoints (encode/decode) backed by {@link TextTransformService}.
 */
@RestController
@RequestMapping("/api/v1/text")
public class TextController {
    private final TextTransformService service;

    public TextController(TextTransformService service) {
        this.service = service;
    }

    /**
     * Encodes the input text using the provided pipeline (steps applied in order).
     */
    @PostMapping("/encode")
    public TextTransformResponse encode(@Valid @RequestBody TextTransformRequest request) {
        return new TextTransformResponse(service.encode(request));
    }

    /**
     * Decodes the input text using the provided pipeline (steps applied in reverse order).
     */
    @PostMapping("/decode")
    public TextTransformResponse decode(@Valid @RequestBody TextTransformRequest request) {
        return new TextTransformResponse(service.decode(request));
    }
}
