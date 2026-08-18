package org.skhuconnect.mcmbe.conversation.service;

import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.conversation.dto.ConversationResponse;
import org.skhuconnect.mcmbe.conversation.entity.CharacterType;
import org.skhuconnect.mcmbe.conversation.entity.Conversation;
import org.skhuconnect.mcmbe.conversation.repository.ConversationMessageRepository;
import org.skhuconnect.mcmbe.conversation.repository.ConversationRepository;
import org.skhuconnect.mcmbe.game.entity.GameProgress;
import org.skhuconnect.mcmbe.game.entity.GameStatus;
import org.skhuconnect.mcmbe.game.repository.GameProgressRepository;
import org.springframework.stereotype.Service;

@Service
public class ConversationQueryService {

    private final GameProgressRepository gameProgressRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMessageRepository conversationMessageRepository;
    private final ConversationCommandService conversationCommandService;

    public ConversationQueryService(
            GameProgressRepository gameProgressRepository,
            ConversationRepository conversationRepository,
            ConversationMessageRepository conversationMessageRepository,
            ConversationCommandService conversationCommandService
    ) {
        this.gameProgressRepository = gameProgressRepository;
        this.conversationRepository = conversationRepository;
        this.conversationMessageRepository = conversationMessageRepository;
        this.conversationCommandService = conversationCommandService;
    }

    public ConversationResponse getConversation(Long memberId, CharacterType characterType) {
        GameProgress gameProgress = gameProgressRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GAME_NOT_IN_PROGRESS));

        if (gameProgress.getStatus() != GameStatus.IN_PROGRESS
                || gameProgress.getCurrentCase() == null) {
            throw new BusinessException(ErrorCode.GAME_NOT_IN_PROGRESS);
        }

        if (!characterType.belongsTo(gameProgress.getCurrentCase())) {
            throw new BusinessException(ErrorCode.CHARACTER_NOT_AVAILABLE_FOR_CURRENT_CASE);
        }

        Conversation conversation = conversationRepository
                .findByGameProgressIdAndCharacterType(gameProgress.getId(), characterType)
                .orElse(null);

        if (conversation == null || !conversation.isAiInitialized()) {
            conversationCommandService.ensureInitialized(memberId, characterType);
            conversation = conversationRepository
                    .findByGameProgressIdAndCharacterType(gameProgress.getId(), characterType)
                    .orElseThrow(() -> new BusinessException(ErrorCode.GAME_NOT_IN_PROGRESS));
        }

        return ConversationResponse.from(
                conversation,
                conversationMessageRepository
                        .findAllByConversationIdOrderBySequenceNumberAsc(conversation.getId())
        );
    }
}
