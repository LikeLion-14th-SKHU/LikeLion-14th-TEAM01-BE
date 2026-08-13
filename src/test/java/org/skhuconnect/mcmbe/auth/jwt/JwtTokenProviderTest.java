package org.skhuconnect.mcmbe.auth.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skhuconnect.mcmbe.auth.config.AuthProperties;
import org.skhuconnect.mcmbe.auth.dto.TokenResponse;
import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.member.entity.Member;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private Member member;

    @BeforeEach
    void setUp() {
        AuthProperties properties = new AuthProperties(
                new AuthProperties.Kakao("client", "", "http://localhost/callback"),
                new AuthProperties.Jwt(
                        "MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
                        1_800_000,
                        1_209_600_000
                )
        );
        jwtTokenProvider = new JwtTokenProvider(properties);
        member = Member.kakao("12345", "member@example.com", "회원", null);
        ReflectionTestUtils.setField(member, "id", 1L);
    }

    @Test
    void issuesAndParsesAccessAndRefreshTokens() {
        TokenResponse tokens = jwtTokenProvider.issueTokens(member);

        AuthenticatedMember authenticatedMember =
                jwtTokenProvider.parseAccessToken(tokens.accessToken());

        assertThat(authenticatedMember.memberId()).isEqualTo(1L);
        assertThat(authenticatedMember.role()).isEqualTo(member.getRole());
        assertThat(jwtTokenProvider.parseRefreshToken(tokens.refreshToken())).isEqualTo(1L);
    }

    @Test
    void issuedRefreshTokensAreUnique() {
        TokenResponse first = jwtTokenProvider.issueTokens(member);
        TokenResponse second = jwtTokenProvider.issueTokens(member);

        assertThat(second.refreshToken()).isNotEqualTo(first.refreshToken());
    }
    @Test
    void refreshTokenCannotBeUsedAsAccessToken() {
        TokenResponse tokens = jwtTokenProvider.issueTokens(member);

        assertThatThrownBy(() -> jwtTokenProvider.parseAccessToken(tokens.refreshToken()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    void createsAndValidatesStatelessOAuthState() {
        String state = jwtTokenProvider.createOAuthState();

        jwtTokenProvider.validateOAuthState(state);
        assertThatThrownBy(() -> jwtTokenProvider.parseAccessToken(state))
                .isInstanceOf(BusinessException.class);
    }
}
