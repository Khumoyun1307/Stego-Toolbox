package com.yourorg.stegoapp.core;

/**
 * Zero-width character encoding step.
 * <p>
 * Hides data by mapping bits to zero-width spaces (U+200B) and zero-width non-joiners (U+200C).
 * Each byte is encoded as 8 zero-width characters.
 * </p>
 */
public class ZeroWidthStep implements StegoStep {
    private static final char ZW_SPACE = '\u200B';       // bit 0
    private static final char ZW_NON_JOINER = '\u200C';  // bit 1

    /**
     * Encodes the input string into a sequence of zero-width characters.
     * Each bit of each byte is mapped to a zero-width space (0) or non-joiner (1).
     *
     * @param input The string to encode
     * @return Encoded string using zero-width characters
     */
    @Override
    public String encode(String input) {
        StringBuilder binary = new StringBuilder();
        for (byte b : input.getBytes()) {
            for (int i = 7; i >= 0; i--) {
                binary.append(((b >> i) & 1) == 1 ? ZW_NON_JOINER : ZW_SPACE);
            }
        }
        return binary.toString();
    }

    /**
     * Decodes a string of zero-width characters back to the original string.
     * The input length must be a multiple of 8.
     *
     * @param input Encoded string using zero-width characters
     * @return Decoded original string
     * @throws IndexOutOfBoundsException if input length is not a multiple of 8
     */
    @Override
    public String decode(String input) {
        // ← add this guard
        if (input.length() % 8 != 0) {
            throw new IndexOutOfBoundsException(
                    "Encoded string length must be a multiple of 8, but was " + input.length()
            );
        }

        byte[] bytes = new byte[input.length() / 8];
        for (int byteIdx = 0; byteIdx < bytes.length; byteIdx++) {
            byte b = 0;
            for (int bit = 0; bit < 8; bit++) {
                char c = input.charAt(byteIdx * 8 + bit);
                b <<= 1;
                if (c == ZW_NON_JOINER) {
                    b |= 1;
                }
            }
            bytes[byteIdx] = b;
        }
        return new String(bytes);
    }
}

