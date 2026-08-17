package org.skhuconnect.mcmbe.auth.token.repository;

import jakarta.persistence.LockModeType;
import org.skhuconnect.mcmbe.auth.token.entity.LoginCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LoginCodeRepository extends JpaRepository<LoginCode, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select loginCode from LoginCode loginCode join fetch loginCode.member where loginCode.codeHash = :codeHash")
    Optional<LoginCode> findByCodeHashForUpdate(@Param("codeHash") String codeHash);

    @Modifying
    @Query("delete from LoginCode loginCode where loginCode.member.id = :memberId")
    int deleteAllByMemberId(@Param("memberId") Long memberId);
}
