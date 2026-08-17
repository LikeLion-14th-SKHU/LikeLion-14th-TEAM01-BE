package org.skhuconnect.mcmbe.auth.token.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.skhuconnect.mcmbe.member.entity.Member;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "login_codes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_login_codes_code_hash",
                columnNames = "code_hash"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "code_hash", nullable = false, length = 64)
    private String codeHash;

    @Column(name = "new_member", nullable = false)
    private boolean newMember;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    private LoginCode(Member member, String codeHash, boolean newMember, LocalDateTime expiresAt) {
        this.member = member;
        this.codeHash = codeHash;
        this.newMember = newMember;
        this.expiresAt = expiresAt;
    }

    public static LoginCode issue(
            Member member,
            String codeHash,
            boolean newMember,
            LocalDateTime expiresAt
    ) {
        return new LoginCode(member, codeHash, newMember, expiresAt);
    }

    public boolean isUsable(LocalDateTime now) {
        return usedAt == null && now.isBefore(expiresAt);
    }

    public void consume(LocalDateTime now) {
        this.usedAt = now;
    }
}
