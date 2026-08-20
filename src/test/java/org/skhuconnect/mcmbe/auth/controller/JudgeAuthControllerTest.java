package org.skhuconnect.mcmbe.auth.controller;

import org.junit.jupiter.api.Test;
import org.skhuconnect.mcmbe.auth.dto.JudgeLoginRequest;
import org.skhuconnect.mcmbe.auth.dto.LoginExchangeResponse;
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

class JudgeAuthControllerTest {

    @Test
    void logsInJudgeAccountForTokenPair() {
        AuthService authService = mock(AuthService.class);
        JudgeLoginRequest request = new JudgeLoginRequest("judge-mcm", "MCM1976!");
        TokenResponse tokens = new TokenResponse("Bearer", "access-token", 1_800L, "refresh-token", 86_400L);
        LoginExchangeResponse expected = new LoginExchangeResponse(8L, false, "심사위원", tokens);
        when(authService.loginAsJudge(request)).thenReturn(expected);

        ResponseEntity<ApiResTemplate<LoginExchangeResponse>> response = new JudgeAuthController(authService)
                .judgeLogin(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().data()).isEqualTo(expected);
        verify(authService).loginAsJudge(request);
    }

    @Test
    void redirectsJudgeLoginResultToFrontendWithOneTimeCode() {
        AuthService authService = mock(AuthService.class);
        JudgeLoginRequest request = new JudgeLoginRequest("judge-mcm", "MCM1976!");
        when(authService.createJudgeLoginCode(request)).thenReturn("judge-login-code");

        ResponseEntity<Void> response = new JudgeAuthController(authService)
                .judgeLoginRedirect(request);

        assertThat(response.getStatusCode().value()).isEqualTo(302);
        URI location = response.getHeaders().getLocation();
        assertThat(location).isNotNull();
        assertThat(location.getScheme()).isEqualTo("https");
        assertThat(location.getHost()).isEqualTo("seongju-detective.vercel.app");
        assertThat(location.getPath()).isEqualTo("/auth/callback");
        assertThat(URLDecoder.decode(location.getQuery(), StandardCharsets.UTF_8))
                .isEqualTo("code=judge-login-code");
        verify(authService).createJudgeLoginCode(request);
    }
}
