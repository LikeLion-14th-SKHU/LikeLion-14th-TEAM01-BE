package org.skhuconnect.mcmbe.auth.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "auth")
public record AuthProperties(
        @Valid @NotNull Kakao kakao,
        @Valid @NotNull Jwt jwt
) {

    public record Kakao(
            @NotBlank String clientId,
            String clientSecret,
            @NotBlank String redirectUri
    ) {
    }

    public record Jwt(
            @NotBlank String secret,
            @Positive long accessTokenExpiration,
            @Positive long refreshTokenExpiration
    ) {
    }
}
