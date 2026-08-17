package org.skhuconnect.mcmbe.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skhuconnect.mcmbe.auth.config.AuthProperties;
import org.skhuconnect.mcmbe.auth.dto.TokenResponse;
import org.skhuconnect.mcmbe.auth.jwt.JwtTokenProvider;
import org.skhuconnect.mcmbe.auth.token.entity.RefreshToken;
import org.skhuconnect.mcmbe.auth.token.entity.LoginCode;
import org.skhuconnect.mcmbe.auth.token.repository.LoginCodeRepository;
import org.skhuconnect.mcmbe.auth.token.repository.RefreshTokenRepository;
import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.member.entity.Member;
import org.skhuconnect.mcmbe.member.repository.MemberRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.ArgumentCaptor;

class AuthTransactionServiceTest {

    private MemberRepository memberRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private LoginCodeRepository loginCodeRepository;
    private JwtTokenProvider jwtTokenProvider;
    private AuthTransactionService service;
    private Member member;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        loginCodeRepository = mock(LoginCodeRepository.class);
        jwtTokenProvider = tokenProvider(1_800_000L, 1_209_600_000L);
        service = new AuthTransactionService(
                memberRepository,
                refreshTokenRepository,
                loginCodeRepository,
                jwtTokenProvider
        );
        member = Member.kakao("provider-id", null, "회원", null);
        ReflectionTestUtils.setField(member, "id", 1L);
    }

    @Test
    void rotatesValidRefreshTokenAndReturnsNewTokenPair() {
        TokenResponse current = jwtTokenProvider.issueTokens(member);
        RefreshToken stored = RefreshToken.issue(member, hash(current.refreshToken()));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(refreshTokenRepository.findByMemberIdForUpdate(1L))
                .thenReturn(Optional.of(stored));

        TokenResponse rotated = service.rotate(current.refreshToken());

        assertThat(rotated.tokenType()).isEqualTo("Bearer");
        assertThat(rotated.accessToken()).isNotBlank().isNotEqualTo(current.accessToken());
        assertThat(rotated.refreshToken()).isNotBlank().isNotEqualTo(current.refreshToken());
        assertThat(rotated.accessTokenExpiresIn()).isPositive();
        assertThat(rotated.refreshTokenExpiresIn()).isPositive();
        assertThat(jwtTokenProvider.parseAccessToken(rotated.accessToken()).memberId()).isEqualTo(1L);
        assertThat(jwtTokenProvider.parseRefreshToken(rotated.refreshToken())).isEqualTo(1L);
        assertThat(stored.getTokenHash()).isEqualTo(hash(rotated.refreshToken()));
    }

    @Test
    void rejectsPreviousRefreshTokenAfterSuccessfulRotation() {
        TokenResponse current = jwtTokenProvider.issueTokens(member);
        RefreshToken stored = RefreshToken.issue(member, hash(current.refreshToken()));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(refreshTokenRepository.findByMemberIdForUpdate(1L))
                .thenReturn(Optional.of(stored));

        service.rotate(current.refreshToken());

        assertThatThrownBy(() -> service.rotate(current.refreshToken()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    void rejectsInvalidRefreshTokenBeforeDatabaseLookup() {
        assertThatThrownBy(() -> service.rotate("not-a-jwt"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_TOKEN);

        verify(memberRepository, never()).findById(org.mockito.ArgumentMatchers.anyLong());
        verify(refreshTokenRepository, never())
                .findByMemberIdForUpdate(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void rejectsExpiredRefreshToken() throws InterruptedException {
        JwtTokenProvider shortLivedProvider = tokenProvider(1_800_000L, 1L);
        AuthTransactionService shortLivedService = new AuthTransactionService(
                memberRepository,
                refreshTokenRepository,
                loginCodeRepository,
                shortLivedProvider
        );
        String expiredRefreshToken = shortLivedProvider.issueTokens(member).refreshToken();
        Thread.sleep(20L);

        assertThatThrownBy(() -> shortLivedService.rotate(expiredRefreshToken))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.EXPIRED_TOKEN);
    }

    @Test
    void logoutDeletesStoredRefreshTokenForAuthenticatedMember() {
        service.logout(1L);

        verify(refreshTokenRepository).deleteByMemberId(1L);
    }

    @Test
    void exchangesLoginCodeOnlyOnce() {
        LoginCode loginCode = LoginCode.issue(
                member,
                hash("login-code"),
                true,
                java.time.LocalDateTime.now().plusMinutes(1)
        );
        when(loginCodeRepository.findByCodeHashForUpdate(hash("login-code")))
                .thenReturn(Optional.of(loginCode));

        org.skhuconnect.mcmbe.auth.dto.LoginExchangeResponse response = service.exchangeLoginCode("login-code");

        assertThat(response.memberId()).isEqualTo(1L);
        assertThat(response.newMember()).isTrue();
        assertThat(response.tokens().accessToken()).isNotBlank();
        assertThat(loginCode.getUsedAt()).isNotNull();
        assertThatThrownBy(() -> service.exchangeLoginCode("login-code"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_LOGIN_CODE);
    }

    @Test
    void rejectsExpiredAndUnknownLoginCodes() {
        LoginCode expired = LoginCode.issue(
                member,
                hash("expired-code"),
                false,
                java.time.LocalDateTime.now().minusSeconds(1)
        );
        when(loginCodeRepository.findByCodeHashForUpdate(hash("expired-code")))
                .thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.exchangeLoginCode("expired-code"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_LOGIN_CODE);
        assertThatThrownBy(() -> service.exchangeLoginCode("unknown-code"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_LOGIN_CODE);
    }

    @Test
    void storesOnlyHashedLoginCode() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        String code = service.issueLoginCode(1L, true);

        ArgumentCaptor<LoginCode> captor = ArgumentCaptor.forClass(LoginCode.class);
        verify(loginCodeRepository).save(captor.capture());
        assertThat(code).isNotBlank();
        assertThat(captor.getValue().getCodeHash())
                .hasSize(64)
                .isEqualTo(hash(code))
                .isNotEqualTo(code);
    }

    private JwtTokenProvider tokenProvider(long accessExpiration, long refreshExpiration) {
        return new JwtTokenProvider(new AuthProperties(
                new AuthProperties.Kakao("client", "", "http://localhost/callback"),
                new AuthProperties.Jwt(
                        "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
                        accessExpiration,
                        refreshExpiration
                )
        ));
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
