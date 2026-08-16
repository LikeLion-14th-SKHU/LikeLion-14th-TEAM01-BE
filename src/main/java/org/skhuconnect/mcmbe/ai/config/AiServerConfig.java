package org.skhuconnect.mcmbe.ai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(AiServerProperties.class)
public class AiServerConfig {

    static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    static final Duration READ_TIMEOUT = Duration.ofSeconds(30);

    @Bean
    public RestClient aiServerRestClient(AiServerProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(READ_TIMEOUT);

        RestClient.Builder builder = RestClient.builder()
                .requestFactory(requestFactory);
        if (properties.isConfigured()) {
            builder.baseUrl(properties.baseUrl());
        }
        return builder.build();
    }
}
