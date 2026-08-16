package org.skhuconnect.mcmbe.game.service;

import org.junit.jupiter.api.Test;
import org.skhuconnect.mcmbe.conversation.entity.CharacterType;
import org.skhuconnect.mcmbe.game.dto.FinalDeductionRequest;
import org.skhuconnect.mcmbe.game.dto.FinalDeductionResponse;
import org.skhuconnect.mcmbe.game.entity.CaseType;
import org.skhuconnect.mcmbe.game.entity.DesignDirection;
import org.skhuconnect.mcmbe.game.entity.GameProgress;
import org.skhuconnect.mcmbe.game.repository.GameProgressRepository;
import org.skhuconnect.mcmbe.member.entity.Member;
import org.skhuconnect.mcmbe.member.repository.MemberRepository;
import org.skhuconnect.mcmbe.mypage.service.DesignerPassIssuanceService;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GameProgressServiceTest {

    @Test
    void allowsFinalDeductionBeforeUsingAllQuestions() {
        MemberRepository memberRepository = mock(MemberRepository.class);
        GameProgressRepository gameProgressRepository = mock(GameProgressRepository.class);
        DesignerPassIssuanceService passService = mock(DesignerPassIssuanceService.class);
        Member member = Member.kakao("provider-id", null, "회원", null);
        ReflectionTestUtils.setField(member, "id", 1L);
        GameProgress progress = GameProgress.selectDesign(member, DesignDirection.TRAVEL);
        ReflectionTestUtils.setField(progress, "id", 10L);
        progress.selectCase(CaseType.FUNCTION);
        when(memberRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(member));
        when(gameProgressRepository.findByMemberIdForUpdate(1L)).thenReturn(Optional.of(progress));
        GameProgressService service = new GameProgressService(
                memberRepository,
                gameProgressRepository,
                passService
        );

        FinalDeductionResponse response = service.deduce(
                1L,
                new FinalDeductionRequest(CharacterType.EMIL)
        );

        assertThat(response.correct()).isTrue();
        assertThat(response.progress().functionSucceeded()).isTrue();
        assertThat(response.progress().currentCase()).isNull();
        verify(passService).issueIfEligible(member, progress);
    }
}
