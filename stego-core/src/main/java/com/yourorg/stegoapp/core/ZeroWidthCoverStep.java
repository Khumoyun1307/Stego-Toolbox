package com.yourorg.stegoapp.core;

import java.util.Objects;

/**
 * Zero-width encoding that appends the hidden payload to visible cover text.
 * <p>
 * Encode returns {@code coverText + zeroWidthPayload}. Decode extracts only the
 * zero-width characters from the input and decodes them.
 * </p>
 */
public final class ZeroWidthCoverStep implements StegoStep {
    private final String coverText;
    private final ZeroWidthStep zeroWidth = new ZeroWidthStep();

    /**
     * Creates a cover-text encoder/decoder.
     *
     * @param coverText visible text to prepend to the encoded payload (required)
     */
    public ZeroWidthCoverStep(String coverText) {
        this.coverText = Objects.requireNonNull(coverText, "coverText");
    }

    @Override
    public String encode(String input) {
        return coverText + zeroWidth.encode(input);
    }

    @Override
    public String decode(String input) {
        String zwOnly = input.chars()
                .filter(c -> c == 0x200B || c == 0x200C)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return zeroWidth.decode(zwOnly);
    }
}
