package org.skhuconnect.mcmbe.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai")
public record OpenAiProperties(
        String apiKey,
        String model,
        String baseUrl
) {

    public OpenAiProperties {
        apiKey = apiKey == null ? "" : apiKey;
        model = model == null ? "" : model;
        baseUrl = baseUrl == null || baseUrl.isBlank()
                ? "https://api.openai.com"
                : baseUrl;
    }

    public boolean isConfigured() {
        return !apiKey.isBlank() && !model.isBlank();
    }
}
