package org.skhuconnect.mcmbe.conversation.service;

import org.skhuconnect.mcmbe.ai.client.SuspectAiClient;
import org.skhuconnect.mcmbe.conversation.dto.ConversationQuestionRequest;
import org.skhuconnect.mcmbe.conversation.dto.ConversationResponse;
import org.skhuconnect.mcmbe.conversation.entity.CharacterType;
import org.springframework.stereotype.Service;

@Service
public class ConversationCommandService {

    private final SuspectAiClient suspectAiClient;
    private final ConversationTransactionService transactionService;

    public ConversationCommandService(
            SuspectAiClient suspectAiClient,
            ConversationTransactionService transactionService
    ) {
        this.suspectAiClient = suspectAiClient;
        this.transactionService = transactionService;
    }

    public ConversationResponse ask(
            Long memberId,
            CharacterType characterType,
            ConversationQuestionRequest request
    ) {
        String question = request.content().trim();
        ConversationContext context = transactionService.prepare(memberId, characterType);
        String answer = suspectAiClient.answer(characterType, context.aiSessionId(), question);
        return transactionService.saveMessages(
                memberId,
                characterType,
                context.conversationId(),
                question,
                answer
        );
    }
}
