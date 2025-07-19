package com.yourorg.stegoapp.core;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Emoji encoding step.
 * <p>
 * Maps bytes to a sequence of emoji based on a defined map.
 * Each byte is split into two 4-bit values, each mapped to an emoji.
 * </p>
 */
public class EmojiStep implements StegoStep {
    private static final String[] EMOJI_MAP = new String[] {
            "😀","😁","😂","😃","😄","😅","😆","😉",
            "😊","😋","😎","😍","😘","😗","😙","😚"
    };
    private static final Map<String, Byte> REVERSE_MAP = new HashMap<>();

    static {
        for (int i = 0; i < EMOJI_MAP.length; i++) {
            REVERSE_MAP.put(EMOJI_MAP[i], (byte) i);
        }
    }

    /**
     * Encodes the input string as a sequence of emoji.
     *
     * @param input The string to encode
     * @return Encoded emoji string
     */
    @Override
    public String encode(String input) {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            // split byte into two 4-bit halves
            int high = (b >> 4) & 0xF;
            int low  = b & 0xF;
            sb.append(EMOJI_MAP[high]).append(EMOJI_MAP[low]);
        }
        return sb.toString();
    }

    /**
     * Decodes a sequence of emoji back to the original string.
     *
     * @param input Emoji-encoded string
     * @return Decoded original string
     * @throws IllegalArgumentException if the emoji sequence is invalid
     */
    public String decode(String input) {
        // Convert input string into a list of emojis (2 emojis = 1 byte)
        List<String> emojis = splitEmojis(input);
        byte[] result = new byte[emojis.size() / 2];

        for (int i = 0; i < emojis.size(); i += 2) {
            Byte high = REVERSE_MAP.get(emojis.get(i));
            Byte low  = REVERSE_MAP.get(emojis.get(i + 1));
            if (high == null || low == null) {
                throw new IllegalArgumentException("Invalid emoji sequence.");
            }
            result[i / 2] = (byte) ((high << 4) | low);
        }

        return new String(result, StandardCharsets.UTF_8);
    }

    /**
     * Splits a string of emoji characters into individual emoji strings.
     *
     * @param input Emoji string
     * @return List of emoji as strings
     */
    private List<String> splitEmojis(String input) {
        List<String> emojis = new ArrayList<>();
        int i = 0;
        while (i < input.length()) {
            int cp = input.codePointAt(i);
            emojis.add(new String(Character.toChars(cp)));
            i += Character.charCount(cp);
        }
        return emojis;
    }
}
