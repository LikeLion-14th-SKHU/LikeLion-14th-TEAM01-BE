package org.skhuconnect.mcmbe.conversation.service;

import org.skhuconnect.mcmbe.ai.client.SuspectAiClient;
import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.conversation.dto.ConversationQuestionRequest;
import org.skhuconnect.mcmbe.conversation.dto.ConversationResponse;
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

import java.util.List;

@Service
public class ConversationCommandService {

    private final GameProgressRepository gameProgressRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final SuspectAiClient suspectAiClient;

    public ConversationCommandService(
            GameProgressRepository gameProgressRepository,
            ConversationRepository conversationRepository,
            ConversationMessageRepository conversationMessageRepository,
            SuspectAiClient suspectAiClient
    ) {
        this.gameProgressRepository = gameProgressRepository;
        this.conversationRepository = conversationRepository;
        this.conversationMessageRepository = conversationMessageRepository;
        this.suspectAiClient = suspectAiClient;
    }

    @Transactional
    public ConversationResponse ask(
            Long memberId,
            CharacterType characterType,
            ConversationQuestionRequest request
    ) {
        GameProgress gameProgress = gameProgressRepository.findByMemberIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GAME_NOT_IN_PROGRESS));

        validateAccess(gameProgress, characterType);

        Conversation conversation = conversationRepository
                .findByGameProgressIdAndCharacterTypeForUpdate(gameProgress.getId(), characterType)
                .orElseGet(() -> conversationRepository.save(
                        Conversation.create(gameProgress, characterType)
                ));

        if (!conversation.canAskQuestion()) {
            throw new BusinessException(ErrorCode.QUESTION_LIMIT_EXCEEDED);
        }

        List<ConversationMessage> existingMessages = conversationMessageRepository
                .findAllByConversationIdOrderBySequenceNumberAsc(conversation.getId());
        String question = request.content().trim();
        String answer = suspectAiClient.answer(characterType, conversation.getAiSessionId(), question);
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
        conversation.recordCompletedQuestion();

        return ConversationResponse.from(
                conversation,
                append(existingMessages, userMessage, characterMessage)
        );
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

    private List<ConversationMessage> append(
            List<ConversationMessage> messages,
            ConversationMessage userMessage,
            ConversationMessage characterMessage
    ) {
        java.util.ArrayList<ConversationMessage> result = new java.util.ArrayList<>(messages);
        result.add(userMessage);
        result.add(characterMessage);
        return result;
    }
}
