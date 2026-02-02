package com.yourorg.stegoapp.api.error;

import com.yourorg.stegoapp.core.error.StegoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Central exception mapping for the API.
 * <p>
 * Produces RFC 7807 style responses via Spring's {@link ProblemDetail}. Domain failures from the
 * core engine are surfaced as {@code 400 Bad Request} with a stable {@code code} property
 * containing {@link com.yourorg.stegoapp.core.error.StegoErrorCode}.
 * </p>
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(StegoException.class)
    public ResponseEntity<ProblemDetail> handleStegoException(StegoException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Stego error");
        pd.setDetail(ex.getMessage());
        pd.setType(URI.create("https://errors.stego-tool.local/" + ex.getCode().name()));
        pd.setInstance(URI.create(request.getRequestURI()));
        pd.setProperty("code", ex.getCode().name());
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Invalid request");
        pd.setDetail(ex.getMessage());
        pd.setType(URI.create("https://errors.stego-tool.local/INVALID_REQUEST"));
        pd.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation failed");
        pd.setDetail("One or more fields are invalid.");
        pd.setType(URI.create("https://errors.stego-tool.local/VALIDATION_FAILED"));
        pd.setInstance(URI.create(request.getRequestURI()));

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        pd.setProperty("errors", fieldErrors);
        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Internal error");
        pd.setDetail("Unexpected server error.");
        pd.setType(URI.create("https://errors.stego-tool.local/INTERNAL_ERROR"));
        pd.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }
}
