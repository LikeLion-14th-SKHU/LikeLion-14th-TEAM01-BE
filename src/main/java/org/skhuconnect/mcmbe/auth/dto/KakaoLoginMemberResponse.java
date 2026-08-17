package org.skhuconnect.mcmbe.auth.dto;

public record KakaoLoginMemberResponse(
        Long memberId,
        boolean newMember
) {
}
