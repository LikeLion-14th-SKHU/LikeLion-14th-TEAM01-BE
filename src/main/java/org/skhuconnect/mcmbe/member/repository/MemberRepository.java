package org.skhuconnect.mcmbe.member.repository;

import org.skhuconnect.mcmbe.member.entity.AuthProvider;
import org.skhuconnect.mcmbe.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByProviderAndProviderId(AuthProvider provider, String providerId);
}
