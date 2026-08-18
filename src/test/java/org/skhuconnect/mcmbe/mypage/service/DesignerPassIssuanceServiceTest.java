package org.skhuconnect.mcmbe.mypage.service;

import org.junit.jupiter.api.Test;
import org.skhuconnect.mcmbe.game.entity.CaseType;
import org.skhuconnect.mcmbe.game.entity.DesignDirection;
import org.skhuconnect.mcmbe.game.entity.GameProgress;
import org.skhuconnect.mcmbe.game.repository.GameProgressRepository;
import org.skhuconnect.mcmbe.member.entity.Member;
import org.skhuconnect.mcmbe.mypage.entity.DesignerPass;
import org.skhuconnect.mcmbe.mypage.entity.DesignerPassGrade;
import org.skhuconnect.mcmbe.mypage.repository.DesignerPassRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DesignerPassIssuanceServiceTest {

    @Test
    void issuesMemberPrimaryKeyBasedPassCodeAndStoresSelectedGrade() {
        GameProgressRepository gameProgressRepository = mock(GameProgressRepository.class);
        DesignerPassRepository designerPassRepository = mock(DesignerPassRepository.class);
        DesignerPassIssuanceService service = new DesignerPassIssuanceService(
                gameProgressRepository,
                designerPassRepository,
                new DesignerPassGradeSelector(() -> 96)
        );
        Member member = member(27L);
        when(designerPassRepository.findByMemberId(27L)).thenReturn(Optional.empty());
        when(designerPassRepository.save(any(DesignerPass.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DesignerPass pass = service.issueIfEligible(member, completedProgress(member));

        assertThat(pass.getPassCode()).isEqualTo("MCM-000027");
        assertThat(pass.getGrade()).isEqualTo(DesignerPassGrade.GOLDEN);
        verify(designerPassRepository).save(pass);
    }

    @Test
    void padsSingleDigitMemberIdToSixDigits() {
        GameProgressRepository gameProgressRepository = mock(GameProgressRepository.class);
        DesignerPassRepository designerPassRepository = mock(DesignerPassRepository.class);
        DesignerPassIssuanceService service = new DesignerPassIssuanceService(
                gameProgressRepository,
                designerPassRepository,
                new DesignerPassGradeSelector(() -> 0)
        );
        Member member = member(1L);
        when(designerPassRepository.findByMemberId(1L)).thenReturn(Optional.empty());
        when(designerPassRepository.save(any(DesignerPass.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DesignerPass pass = service.issueIfEligible(member, completedProgress(member));

        assertThat(pass.getPassCode()).isEqualTo("MCM-000001");
    }

    @Test
    void keepsExistingPassCodeAndGradeWithoutRedrawing() {
        GameProgressRepository gameProgressRepository = mock(GameProgressRepository.class);
        DesignerPassRepository designerPassRepository = mock(DesignerPassRepository.class);
        DesignerPassIssuanceService service = new DesignerPassIssuanceService(
                gameProgressRepository,
                designerPassRepository,
                new DesignerPassGradeSelector(() -> 0)
        );
        Member member = member(1L);
        DesignerPass existing = DesignerPass.issue(
                member,
                "MCM-000001",
                java.time.LocalDateTime.now(),
                DesignerPassGrade.NAVY
        );
        when(designerPassRepository.findByMemberId(1L)).thenReturn(Optional.of(existing));

        DesignerPass pass = service.issueIfEligible(member, completedProgress(member));

        assertThat(pass).isSameAs(existing);
        assertThat(pass.getPassCode()).isEqualTo("MCM-000001");
        assertThat(pass.getGrade()).isEqualTo(DesignerPassGrade.NAVY);
    }

    private Member member(Long id) {
        Member member = Member.kakao("provider-" + id, null, "회원", null);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }

    private GameProgress completedProgress(Member member) {
        GameProgress progress = GameProgress.selectDesign(member, DesignDirection.TRAVEL);
        progress.selectCase(CaseType.FUNCTION);
        progress.completeCurrentCase(true);
        progress.selectCase(CaseType.SIGNATURE);
        progress.completeCurrentCase(true);
        return progress;
    }
}
