package org.skhuconnect.mcmbe.auth.token.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "refresh_tokens",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_refresh_tokens_member_id",
                columnNames = "member_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "token_hash", nullable = false, length = 64)
    private String tokenHash;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private RefreshToken(Member member, String tokenHash) {
        this.member = member;
        rotate(tokenHash);
    }

    public static RefreshToken issue(Member member, String tokenHash) {
        return new RefreshToken(member, tokenHash);
    }

    public void rotate(String tokenHash) {
        this.tokenHash = tokenHash;
        this.updatedAt = LocalDateTime.now();
    }
}
