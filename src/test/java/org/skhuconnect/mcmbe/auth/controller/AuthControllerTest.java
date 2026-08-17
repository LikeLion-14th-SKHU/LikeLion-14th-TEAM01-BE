package org.skhuconnect.mcmbe.auth.controller;

import org.junit.jupiter.api.Test;
import org.skhuconnect.mcmbe.auth.dto.KakaoLoginResponse;
import org.skhuconnect.mcmbe.auth.dto.RefreshTokenRequest;
import org.skhuconnect.mcmbe.auth.dto.TokenResponse;
import org.skhuconnect.mcmbe.common.response.ApiResTemplate;
import org.skhuconnect.mcmbe.auth.service.AuthService;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Test
    void redirectsKakaoLoginResultToFrontendWithAuthenticationFragment() {
        AuthService authService = mock(AuthService.class);
        TokenResponse tokens = new TokenResponse(
                "Bearer",
                "access.token.value",
                1_800_000L,
                "refresh.token.value",
                1_209_600_000L
        );
        when(authService.loginWithKakao("authorization-code", "oauth-state"))
                .thenReturn(new KakaoLoginResponse(7L, true, "테스터", tokens));

        ResponseEntity<Void> response = new AuthController(authService)
                .kakaoCallback("authorization-code", "oauth-state");

        assertThat(response.getStatusCode().value()).isEqualTo(302);
        URI location = response.getHeaders().getLocation();
        assertThat(location).isNotNull();
        assertThat(location.getScheme()).isEqualTo("https");
        assertThat(location.getHost()).isEqualTo("seongju-detective.vercel.app");
        assertThat(location.getPath()).isEqualTo("/");
        assertThat(location.getQuery()).isNull();
        assertThat(URLDecoder.decode(location.getRawFragment(), StandardCharsets.UTF_8))
                .contains("tokenType=Bearer")
                .contains("accessToken=access.token.value")
                .contains("refreshToken=refresh.token.value")
                .contains("memberId=7")
                .contains("newMember=true")
                .contains("nickname=테스터");
        verify(authService).loginWithKakao("authorization-code", "oauth-state");
    }

    @Test
    void refreshReturnsRotatedAccessAndRefreshTokensWithExpirationInformation() {
        AuthService authService = mock(AuthService.class);
        TokenResponse rotated = new TokenResponse(
                "Bearer",
                "new-access-token",
                1_800L,
                "new-refresh-token",
                1_209_600L
        );
        when(authService.refresh("stored-refresh-token")).thenReturn(rotated);

        ResponseEntity<ApiResTemplate<TokenResponse>> response = new AuthController(authService)
                .refresh(new RefreshTokenRequest("stored-refresh-token"));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isEqualTo(rotated);
        assertThat(response.getBody().data().accessToken()).isNotBlank();
        assertThat(response.getBody().data().refreshToken()).isNotBlank();
        assertThat(response.getBody().data().accessTokenExpiresIn()).isPositive();
        assertThat(response.getBody().data().refreshTokenExpiresIn()).isPositive();
        verify(authService).refresh("stored-refresh-token");
    }

    @Test
    void logoutRevokesAuthenticatedMembersRefreshToken() {
        AuthService authService = mock(AuthService.class);

        ResponseEntity<Void> response = new AuthController(authService)
                .logout(new org.skhuconnect.mcmbe.auth.jwt.AuthenticatedMember(
                        7L,
                        org.skhuconnect.mcmbe.member.entity.MemberRole.USER
                ));

        assertThat(response.getStatusCode().value()).isEqualTo(204);
        verify(authService).logout(7L);
    }
}
