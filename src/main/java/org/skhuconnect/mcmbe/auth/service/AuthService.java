package org.skhuconnect.mcmbe.auth.service;

import org.skhuconnect.mcmbe.auth.dto.KakaoLoginResponse;
import org.skhuconnect.mcmbe.auth.dto.TokenResponse;
import org.skhuconnect.mcmbe.auth.jwt.JwtTokenProvider;
import org.skhuconnect.mcmbe.auth.kakao.KakaoApiClient;
import org.skhuconnect.mcmbe.auth.kakao.KakaoUserResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final KakaoApiClient kakaoApiClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthTransactionService authTransactionService;

    public AuthService(
            KakaoApiClient kakaoApiClient,
            JwtTokenProvider jwtTokenProvider,
            AuthTransactionService authTransactionService
    ) {
        this.kakaoApiClient = kakaoApiClient;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authTransactionService = authTransactionService;
    }

    public String getKakaoAuthorizationUrl() {
        return kakaoApiClient.buildAuthorizationUrl(jwtTokenProvider.createOAuthState());
    }

    public KakaoLoginResponse loginWithKakao(String authorizationCode, String state) {
        jwtTokenProvider.validateOAuthState(state);
        KakaoUserResponse kakaoUser = kakaoApiClient.getUser(authorizationCode);
        return authTransactionService.completeKakaoLogin(kakaoUser);
    }

    public TokenResponse refresh(String refreshToken) {
        return authTransactionService.rotate(refreshToken);
    }

    public void logout(Long memberId) {
        authTransactionService.logout(memberId);
    }
}
