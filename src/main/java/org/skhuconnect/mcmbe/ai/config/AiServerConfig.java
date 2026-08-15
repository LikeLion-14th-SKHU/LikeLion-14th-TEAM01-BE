package org.skhuconnect.mcmbe.ai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AiServerProperties.class)
public class AiServerConfig {

    @Bean
    public RestClient aiServerRestClient(AiServerProperties properties) {
        RestClient.Builder builder = RestClient.builder();
        if (properties.isConfigured()) {
            builder.baseUrl(properties.baseUrl());
        }
        return builder.build();
    }
}
