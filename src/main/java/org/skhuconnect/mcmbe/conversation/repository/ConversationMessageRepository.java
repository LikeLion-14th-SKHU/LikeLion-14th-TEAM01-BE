package org.skhuconnect.mcmbe.conversation.repository;

import org.skhuconnect.mcmbe.conversation.entity.ConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

    List<ConversationMessage> findAllByConversationIdOrderBySequenceNumberAsc(Long conversationId);
}
