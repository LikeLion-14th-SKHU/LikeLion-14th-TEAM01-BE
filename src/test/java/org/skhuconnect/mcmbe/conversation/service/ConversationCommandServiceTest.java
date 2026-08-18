package org.skhuconnect.mcmbe.conversation.service;

import org.junit.jupiter.api.Test;
import org.skhuconnect.mcmbe.ai.client.SuspectAiClient;
import org.skhuconnect.mcmbe.ai.client.SuspectAiInitialization;
import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.conversation.dto.ConversationQuestionRequest;
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
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

class ConversationCommandServiceTest {

    @Test
    void savesQuestionAndAnswerAfterSuccessfulAiCall() {
        SuspectAiClient aiClient = mock(SuspectAiClient.class);
        ConversationTransactionService transactionService = mock(ConversationTransactionService.class);
        ConversationContext context = new ConversationContext(11L, "ai-session-id", true);
        ConversationResponse expected = ConversationResponse.notStarted(CharacterType.FELIX);
        when(transactionService.prepareInitialization(1L, CharacterType.FELIX)).thenReturn(context);
        when(transactionService.prepare(1L, CharacterType.FELIX)).thenReturn(context);
        when(aiClient.answer(CharacterType.FELIX, "ai-session-id", "질문"))
                .thenReturn("답변");
        when(transactionService.saveMessages(
                1L, CharacterType.FELIX, 11L, "질문", "답변"
        )).thenReturn(expected);

        ConversationResponse response = new ConversationCommandService(aiClient, transactionService)
                .ask(1L, CharacterType.FELIX, new ConversationQuestionRequest(" 질문 "));

        assertThat(response).isSameAs(expected);
        verify(transactionService).prepare(1L, CharacterType.FELIX);
        verify(aiClient).answer(CharacterType.FELIX, "ai-session-id", "질문");
        verify(transactionService).saveMessages(
                1L, CharacterType.FELIX, 11L, "질문", "답변"
        );
    }

    @Test
    void doesNotSaveMessagesWhenAiCallFails() {
        SuspectAiClient aiClient = mock(SuspectAiClient.class);
        GameProgressRepository gameProgressRepository = mock(GameProgressRepository.class);
        ConversationRepository conversationRepository = mock(ConversationRepository.class);
        ConversationMessageRepository messageRepository = mock(ConversationMessageRepository.class);
        Member member = Member.kakao("provider-id", null, "회원", null);
        ReflectionTestUtils.setField(member, "id", 1L);
        GameProgress progress = GameProgress.selectDesign(member, DesignDirection.TRAVEL);
        ReflectionTestUtils.setField(progress, "id", 10L);
        progress.selectCase(CaseType.FUNCTION);
        Conversation conversation = Conversation.create(progress, CharacterType.FELIX);
        ReflectionTestUtils.setField(conversation, "id", 11L);
        when(gameProgressRepository.findByMemberIdForUpdate(1L))
                .thenReturn(Optional.of(progress));
        when(conversationRepository.findByGameProgressIdAndCharacterTypeForUpdate(
                10L, CharacterType.FELIX
        )).thenReturn(Optional.of(conversation));
        when(aiClient.answer(CharacterType.FELIX, conversation.getAiSessionId(), "질문"))
                .thenThrow(new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE));
        ConversationTransactionService transactionService = new ConversationTransactionService(
                gameProgressRepository,
                conversationRepository,
                messageRepository
        );

        assertThatThrownBy(() -> new ConversationCommandService(aiClient, transactionService)
                .ask(1L, CharacterType.FELIX, new ConversationQuestionRequest("질문")))
                .isInstanceOf(BusinessException.class);

        assertThat(conversation.getQuestionCount()).isZero();
        verifyNoInteractions(messageRepository);
    }

    @Test
    void keepsAiCallOutsideTransactionalMethod() throws NoSuchMethodException {
        assertThat(ConversationCommandService.class
                .getMethod("ask", Long.class, CharacterType.class, ConversationQuestionRequest.class)
                .getAnnotation(Transactional.class))
                .isNull();
        assertThat(ConversationTransactionService.class
                .getMethod("prepare", Long.class, CharacterType.class)
                .getAnnotation(Transactional.class))
                .isNotNull();
        assertThat(ConversationTransactionService.class
                .getMethod(
                        "saveMessages",
                        Long.class,
                        CharacterType.class,
                        Long.class,
                        String.class,
                        String.class
                )
                .getAnnotation(Transactional.class))
                .isNotNull();
    }

    @Test
    void initializesOnceWithTheConversationSessionBeforeFirstQuestion() {
        SuspectAiClient aiClient = mock(SuspectAiClient.class);
        ConversationTransactionService transactionService = mock(ConversationTransactionService.class);
        ConversationContext uninitialized = new ConversationContext(11L, "same-session-id", false);
        ConversationContext initialized = new ConversationContext(11L, "same-session-id", true);
        when(transactionService.prepareInitialization(1L, CharacterType.FELIX))
                .thenReturn(uninitialized, initialized);
        when(aiClient.initialize(CharacterType.FELIX, "same-session-id"))
                .thenReturn(new SuspectAiInitialization("초기 증언", "추천 질문"));

        ConversationCommandService service = new ConversationCommandService(aiClient, transactionService);
        service.ensureInitialized(1L, CharacterType.FELIX);
        service.ensureInitialized(1L, CharacterType.FELIX);

        verify(aiClient).initialize(CharacterType.FELIX, "same-session-id");
        verify(transactionService).saveInitialization(
                11L, new SuspectAiInitialization("초기 증언", "추천 질문")
        );
    }
}
