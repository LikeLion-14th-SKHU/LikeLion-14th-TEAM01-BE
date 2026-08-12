package org.skhuconnect.mcmbe.auth.dto;

public record KakaoLoginResponse(
        Long memberId,
        boolean newMember,
        String nickname,
        TokenResponse tokens
) {
}
