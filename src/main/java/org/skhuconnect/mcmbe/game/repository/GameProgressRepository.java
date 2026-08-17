package org.skhuconnect.mcmbe.game.repository;

import jakarta.persistence.LockModeType;
import org.skhuconnect.mcmbe.game.entity.GameProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GameProgressRepository extends JpaRepository<GameProgress, Long> {

    Optional<GameProgress> findByMemberId(Long memberId);

    long deleteByMemberId(Long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select gameProgress from GameProgress gameProgress where gameProgress.member.id = :memberId")
    Optional<GameProgress> findByMemberIdForUpdate(@Param("memberId") Long memberId);
}
