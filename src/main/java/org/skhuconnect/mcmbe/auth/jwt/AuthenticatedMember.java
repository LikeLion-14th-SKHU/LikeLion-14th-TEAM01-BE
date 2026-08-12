package org.skhuconnect.mcmbe.auth.jwt;

import org.skhuconnect.mcmbe.member.entity.MemberRole;

public record AuthenticatedMember(
        Long memberId,
        MemberRole role
) {
}
