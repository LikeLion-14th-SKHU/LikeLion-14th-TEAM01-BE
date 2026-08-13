package org.skhuconnect.mcmbe.conversation.repository;

import org.skhuconnect.mcmbe.conversation.entity.CharacterType;
import org.skhuconnect.mcmbe.conversation.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByGameProgressIdAndCharacterType(
            Long gameProgressId,
            CharacterType characterType
    );
}
