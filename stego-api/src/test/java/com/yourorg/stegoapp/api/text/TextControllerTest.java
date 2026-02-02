package com.yourorg.stegoapp.api.text;

import com.yourorg.stegoapp.api.error.ApiExceptionHandler;
import com.yourorg.stegoapp.core.error.StegoErrorCode;
import com.yourorg.stegoapp.core.error.StegoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TextController.class)
@Import(ApiExceptionHandler.class)
class TextControllerWebMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TextTransformService service;

    @Test
    void stegoExceptionIsMappedToProblemDetail() throws Exception {
        when(service.encode(any()))
                .thenThrow(new StegoException(StegoErrorCode.INVALID_OPTIONS, "bad options"));

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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Stego error"))
                .andExpect(jsonPath("$.code").value("INVALID_OPTIONS"))
                .andExpect(jsonPath("$.detail").value("bad options"));
    }

    @Test
    void illegalArgumentIsMappedToBadRequestProblemDetail() throws Exception {
        when(service.decode(any()))
                .thenThrow(new IllegalArgumentException("Invalid encoded format"));

        mvc.perform(post("/api/v1/text/decode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "bad",
                                  "pipeline": [
                                    { "type": "BASE64" }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"))
                .andExpect(jsonPath("$.detail", containsString("Invalid encoded format")));
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
    void validationErrorIncludesNestedFieldErrors() throws Exception {
        mvc.perform(post("/api/v1/text/encode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "Hello",
                                  "pipeline": [
                                    {}
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors['pipeline[0].type']").exists());
    }
}
