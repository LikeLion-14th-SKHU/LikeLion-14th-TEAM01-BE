package org.skhuconnect.mcmbe.conversation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.conversation.dto.ConversationResponse;
import org.skhuconnect.mcmbe.conversation.entity.CharacterType;
import org.skhuconnect.mcmbe.conversation.entity.Conversation;
import org.skhuconnect.mcmbe.conversation.entity.ConversationMessage;
import org.skhuconnect.mcmbe.conversation.entity.ConversationStatus;
import org.skhuconnect.mcmbe.conversation.entity.MessageSenderType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConversationTransactionServiceTest {

    private GameProgressRepository gameProgressRepository;
    private ConversationRepository conversationRepository;
    private ConversationMessageRepository messageRepository;
    private ConversationTransactionService service;
    private GameProgress gameProgress;
    private Conversation conversation;

    @BeforeEach
    void setUp() {
        gameProgressRepository = mock(GameProgressRepository.class);
        conversationRepository = mock(ConversationRepository.class);
        messageRepository = mock(ConversationMessageRepository.class);
        service = new ConversationTransactionService(
                gameProgressRepository,
                conversationRepository,
                messageRepository
        );

        Member member = Member.kakao("provider-id", null, "회원", null);
        ReflectionTestUtils.setField(member, "id", 1L);
        gameProgress = GameProgress.selectDesign(member, DesignDirection.TRAVEL);
        ReflectionTestUtils.setField(gameProgress, "id", 10L);
        gameProgress.selectCase(CaseType.FUNCTION);
        conversation = Conversation.create(gameProgress, CharacterType.FELIX);
        ReflectionTestUtils.setField(conversation, "id", 11L);

        when(gameProgressRepository.findByMemberIdForUpdate(1L))
                .thenReturn(Optional.of(gameProgress));
        when(conversationRepository.findByIdForUpdate(11L))
                .thenReturn(Optional.of(conversation));
    }

    @Test
    void recalculatesSequenceImmediatelyBeforeSaving() {
        ConversationMessage existing = ConversationMessage.of(
                conversation,
                MessageSenderType.CHARACTER,
                8,
                "기존 답변"
        );
        when(messageRepository.findAllByConversationIdOrderBySequenceNumberAsc(11L))
                .thenReturn(List.of(existing));

        ConversationResponse response = service.saveMessages(
                1L,
                CharacterType.FELIX,
                11L,
                "새 질문",
                "새 답변",
                "새 추천 질문"
        );

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ConversationMessage>> captor = ArgumentCaptor.forClass(List.class);
        verify(messageRepository).saveAll(captor.capture());
        assertThat(captor.getValue())
                .extracting(ConversationMessage::getSequenceNumber)
                .containsExactly(9, 10);
        assertThat(response.questionCount()).isEqualTo(1);
        assertThat(response.remainingQuestionCount()).isEqualTo(Conversation.MAX_QUESTION_COUNT - 1);
        assertThat(response.recommendedQuestions()).containsExactly("새 추천 질문");
        assertThat(response.messages()).hasSize(3);
    }

    @Test
    void updatesRecommendedQuestionWithLatestSuccessfulAiAnswer() {
        conversation.initializeAiConversation("초기 증언", "초기 추천 질문");
        String aiSessionId = conversation.getAiSessionId();
        when(messageRepository.findAllByConversationIdOrderBySequenceNumberAsc(11L))
                .thenReturn(List.of());

        ConversationResponse firstResponse = service.saveMessages(
                1L,
                CharacterType.FELIX,
                11L,
                "첫 질문",
                "첫 답변",
                "첫 추천 질문"
        );
        ConversationResponse secondResponse = service.saveMessages(
                1L,
                CharacterType.FELIX,
                11L,
                "두 번째 질문",
                "두 번째 답변",
                "두 번째 추천 질문"
        );

        assertThat(firstResponse.recommendedQuestions()).containsExactly("첫 추천 질문");
        assertThat(secondResponse.recommendedQuestions()).containsExactly("두 번째 추천 질문");
        assertThat(conversation.getRecommendedQuestion()).isEqualTo("두 번째 추천 질문");
        assertThat(conversation.getAiSessionId()).isEqualTo(aiSessionId);
        assertThat(conversation.getQuestionCount()).isEqualTo(2);
        assertThat(conversation.getRemainingQuestionCount()).isEqualTo(1);
    }

    @Test
    void returnsEmptyRecommendationsWhenThirdSuccessfulQuestionCompletesConversation() {
        conversation.initializeAiConversation("초기 증언", "두 번째 추천 질문");
        conversation.recordCompletedQuestion();
        conversation.recordCompletedQuestion();
        when(messageRepository.findAllByConversationIdOrderBySequenceNumberAsc(11L))
                .thenReturn(List.of());

        ConversationResponse response = service.saveMessages(
                1L,
                CharacterType.FELIX,
                11L,
                "세 번째 질문",
                "세 번째 답변",
                "마지막 추천 질문"
        );

        assertThat(response.status()).isEqualTo(ConversationStatus.COMPLETED);
        assertThat(response.questionCount()).isEqualTo(Conversation.MAX_QUESTION_COUNT);
        assertThat(response.remainingQuestionCount()).isZero();
        assertThat(response.recommendedQuestions()).isEmpty();
        assertThat(conversation.getRecommendedQuestion()).isEqualTo("마지막 추천 질문");
        assertThat(response.messages()).hasSize(2);
    }

    @Test
    void rechecksQuestionLimitInSaveTransaction() {
        conversation.recordCompletedQuestion();
        conversation.recordCompletedQuestion();
        conversation.recordCompletedQuestion();

        assertThatThrownBy(() -> service.saveMessages(
                1L,
                CharacterType.FELIX,
                11L,
                "초과 질문",
                "답변",
                "추천 질문"
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.QUESTION_LIMIT_EXCEEDED);

        verify(messageRepository, never()).saveAll(org.mockito.ArgumentMatchers.anyList());
        assertThat(conversation.getQuestionCount()).isEqualTo(Conversation.MAX_QUESTION_COUNT);
    }

    @Test
    void completesConversationEarlyWithoutConsumingRemainingQuestions() {
        when(conversationRepository.findByGameProgressIdAndCharacterTypeForUpdate(
                10L, CharacterType.FELIX
        )).thenReturn(Optional.of(conversation));
        when(messageRepository.findAllByConversationIdOrderBySequenceNumberAsc(11L))
                .thenReturn(List.of());
        conversation.initializeAiConversation("초기 증언", "추천 질문");

        ConversationResponse response = service.completeEarly(1L, CharacterType.FELIX);

        assertThat(response.status()).isEqualTo(org.skhuconnect.mcmbe.conversation.entity.ConversationStatus.COMPLETED);
        assertThat(response.questionCount()).isZero();
        assertThat(response.remainingQuestionCount()).isEqualTo(Conversation.MAX_QUESTION_COUNT);
        assertThat(response.recommendedQuestions()).isEmpty();
        assertThat(conversation.getRecommendedQuestion()).isEqualTo("추천 질문");
        assertThat(response.messages()).isEmpty();
        assertThat(conversation.getCompletedAt()).isNotNull();
    }

    @Test
    void keepsQuestionCountAndBlocksFurtherQuestionsAfterEarlyCompletion() {
        conversation.recordCompletedQuestion();
        conversation.recordCompletedQuestion();
        when(conversationRepository.findByGameProgressIdAndCharacterTypeForUpdate(
                10L, CharacterType.FELIX
        )).thenReturn(Optional.of(conversation));
        when(messageRepository.findAllByConversationIdOrderBySequenceNumberAsc(11L))
                .thenReturn(List.of());

        service.completeEarly(1L, CharacterType.FELIX);
        service.completeEarly(1L, CharacterType.FELIX);

        assertThat(conversation.getQuestionCount()).isEqualTo(2);
        assertThat(conversation.getRemainingQuestionCount()).isEqualTo(1);
        assertThatThrownBy(() -> service.prepare(1L, CharacterType.FELIX))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.QUESTION_LIMIT_EXCEEDED);
    }
}
