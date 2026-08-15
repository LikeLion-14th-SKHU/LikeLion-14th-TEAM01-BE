package org.skhuconnect.mcmbe.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai-server")
public record AiServerProperties(String baseUrl) {

    public AiServerProperties {
        baseUrl = baseUrl == null ? "" : baseUrl.trim();
    }

    public boolean isConfigured() {
        return !baseUrl.isBlank();
    }
}
