package org.skhuconnect.mcmbe.conversation.service;

import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.conversation.dto.ConversationResponse;
import org.skhuconnect.mcmbe.ai.client.SuspectAiInitialization;
import org.skhuconnect.mcmbe.conversation.entity.CharacterType;
import org.skhuconnect.mcmbe.conversation.entity.Conversation;
import org.skhuconnect.mcmbe.conversation.entity.ConversationMessage;
import org.skhuconnect.mcmbe.conversation.entity.MessageSenderType;
import org.skhuconnect.mcmbe.conversation.repository.ConversationMessageRepository;
import org.skhuconnect.mcmbe.conversation.repository.ConversationRepository;
import org.skhuconnect.mcmbe.game.entity.GameProgress;
import org.skhuconnect.mcmbe.game.entity.GameStatus;
import org.skhuconnect.mcmbe.game.repository.GameProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConversationTransactionService {

    private final GameProgressRepository gameProgressRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository conversationMessageRepository;

    public ConversationTransactionService(
            GameProgressRepository gameProgressRepository,
            ConversationRepository conversationRepository,
            ConversationMessageRepository conversationMessageRepository
    ) {
        this.gameProgressRepository = gameProgressRepository;
        this.conversationRepository = conversationRepository;
        this.conversationMessageRepository = conversationMessageRepository;
    }

    @Transactional
    public ConversationContext prepare(Long memberId, CharacterType characterType) {
        GameProgress gameProgress = findGameProgressForUpdate(memberId);
        validateAccess(gameProgress, characterType);

        Conversation conversation = conversationRepository
                .findByGameProgressIdAndCharacterTypeForUpdate(gameProgress.getId(), characterType)
                .orElseGet(() -> conversationRepository.save(
                        Conversation.create(gameProgress, characterType)
                ));
        conversation.ensureAiSessionId();
        validateCanAsk(conversation);
        return new ConversationContext(conversation.getId(), conversation.getAiSessionId(), conversation.isAiInitialized());
    }

    @Transactional
    public ConversationContext prepareInitialization(Long memberId, CharacterType characterType) {
        GameProgress gameProgress = findGameProgressForUpdate(memberId);
        validateAccess(gameProgress, characterType);

        Conversation conversation = conversationRepository
                .findByGameProgressIdAndCharacterTypeForUpdate(gameProgress.getId(), characterType)
                .orElseGet(() -> conversationRepository.save(
                        Conversation.create(gameProgress, characterType)
                ));
        conversation.ensureAiSessionId();
        return new ConversationContext(conversation.getId(), conversation.getAiSessionId(), conversation.isAiInitialized());
    }

    @Transactional
    public void saveInitialization(Long conversationId, SuspectAiInitialization initialization) {
        Conversation conversation = conversationRepository.findByIdForUpdate(conversationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GAME_NOT_IN_PROGRESS));
        if (!conversation.isAiInitialized()) {
            conversation.initializeAiConversation(
                    initialization.initialMessage(),
                    initialization.recommendedQuestion()
            );
        }
    }

    @Transactional
    public ConversationResponse saveMessages(
            Long memberId,
            CharacterType characterType,
            Long conversationId,
            String question,
            String answer,
            String recommendedQuestion
    ) {
        GameProgress gameProgress = findGameProgressForUpdate(memberId);
        validateAccess(gameProgress, characterType);

        Conversation conversation = conversationRepository.findByIdForUpdate(conversationId)
                .filter(found -> found.getGameProgress().getId().equals(gameProgress.getId()))
                .filter(found -> found.getCharacterType() == characterType)
                .orElseThrow(() -> new BusinessException(ErrorCode.GAME_NOT_IN_PROGRESS));
        validateCanAsk(conversation);

        List<ConversationMessage> existingMessages = conversationMessageRepository
                .findAllByConversationIdOrderBySequenceNumberAsc(conversation.getId());
        int nextSequenceNumber = existingMessages.isEmpty()
                ? 1
                : existingMessages.get(existingMessages.size() - 1).getSequenceNumber() + 1;

        ConversationMessage userMessage = ConversationMessage.of(
                conversation,
                MessageSenderType.USER,
                nextSequenceNumber,
                question
        );
        ConversationMessage characterMessage = ConversationMessage.of(
                conversation,
                MessageSenderType.CHARACTER,
                nextSequenceNumber + 1,
                answer
        );
        conversationMessageRepository.saveAll(List.of(userMessage, characterMessage));
        conversation.updateRecommendedQuestion(recommendedQuestion);
        conversation.recordCompletedQuestion();

        List<ConversationMessage> allMessages = new ArrayList<>(existingMessages);
        allMessages.add(userMessage);
        allMessages.add(characterMessage);
        return ConversationResponse.from(conversation, allMessages);
    }

    @Transactional
    public ConversationResponse completeEarly(Long memberId, CharacterType characterType) {
        GameProgress gameProgress = findGameProgressForUpdate(memberId);
        validateAccess(gameProgress, characterType);

        Conversation conversation = conversationRepository
                .findByGameProgressIdAndCharacterTypeForUpdate(gameProgress.getId(), characterType)
                .orElseGet(() -> conversationRepository.save(
                        Conversation.create(gameProgress, characterType)
                ));
        conversation.completeEarly();

        return ConversationResponse.from(
                conversation,
                conversationMessageRepository
                        .findAllByConversationIdOrderBySequenceNumberAsc(conversation.getId())
        );
    }

    private GameProgress findGameProgressForUpdate(Long memberId) {
        return gameProgressRepository.findByMemberIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GAME_NOT_IN_PROGRESS));
    }

    private void validateAccess(GameProgress gameProgress, CharacterType characterType) {
        if (gameProgress.getStatus() != GameStatus.IN_PROGRESS
                || gameProgress.getCurrentCase() == null) {
            throw new BusinessException(ErrorCode.GAME_NOT_IN_PROGRESS);
        }
        if (!characterType.belongsTo(gameProgress.getCurrentCase())) {
            throw new BusinessException(ErrorCode.CHARACTER_NOT_AVAILABLE_FOR_CURRENT_CASE);
        }
    }

    private void validateCanAsk(Conversation conversation) {
        if (!conversation.canAskQuestion()) {
            throw new BusinessException(ErrorCode.QUESTION_LIMIT_EXCEEDED);
        }
    }
}
