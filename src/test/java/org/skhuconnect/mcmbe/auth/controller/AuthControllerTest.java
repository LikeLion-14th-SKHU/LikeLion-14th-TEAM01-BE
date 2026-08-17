package org.skhuconnect.mcmbe.auth.controller;

import org.junit.jupiter.api.Test;
import org.skhuconnect.mcmbe.auth.dto.LoginCodeExchangeRequest;
import org.skhuconnect.mcmbe.auth.dto.LoginExchangeResponse;
import org.skhuconnect.mcmbe.auth.dto.RefreshTokenRequest;
import org.skhuconnect.mcmbe.auth.dto.TokenResponse;
import org.skhuconnect.mcmbe.auth.service.AuthService;
import org.skhuconnect.mcmbe.common.response.ApiResTemplate;
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
    void redirectsKakaoLoginResultToFrontendWithOneTimeCode() {
        AuthService authService = mock(AuthService.class);
        when(authService.loginWithKakao("authorization-code", "oauth-state"))
                .thenReturn("one-time-login-code");

        ResponseEntity<Void> response = new AuthController(authService)
                .kakaoCallback("authorization-code", "oauth-state");

        assertThat(response.getStatusCode().value()).isEqualTo(302);
        URI location = response.getHeaders().getLocation();
        assertThat(location).isNotNull();
        assertThat(location.getScheme()).isEqualTo("https");
        assertThat(location.getHost()).isEqualTo("seongju-detective.vercel.app");
        assertThat(location.getPath()).isEqualTo("/auth/callback");
        assertThat(URLDecoder.decode(location.getQuery(), StandardCharsets.UTF_8))
                .isEqualTo("code=one-time-login-code");
        assertThat(location.getFragment()).isNull();
        verify(authService).loginWithKakao("authorization-code", "oauth-state");
    }

    @Test
    void exchangesOneTimeLoginCodeForTokenPair() {
        AuthService authService = mock(AuthService.class);
        TokenResponse tokens = new TokenResponse("Bearer", "access-token", 1_800L, "refresh-token", 86_400L);
        LoginExchangeResponse expected = new LoginExchangeResponse(7L, true, "테스터", tokens);
        when(authService.exchangeLoginCode("one-time-login-code")).thenReturn(expected);

        ResponseEntity<ApiResTemplate<LoginExchangeResponse>> response = new AuthController(authService)
                .exchange(new LoginCodeExchangeRequest("one-time-login-code"));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isEqualTo(expected);
        verify(authService).exchangeLoginCode("one-time-login-code");
    }

    @Test
    void refreshReturnsRotatedAccessAndRefreshTokensWithExpirationInformation() {
        AuthService authService = mock(AuthService.class);
        TokenResponse rotated = new TokenResponse("Bearer", "new-access-token", 1_800L, "new-refresh-token", 1_209_600L);
        when(authService.refresh("stored-refresh-token")).thenReturn(rotated);

        ResponseEntity<ApiResTemplate<TokenResponse>> response = new AuthController(authService)
                .refresh(new RefreshTokenRequest("stored-refresh-token"));

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isEqualTo(rotated);
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
