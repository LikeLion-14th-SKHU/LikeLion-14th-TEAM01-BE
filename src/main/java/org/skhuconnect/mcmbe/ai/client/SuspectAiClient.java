package org.skhuconnect.mcmbe.ai.client;

import org.skhuconnect.mcmbe.conversation.entity.CharacterType;

public interface SuspectAiClient {

    SuspectAiInitialization initialize(CharacterType characterType, String aiSessionId);

    SuspectAiAnswer answer(
            CharacterType characterType,
            String aiSessionId,
            String question
    );
}
