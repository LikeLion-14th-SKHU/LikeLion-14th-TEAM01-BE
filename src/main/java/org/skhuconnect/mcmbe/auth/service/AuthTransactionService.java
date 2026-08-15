package org.skhuconnect.mcmbe.auth.service;

import org.skhuconnect.mcmbe.auth.dto.KakaoLoginResponse;
import org.skhuconnect.mcmbe.auth.dto.TokenResponse;
import org.skhuconnect.mcmbe.auth.jwt.JwtTokenProvider;
import org.skhuconnect.mcmbe.auth.kakao.KakaoUserResponse;
import org.skhuconnect.mcmbe.auth.token.entity.RefreshToken;
import org.skhuconnect.mcmbe.auth.token.repository.RefreshTokenRepository;
import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.member.entity.AuthProvider;
import org.skhuconnect.mcmbe.member.entity.Member;
import org.skhuconnect.mcmbe.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class AuthTransactionService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthTransactionService(
            MemberRepository memberRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.memberRepository = memberRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public KakaoLoginResponse completeKakaoLogin(KakaoUserResponse kakaoUser) {
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
            member.updateProfile(kakaoUser.email(), kakaoUser.nickname(), kakaoUser.profileImageUrl());
        }

        TokenResponse tokens = jwtTokenProvider.issueTokens(member);
        saveOrRotate(member, tokens.refreshToken());
        return new KakaoLoginResponse(member.getId(), newMember, member.getNickname(), tokens);
    }

    @Transactional
    public TokenResponse rotate(String currentRefreshToken) {
        Long memberId = jwtTokenProvider.parseRefreshToken(currentRefreshToken);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
        RefreshToken storedToken = refreshTokenRepository.findByMemberIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (!MessageDigest.isEqual(
                storedToken.getTokenHash().getBytes(StandardCharsets.US_ASCII),
                hash(currentRefreshToken).getBytes(StandardCharsets.US_ASCII)
        )) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        TokenResponse tokens = jwtTokenProvider.issueTokens(member);
        storedToken.rotate(hash(tokens.refreshToken()));
        return tokens;
    }

    private void saveOrRotate(Member member, String refreshToken) {
        String tokenHash = hash(refreshToken);
        RefreshToken storedToken = refreshTokenRepository.findByMemberId(member.getId())
                .orElse(null);
        if (storedToken == null) {
            refreshTokenRepository.save(RefreshToken.issue(member, tokenHash));
            return;
        }
        storedToken.rotate(tokenHash);
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", exception);
        }
    }
}
