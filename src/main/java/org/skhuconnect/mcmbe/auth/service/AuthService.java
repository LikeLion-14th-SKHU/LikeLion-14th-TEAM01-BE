package org.skhuconnect.mcmbe.auth.service;

import org.skhuconnect.mcmbe.auth.dto.KakaoLoginResponse;
import org.skhuconnect.mcmbe.auth.dto.TokenResponse;
import org.skhuconnect.mcmbe.auth.jwt.JwtTokenProvider;
import org.skhuconnect.mcmbe.auth.kakao.KakaoApiClient;
import org.skhuconnect.mcmbe.auth.kakao.KakaoUserResponse;
import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.member.entity.AuthProvider;
import org.skhuconnect.mcmbe.member.entity.Member;
import org.skhuconnect.mcmbe.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final KakaoApiClient kakaoApiClient;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(
            KakaoApiClient kakaoApiClient,
            MemberRepository memberRepository,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.kakaoApiClient = kakaoApiClient;
        this.memberRepository = memberRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String getKakaoAuthorizationUrl() {
        return kakaoApiClient.buildAuthorizationUrl(jwtTokenProvider.createOAuthState());
    }

    @Transactional
    public KakaoLoginResponse loginWithKakao(String authorizationCode, String state) {
        jwtTokenProvider.validateOAuthState(state);
        KakaoUserResponse kakaoUser = kakaoApiClient.getUser(authorizationCode);
        String providerId = kakaoUser.id().toString();

        Member member = memberRepository
                .findByProviderAndProviderId(AuthProvider.KAKAO, providerId)
                .orElse(null);
        boolean newMember = member == null;

        if (newMember) {
            member = memberRepository.save(Member.kakao(
                    providerId,
                    kakaoUser.email(),
                    kakaoUser.nickname(),
                    kakaoUser.profileImageUrl()
            ));
        } else {
            member.updateProfile(
                    kakaoUser.email(),
                    kakaoUser.nickname(),
                    kakaoUser.profileImageUrl()
            );
        }

        return new KakaoLoginResponse(
                member.getId(),
                newMember,
                member.getNickname(),
                jwtTokenProvider.issueTokens(member)
        );
    }

    @Transactional(readOnly = true)
    public TokenResponse refresh(String refreshToken) {
        Long memberId = jwtTokenProvider.parseRefreshToken(refreshToken);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
        return jwtTokenProvider.issueTokens(member);
    }
}
