package org.skhuconnect.mcmbe.ai.client;

import org.skhuconnect.mcmbe.conversation.entity.CharacterType;

import java.util.List;

public interface SuspectAiClient {

    String answer(
            CharacterType characterType,
            List<AiConversationMessage> messages,
            String question
    );
}
