package org.skhuconnect.mcmbe.auth.service;

import org.skhuconnect.mcmbe.auth.dto.KakaoLoginMemberResponse;
import org.skhuconnect.mcmbe.auth.dto.JudgeLoginRequest;
import org.skhuconnect.mcmbe.auth.dto.LoginExchangeResponse;
import org.skhuconnect.mcmbe.auth.dto.TokenResponse;
import org.skhuconnect.mcmbe.auth.jwt.JwtTokenProvider;
import org.skhuconnect.mcmbe.auth.kakao.KakaoApiClient;
import org.skhuconnect.mcmbe.auth.kakao.KakaoUserResponse;
import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String JUDGE_LOGIN_ID = "test";
    private static final String JUDGE_PASSWORD = "test";

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

    public String loginWithKakao(String authorizationCode, String state) {
        jwtTokenProvider.validateOAuthState(state);
        KakaoUserResponse kakaoUser = kakaoApiClient.getUser(authorizationCode);
        KakaoLoginMemberResponse login = authTransactionService.completeKakaoLogin(kakaoUser);
        return authTransactionService.issueLoginCode(login.memberId(), login.newMember());
    }

    public LoginExchangeResponse exchangeLoginCode(String code) {
        return authTransactionService.exchangeLoginCode(code);
    }

    public LoginExchangeResponse loginAsJudge(JudgeLoginRequest request) {
        if (!JUDGE_LOGIN_ID.equals(request.loginId()) || !JUDGE_PASSWORD.equals(request.password())) {
            throw new BusinessException(ErrorCode.INVALID_JUDGE_CREDENTIALS);
        }
        return authTransactionService.completeJudgeLogin();
    }

    public TokenResponse refresh(String refreshToken) {
        return authTransactionService.rotate(refreshToken);
    }

    public void logout(Long memberId) {
        authTransactionService.logout(memberId);
    }
}
