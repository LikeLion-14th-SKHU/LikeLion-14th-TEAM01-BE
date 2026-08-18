package org.skhuconnect.mcmbe.mypage.entity;

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

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "designer_passes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_designer_passes_member_id", columnNames = "member_id"),
                @UniqueConstraint(name = "uk_designer_passes_pass_code", columnNames = "pass_code")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DesignerPass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "pass_code", nullable = false, length = 12)
    private String passCode;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DesignerPassGrade grade;

    private DesignerPass(Member member, String passCode, LocalDateTime issuedAt, DesignerPassGrade grade) {
        this.member = member;
        this.passCode = passCode;
        this.issuedAt = issuedAt;
        this.grade = grade;
    }

    public static DesignerPass issue(
            Member member,
            String passCode,
            LocalDateTime issuedAt,
            DesignerPassGrade grade
    ) {
        return new DesignerPass(member, passCode, issuedAt, grade);
    }
}
