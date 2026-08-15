package org.skhuconnect.mcmbe.game.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.skhuconnect.mcmbe.member.entity.Member;

@Getter
@Entity
@Table(
        name = "game_progresses",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_game_progresses_member_id",
                columnNames = "member_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "design_direction", nullable = false, length = 20)
    private DesignDirection designDirection;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_case", length = 20)
    private CaseType currentCase;

    @Column(name = "signature_succeeded", nullable = false)
    private boolean signatureSucceeded;

    @Column(name = "function_succeeded", nullable = false)
    private boolean functionSucceeded;

    private GameProgress(Member member, DesignDirection designDirection) {
        this.member = member;
        this.status = GameStatus.NOT_STARTED;
        this.designDirection = designDirection;
        this.signatureSucceeded = false;
        this.functionSucceeded = false;
    }

    public static GameProgress selectDesign(Member member, DesignDirection designDirection) {
        return new GameProgress(member, designDirection);
    }

    public void selectCase(CaseType currentCase) {
        this.currentCase = currentCase;
        this.status = GameStatus.IN_PROGRESS;
    }

    public void completeCurrentCase(boolean succeeded) {
        if (!succeeded) {
            this.status = GameStatus.FAILED;
            return;
        }

        if (currentCase == CaseType.FUNCTION) {
            this.functionSucceeded = true;
            this.currentCase = null;
            this.status = GameStatus.NOT_STARTED;
            return;
        }

        this.signatureSucceeded = true;
        this.status = GameStatus.COMPLETED;
    }
}
