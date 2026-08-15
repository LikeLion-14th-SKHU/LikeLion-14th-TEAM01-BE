package org.skhuconnect.mcmbe.ai.client;

import org.skhuconnect.mcmbe.conversation.entity.CharacterType;

public interface SuspectAiClient {

    String answer(
            CharacterType characterType,
            String aiSessionId,
            String question
    );
}
