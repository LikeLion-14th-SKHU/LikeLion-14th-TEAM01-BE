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
import org.skhuconnect.mcmbe.game.entity.GameProgress;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "conversations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_conversations_game_character",
                columnNames = {"game_progress_id", "character_type"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Conversation {

    public static final int MAX_QUESTION_COUNT = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_progress_id", nullable = false)
    private GameProgress gameProgress;

    @Enumerated(EnumType.STRING)
    @Column(name = "character_type", nullable = false, length = 20)
    private CharacterType characterType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationStatus status;

    @Column(name = "question_count", nullable = false)
    private int questionCount;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    private Conversation(GameProgress gameProgress, CharacterType characterType) {
        this.gameProgress = gameProgress;
        this.characterType = characterType;
        this.status = ConversationStatus.NOT_STARTED;
        this.questionCount = 0;
    }

    public static Conversation create(GameProgress gameProgress, CharacterType characterType) {
        return new Conversation(gameProgress, characterType);
    }

    public int getRemainingQuestionCount() {
        return MAX_QUESTION_COUNT - questionCount;
    }
}
