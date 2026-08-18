package org.skhuconnect.mcmbe.mypage.dto;

import org.junit.jupiter.api.Test;
import org.skhuconnect.mcmbe.member.entity.Member;
import org.skhuconnect.mcmbe.mypage.entity.DesignerPass;
import org.skhuconnect.mcmbe.mypage.entity.DesignerPassGrade;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DesignerPassResponseTest {

    @Test
    void returnsStoredGradeAndDisplayName() {
        Member member = Member.kakao("provider", null, "회원", null);
        DesignerPass pass = DesignerPass.issue(
                member,
                "MCM-000027",
                LocalDateTime.of(2026, 8, 18, 12, 0),
                DesignerPassGrade.NAVY
        );

        DesignerPassResponse response = DesignerPassResponse.from(pass);

        assertThat(response.passCode()).isEqualTo("MCM-000027");
        assertThat(response.issuedDate()).isEqualTo(java.time.LocalDate.of(1976, 8, 18));
        assertThat(response.grade()).isEqualTo(DesignerPassGrade.NAVY);
        assertThat(response.displayName()).isEqualTo("München Navy");
    }
}
