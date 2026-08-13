package org.skhuconnect.mcmbe.conversation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "conversation_messages",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_conversation_messages_conversation_sequence",
                columnNames = {"conversation_id", "sequence_number"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConversationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false, length = 20)
    private MessageSenderType senderType;

    @Column(name = "sequence_number", nullable = false)
    private int sequenceNumber;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private ConversationMessage(
            Conversation conversation,
            MessageSenderType senderType,
            int sequenceNumber,
            String content
    ) {
        this.conversation = conversation;
        this.senderType = senderType;
        this.sequenceNumber = sequenceNumber;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public static ConversationMessage of(
            Conversation conversation,
            MessageSenderType senderType,
            int sequenceNumber,
            String content
    ) {
        return new ConversationMessage(conversation, senderType, sequenceNumber, content);
    }
}
