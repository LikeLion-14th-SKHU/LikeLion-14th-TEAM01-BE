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
}
