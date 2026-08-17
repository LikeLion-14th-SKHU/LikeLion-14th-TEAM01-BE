package org.skhuconnect.mcmbe.conversation.repository;

import org.skhuconnect.mcmbe.conversation.entity.ConversationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

    List<ConversationMessage> findAllByConversationIdOrderBySequenceNumberAsc(Long conversationId);

    @Modifying
    @Query("""
            delete from ConversationMessage message
            where message.conversation.id in (
                select conversation.id
                from Conversation conversation
                where conversation.gameProgress.member.id = :memberId
            )
            """)
    int deleteAllByMemberId(@Param("memberId") Long memberId);
}
