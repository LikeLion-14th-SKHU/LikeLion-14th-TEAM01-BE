package org.skhuconnect.mcmbe.auth.service;

import org.skhuconnect.mcmbe.auth.dto.KakaoLoginMemberResponse;
import org.skhuconnect.mcmbe.auth.dto.LoginExchangeResponse;
import org.skhuconnect.mcmbe.auth.dto.TokenResponse;
import org.skhuconnect.mcmbe.auth.jwt.JwtTokenProvider;
import org.skhuconnect.mcmbe.auth.kakao.KakaoUserResponse;
import org.skhuconnect.mcmbe.auth.token.entity.LoginCode;
import org.skhuconnect.mcmbe.auth.token.entity.RefreshToken;
import org.skhuconnect.mcmbe.auth.token.repository.LoginCodeRepository;
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
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class AuthTransactionService {

    private static final long LOGIN_CODE_EXPIRATION_SECONDS = 60;
    private static final String JUDGE_PROVIDER_ID = "judge:test";
    private static final String JUDGE_NICKNAME = "심사위원";

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginCodeRepository loginCodeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthTransactionService(
            MemberRepository memberRepository,
            RefreshTokenRepository refreshTokenRepository,
            LoginCodeRepository loginCodeRepository,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.memberRepository = memberRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.loginCodeRepository = loginCodeRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public KakaoLoginMemberResponse completeKakaoLogin(KakaoUserResponse kakaoUser) {
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

        return new KakaoLoginMemberResponse(member.getId(), newMember);
    }

    @Transactional
    public LoginExchangeResponse completeJudgeLogin() {
        Member member = memberRepository
                .findByProviderAndProviderId(AuthProvider.KAKAO, JUDGE_PROVIDER_ID)
                .orElse(null);
        boolean newMember = member == null;

        if (newMember) {
            member = memberRepository.save(Member.judge(JUDGE_PROVIDER_ID, JUDGE_NICKNAME));
        } else if (member.getDesignerName() == null || member.getDesignerName().isBlank()) {
            member.setDesignerName(JUDGE_NICKNAME);
        }

        TokenResponse tokens = jwtTokenProvider.issueTokens(member);
        saveOrRotate(member, tokens.refreshToken());
        return new LoginExchangeResponse(
                member.getId(),
                newMember,
                member.getNickname(),
                tokens
        );
    }

    @Transactional
    public String issueJudgeLoginCode() {
        Member member = memberRepository
                .findByProviderAndProviderId(AuthProvider.KAKAO, JUDGE_PROVIDER_ID)
                .orElse(null);
        boolean newMember = member == null;

        if (newMember) {
            member = memberRepository.save(Member.judge(JUDGE_PROVIDER_ID, JUDGE_NICKNAME));
        } else if (member.getDesignerName() == null || member.getDesignerName().isBlank()) {
            member.setDesignerName(JUDGE_NICKNAME);
        }

        return issueLoginCode(member.getId(), newMember);
    }

    @Transactional
    public String issueLoginCode(Long memberId, boolean newMember) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        loginCodeRepository.deleteAllByMemberId(memberId);

        String code = generateLoginCode();
        loginCodeRepository.save(LoginCode.issue(
                member,
                hash(code),
                newMember,
                LocalDateTime.now().plusSeconds(LOGIN_CODE_EXPIRATION_SECONDS)
        ));
        return code;
    }

    @Transactional
    public LoginExchangeResponse exchangeLoginCode(String code) {
        LocalDateTime now = LocalDateTime.now();
        LoginCode loginCode = loginCodeRepository.findByCodeHashForUpdate(hash(code))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN_CODE));
        if (!loginCode.isUsable(now)) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN_CODE);
        }

        loginCode.consume(now);
        Member member = loginCode.getMember();
        TokenResponse tokens = jwtTokenProvider.issueTokens(member);
        saveOrRotate(member, tokens.refreshToken());
        return new LoginExchangeResponse(
                member.getId(),
                loginCode.isNewMember(),
                member.getNickname(),
                tokens
        );
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

    @Transactional
    public void logout(Long memberId) {
        refreshTokenRepository.deleteByMemberId(memberId);
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

    private String generateLoginCode() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
