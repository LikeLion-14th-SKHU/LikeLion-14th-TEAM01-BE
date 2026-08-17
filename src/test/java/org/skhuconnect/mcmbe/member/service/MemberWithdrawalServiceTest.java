package org.skhuconnect.mcmbe.member.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.skhuconnect.mcmbe.auth.token.repository.RefreshTokenRepository;
import org.skhuconnect.mcmbe.auth.token.repository.LoginCodeRepository;
import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.conversation.repository.ConversationMessageRepository;
import org.skhuconnect.mcmbe.conversation.repository.ConversationRepository;
import org.skhuconnect.mcmbe.game.repository.GameProgressRepository;
import org.skhuconnect.mcmbe.member.entity.Member;
import org.skhuconnect.mcmbe.member.repository.MemberRepository;
import org.skhuconnect.mcmbe.mypage.repository.DesignerPassRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MemberWithdrawalServiceTest {

    private MemberRepository memberRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private LoginCodeRepository loginCodeRepository;
    private GameProgressRepository gameProgressRepository;
    private ConversationRepository conversationRepository;
    private ConversationMessageRepository conversationMessageRepository;
    private DesignerPassRepository designerPassRepository;
    private MemberWithdrawalService service;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        loginCodeRepository = mock(LoginCodeRepository.class);
        gameProgressRepository = mock(GameProgressRepository.class);
        conversationRepository = mock(ConversationRepository.class);
        conversationMessageRepository = mock(ConversationMessageRepository.class);
        designerPassRepository = mock(DesignerPassRepository.class);
        service = new MemberWithdrawalService(
                memberRepository,
                refreshTokenRepository,
                loginCodeRepository,
                gameProgressRepository,
                conversationRepository,
                conversationMessageRepository,
                designerPassRepository
        );
    }

    @Test
    void deletesOnlyAuthenticatedMembersDataInForeignKeyOrder() {
        Member member = Member.kakao("provider", null, "회원", null);
        ReflectionTestUtils.setField(member, "id", 7L);
        when(memberRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(member));

        service.withdraw(7L);

        InOrder order = inOrder(
                memberRepository,
                conversationMessageRepository,
                conversationRepository,
                gameProgressRepository,
                designerPassRepository,
                refreshTokenRepository,
                loginCodeRepository
        );
        order.verify(memberRepository).findByIdForUpdate(7L);
        order.verify(conversationMessageRepository).deleteAllByMemberId(7L);
        order.verify(conversationRepository).deleteAllByMemberId(7L);
        order.verify(gameProgressRepository).deleteByMemberId(7L);
        order.verify(designerPassRepository).deleteByMemberId(7L);
        order.verify(refreshTokenRepository).deleteByMemberId(7L);
        order.verify(loginCodeRepository).deleteAllByMemberId(7L);
        order.verify(memberRepository).delete(member);

        verify(gameProgressRepository, never()).deleteByMemberId(8L);
    }

    @Test
    void rejectsWithdrawalWhenMemberDoesNotExist() {
        when(memberRepository.findByIdForUpdate(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.withdraw(7L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

        verify(conversationMessageRepository, never()).deleteAllByMemberId(7L);
        verify(memberRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }
}
