package org.skhuconnect.mcmbe.auth.token.repository;

import jakarta.persistence.LockModeType;
import org.skhuconnect.mcmbe.auth.token.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByMemberId(Long memberId);

    long deleteByMemberId(Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select refreshToken from RefreshToken refreshToken where refreshToken.member.id = :memberId")
    Optional<RefreshToken> findByMemberIdForUpdate(@Param("memberId") Long memberId);
}
