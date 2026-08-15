package org.skhuconnect.mcmbe.conversation.repository;

import jakarta.persistence.LockModeType;
import org.skhuconnect.mcmbe.conversation.entity.CharacterType;
import org.skhuconnect.mcmbe.conversation.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByGameProgressIdAndCharacterType(Long gameProgressId, CharacterType characterType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select conversation from Conversation conversation where conversation.gameProgress.id = :gameProgressId and conversation.characterType = :characterType")
    Optional<Conversation> findByGameProgressIdAndCharacterTypeForUpdate(
            @Param("gameProgressId") Long gameProgressId,
            @Param("characterType") CharacterType characterType
    );
}
