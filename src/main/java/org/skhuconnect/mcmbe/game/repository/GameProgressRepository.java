package org.skhuconnect.mcmbe.game.repository;

import org.skhuconnect.mcmbe.game.entity.GameProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameProgressRepository extends JpaRepository<GameProgress, Long> {

    Optional<GameProgress> findByMemberId(Long memberId);
}
