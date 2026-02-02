package com.yourorg.stegoapp.api.text;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TextControllerIT {

    @Autowired
    private MockMvc mvc;

    @Test
    void encodeThenDecodeBase64() throws Exception {
        mvc.perform(post("/api/v1/text/encode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "Hello",
                                  "pipeline": [
                                    { "type": "BASE64" }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SGVsbG8="));

        mvc.perform(post("/api/v1/text/decode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "SGVsbG8=",
                                  "pipeline": [
                                    { "type": "BASE64" }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Hello"));
    }

    @Test
    void cryptoIsRejected() throws Exception {
        mvc.perform(post("/api/v1/text/encode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "Hello",
                                  "pipeline": [
                                    { "type": "CRYPTO" }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Stego error"))
                .andExpect(jsonPath("$.detail", containsString("CRYPTO is client-side only")));
    }

    @Test
    void decodeInvalidBase64ReturnsBadRequestProblemDetail() throws Exception {
        mvc.perform(post("/api/v1/text/decode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "not-base64",
                                  "pipeline": [
                                    { "type": "BASE64" }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void validationErrorIncludesFieldErrors() throws Exception {
        mvc.perform(post("/api/v1/text/encode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "Hello"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.pipeline").exists());
    }

    @Test
    void zeroWidthEmbedInCoverAddsCoverPrefix() throws Exception {
        mvc.perform(post("/api/v1/text/encode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "Hi",
                                  "pipeline": [
                                    { "type": "ZERO_WIDTH", "zeroWidthMode": "EMBED_IN_COVER", "coverText": "Cover: " }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result", startsWith("Cover: ")))
                .andExpect(jsonPath("$.result", anyOf(containsString("\u200B"), containsString("\u200C"))));
    }

    @Test
    void zeroWidthEmbedInCoverWithoutCoverTextIsRejected() throws Exception {
        mvc.perform(post("/api/v1/text/encode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "Hi",
                                  "pipeline": [
                                    { "type": "ZERO_WIDTH", "zeroWidthMode": "EMBED_IN_COVER", "coverText": "" }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Stego error"))
                .andExpect(jsonPath("$.code").value("INVALID_OPTIONS"))
                .andExpect(jsonPath("$.detail", containsString("coverText is required")));
    }

    @Test
    void capabilitiesIncludesZeroWidthModes() throws Exception {
        mvc.perform(get("/api/v1/capabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.steps").isArray())
                .andExpect(jsonPath("$.steps[?(@.type == 'ZERO_WIDTH')]").isNotEmpty());
    }
}
