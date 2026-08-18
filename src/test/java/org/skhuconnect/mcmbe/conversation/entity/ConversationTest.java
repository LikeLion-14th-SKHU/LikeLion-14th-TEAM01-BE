package org.skhuconnect.mcmbe.conversation.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ConversationTest {

    @Test
    void assignsDifferentUuidSessionToEachConversation() {
        Conversation first = Conversation.create(null, CharacterType.FELIX);
        Conversation second = Conversation.create(null, CharacterType.EMIL);

        assertThat(first.getAiSessionId()).isNotEqualTo(second.getAiSessionId());
        assertThatCode(() -> UUID.fromString(first.getAiSessionId()))
                .doesNotThrowAnyException();
    }

    @Test
    void keepsTheSameAiSessionIdAfterInitialization() {
        Conversation conversation = Conversation.create(null, CharacterType.FELIX);
        String sessionId = conversation.getAiSessionId();

        conversation.initializeAiConversation("초기 증언", "추천 질문");
        conversation.ensureAiSessionId();

        assertThat(conversation.getAiSessionId()).isEqualTo(sessionId);
        assertThat(conversation.isAiInitialized()).isTrue();
    }
}
