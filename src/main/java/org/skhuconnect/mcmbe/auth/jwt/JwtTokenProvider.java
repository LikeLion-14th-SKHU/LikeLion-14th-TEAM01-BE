package org.skhuconnect.mcmbe.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.skhuconnect.mcmbe.auth.config.AuthProperties;
import org.skhuconnect.mcmbe.auth.dto.TokenResponse;
import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.member.entity.Member;
import org.skhuconnect.mcmbe.member.entity.MemberRole;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ROLE_CLAIM = "role";
    private static final long OAUTH_STATE_EXPIRATION = 600_000;

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(AuthProperties authProperties) {
        byte[] keyBytes = Decoders.BASE64.decode(authProperties.jwt().secret());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpiration = authProperties.jwt().accessTokenExpiration();
        this.refreshTokenExpiration = authProperties.jwt().refreshTokenExpiration();
    }

    public TokenResponse issueTokens(Member member) {
        String accessToken = createToken(member, TokenType.ACCESS, accessTokenExpiration);
        String refreshToken = createToken(member, TokenType.REFRESH, refreshTokenExpiration);

        return new TokenResponse(
                "Bearer",
                accessToken,
                accessTokenExpiration / 1000,
                refreshToken,
                refreshTokenExpiration / 1000
        );
    }

    public AuthenticatedMember parseAccessToken(String token) {
        Claims claims = parseClaims(token, TokenType.ACCESS);
        return new AuthenticatedMember(
                Long.valueOf(claims.getSubject()),
                MemberRole.valueOf(claims.get(ROLE_CLAIM, String.class))
        );
    }

    public Long parseRefreshToken(String token) {
        return Long.valueOf(parseClaims(token, TokenType.REFRESH).getSubject());
    }

    public String createOAuthState() {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .claim(TOKEN_TYPE_CLAIM, TokenType.OAUTH_STATE.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(OAUTH_STATE_EXPIRATION)))
                .signWith(secretKey)
                .compact();
    }

    public void validateOAuthState(String state) {
        parseClaims(state, TokenType.OAUTH_STATE);
    }

    private String createToken(Member member, TokenType tokenType, long expiration) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(member.getId().toString())
                .claim(TOKEN_TYPE_CLAIM, tokenType.name())
                .claim(ROLE_CLAIM, member.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expiration)))
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token, TokenType requiredType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!requiredType.name().equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
            }
            return claims;
        } catch (ExpiredJwtException exception) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN, exception);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, exception);
        }
    }
}
