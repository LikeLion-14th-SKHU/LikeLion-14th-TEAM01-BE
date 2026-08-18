package org.skhuconnect.mcmbe.conversation.service;

import org.junit.jupiter.api.Test;
import org.skhuconnect.mcmbe.conversation.dto.ConversationResponse;
import org.skhuconnect.mcmbe.conversation.entity.CharacterType;
import org.skhuconnect.mcmbe.conversation.entity.Conversation;
import org.skhuconnect.mcmbe.conversation.repository.ConversationMessageRepository;
import org.skhuconnect.mcmbe.conversation.repository.ConversationRepository;
import org.skhuconnect.mcmbe.game.entity.CaseType;
import org.skhuconnect.mcmbe.game.entity.DesignDirection;
import org.skhuconnect.mcmbe.game.entity.GameProgress;
import org.skhuconnect.mcmbe.game.repository.GameProgressRepository;
import org.skhuconnect.mcmbe.member.entity.Member;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConversationQueryServiceTest {

    @Test
    void initializesNewConversationOnceAndReturnsAiGeneratedRecommendations() {
        GameProgressRepository gameProgressRepository = mock(GameProgressRepository.class);
        ConversationRepository conversationRepository = mock(ConversationRepository.class);
        ConversationMessageRepository messageRepository = mock(ConversationMessageRepository.class);
        ConversationCommandService commandService = mock(ConversationCommandService.class);
        GameProgress progress = progress();
        Conversation conversation = Conversation.create(progress, CharacterType.FELIX);
        ReflectionTestUtils.setField(conversation, "id", 11L);
        conversation.initializeAiConversation("초기 증언", "추천 질문");

        when(gameProgressRepository.findByMemberId(1L)).thenReturn(Optional.of(progress));
        when(conversationRepository.findByGameProgressIdAndCharacterType(10L, CharacterType.FELIX))
                .thenReturn(Optional.empty(), Optional.of(conversation));
        when(messageRepository.findAllByConversationIdOrderBySequenceNumberAsc(11L)).thenReturn(List.of());

        ConversationResponse response = new ConversationQueryService(
                gameProgressRepository, conversationRepository, messageRepository, commandService
        ).getConversation(1L, CharacterType.FELIX);

        verify(commandService).ensureInitialized(1L, CharacterType.FELIX);
        assertThat(response.initialMessage()).isEqualTo("초기 증언");
        assertThat(response.recommendedQuestions()).containsExactly("추천 질문");
    }

    @Test
    void doesNotInitializeAgainWhenConversationAlreadyInitialized() {
        GameProgressRepository gameProgressRepository = mock(GameProgressRepository.class);
        ConversationRepository conversationRepository = mock(ConversationRepository.class);
        ConversationMessageRepository messageRepository = mock(ConversationMessageRepository.class);
        ConversationCommandService commandService = mock(ConversationCommandService.class);
        GameProgress progress = progress();
        Conversation conversation = Conversation.create(progress, CharacterType.FELIX);
        ReflectionTestUtils.setField(conversation, "id", 11L);
        conversation.initializeAiConversation("초기 증언", "추천 질문");

        when(gameProgressRepository.findByMemberId(1L)).thenReturn(Optional.of(progress));
        when(conversationRepository.findByGameProgressIdAndCharacterType(10L, CharacterType.FELIX))
                .thenReturn(Optional.of(conversation));
        when(messageRepository.findAllByConversationIdOrderBySequenceNumberAsc(11L)).thenReturn(List.of());

        new ConversationQueryService(
                gameProgressRepository, conversationRepository, messageRepository, commandService
        ).getConversation(1L, CharacterType.FELIX);

        verify(commandService, never()).ensureInitialized(1L, CharacterType.FELIX);
    }

    private GameProgress progress() {
        Member member = Member.kakao("provider-id", null, "회원", null);
        ReflectionTestUtils.setField(member, "id", 1L);
        GameProgress progress = GameProgress.selectDesign(member, DesignDirection.TRAVEL);
        ReflectionTestUtils.setField(progress, "id", 10L);
        progress.selectCase(CaseType.FUNCTION);
        return progress;
    }
}
