package com.lehangajanayake.speechapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Provides configuration for calls to the OpenAI HTTP API.
 *
 * The API key is supplied by the environment through application.yml and is
 * deliberately not added as a default header, so callers can add it per request.
 */
@Configuration
public class OpenAiConfig {

    @SuppressWarnings("unused")
    private final String apiKey;

    public OpenAiConfig(@Value("${openai.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Bean
    public RestClient openAiRestClient(RestClient.Builder builder) {
        // TODO: Add the injected key as a Bearer token per outgoing request in the service.
        // Keeping it out of default headers makes request-level authorization explicit.
        return builder.baseUrl("https://api.openai.com").build();
    }
}