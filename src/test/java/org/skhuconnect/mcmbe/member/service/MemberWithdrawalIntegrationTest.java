package org.skhuconnect.mcmbe.member.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.skhuconnect.mcmbe.auth.token.entity.RefreshToken;
import org.skhuconnect.mcmbe.auth.token.repository.RefreshTokenRepository;
import org.skhuconnect.mcmbe.conversation.entity.CharacterType;
import org.skhuconnect.mcmbe.conversation.entity.Conversation;
import org.skhuconnect.mcmbe.conversation.entity.ConversationMessage;
import org.skhuconnect.mcmbe.conversation.entity.MessageSenderType;
import org.skhuconnect.mcmbe.conversation.repository.ConversationMessageRepository;
import org.skhuconnect.mcmbe.conversation.repository.ConversationRepository;
import org.skhuconnect.mcmbe.game.entity.DesignDirection;
import org.skhuconnect.mcmbe.game.entity.GameProgress;
import org.skhuconnect.mcmbe.game.repository.GameProgressRepository;
import org.skhuconnect.mcmbe.member.entity.Member;
import org.skhuconnect.mcmbe.member.repository.MemberRepository;
import org.skhuconnect.mcmbe.mypage.entity.DesignerPass;
import org.skhuconnect.mcmbe.mypage.repository.DesignerPassRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(MemberWithdrawalService.class)
class MemberWithdrawalIntegrationTest {

    @Autowired
    private MemberWithdrawalService service;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private GameProgressRepository gameProgressRepository;
    @Autowired
    private ConversationRepository conversationRepository;
    @Autowired
    private ConversationMessageRepository conversationMessageRepository;
    @Autowired
    private DesignerPassRepository designerPassRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void withdrawDeletesAllOwnedDataAndPreservesAnotherMember() {
        Member target = memberRepository.save(Member.kakao("target", null, "탈퇴회원", null));
        Member other = memberRepository.save(Member.kakao("other", null, "유지회원", null));
        GameProgress gameProgress = gameProgressRepository.save(
                GameProgress.selectDesign(target, DesignDirection.TRAVEL)
        );
        Conversation conversation = conversationRepository.save(
                Conversation.create(gameProgress, CharacterType.FELIX)
        );
        ConversationMessage message = conversationMessageRepository.save(
                ConversationMessage.of(conversation, MessageSenderType.USER, 1, "질문")
        );
        DesignerPass designerPass = designerPassRepository.save(
                DesignerPass.issue(target, "TEST-PASS", LocalDateTime.now())
        );
        RefreshToken refreshToken = refreshTokenRepository.save(
                RefreshToken.issue(target, "a".repeat(64))
        );
        entityManager.flush();

        Long targetId = target.getId();
        Long otherId = other.getId();
        Long gameProgressId = gameProgress.getId();
        Long conversationId = conversation.getId();
        Long messageId = message.getId();
        Long designerPassId = designerPass.getId();
        Long refreshTokenId = refreshToken.getId();
        entityManager.clear();

        service.withdraw(targetId);
        entityManager.flush();
        entityManager.clear();

        assertThat(memberRepository.existsById(targetId)).isFalse();
        assertThat(gameProgressRepository.existsById(gameProgressId)).isFalse();
        assertThat(conversationRepository.existsById(conversationId)).isFalse();
        assertThat(conversationMessageRepository.existsById(messageId)).isFalse();
        assertThat(designerPassRepository.existsById(designerPassId)).isFalse();
        assertThat(refreshTokenRepository.existsById(refreshTokenId)).isFalse();
        assertThat(memberRepository.existsById(otherId)).isTrue();
    }
}
