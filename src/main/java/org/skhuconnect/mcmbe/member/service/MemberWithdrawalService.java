package org.skhuconnect.mcmbe.member.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberWithdrawalService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginCodeRepository loginCodeRepository;
    private final GameProgressRepository gameProgressRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final DesignerPassRepository designerPassRepository;

    public MemberWithdrawalService(
            MemberRepository memberRepository,
            RefreshTokenRepository refreshTokenRepository,
            LoginCodeRepository loginCodeRepository,
            GameProgressRepository gameProgressRepository,
            ConversationRepository conversationRepository,
            ConversationMessageRepository conversationMessageRepository,
            DesignerPassRepository designerPassRepository
    ) {
        this.memberRepository = memberRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.loginCodeRepository = loginCodeRepository;
        this.gameProgressRepository = gameProgressRepository;
        this.conversationRepository = conversationRepository;
        this.conversationMessageRepository = conversationMessageRepository;
        this.designerPassRepository = designerPassRepository;
    }

    @Transactional
    public void withdraw(Long memberId) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        conversationMessageRepository.deleteAllByMemberId(memberId);
        conversationRepository.deleteAllByMemberId(memberId);
        gameProgressRepository.deleteByMemberId(memberId);
        designerPassRepository.deleteByMemberId(memberId);
        refreshTokenRepository.deleteByMemberId(memberId);
        loginCodeRepository.deleteAllByMemberId(memberId);
        memberRepository.delete(member);
    }
}
