package org.skhuconnect.mcmbe.auth.dto;

public record LoginExchangeResponse(
        Long memberId,
        boolean newMember,
        String nickname,
        TokenResponse tokens
) {
}
