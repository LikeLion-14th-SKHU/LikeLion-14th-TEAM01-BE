package org.skhuconnect.mcmbe.auth.dto;

public record TokenResponse(
        String tokenType,
        String accessToken,
        long accessTokenExpiresIn,
        String refreshToken,
        long refreshTokenExpiresIn
) {
}
