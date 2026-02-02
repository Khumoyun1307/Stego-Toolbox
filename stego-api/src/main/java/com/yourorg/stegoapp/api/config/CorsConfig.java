package com.yourorg.stegoapp.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * CORS configuration for the API endpoints.
 * <p>
 * Configure allowed origins via {@code stego.cors.allowed-origins} (comma-separated). Defaults to
 * {@code http://localhost:5173} for local development.
 * </p>
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    private final List<String> allowedOrigins;

    public CorsConfig(@Value("#{'${stego.cors.allowed-origins:http://localhost:5173}'.split(',')}") List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.toArray(String[]::new))
                .allowedMethods("GET", "POST")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
