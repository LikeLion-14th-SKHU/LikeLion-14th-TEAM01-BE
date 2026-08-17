package org.skhuconnect.mcmbe.mypage.repository;

import org.skhuconnect.mcmbe.mypage.entity.DesignerPass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DesignerPassRepository extends JpaRepository<DesignerPass, Long> {

    Optional<DesignerPass> findByMemberId(Long memberId);

    long deleteByMemberId(Long memberId);

    boolean existsByPassCode(String passCode);
}
