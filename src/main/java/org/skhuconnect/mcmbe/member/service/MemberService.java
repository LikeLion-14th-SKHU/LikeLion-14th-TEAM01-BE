package org.skhuconnect.mcmbe.member.service;

import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.member.dto.DesignerNameResponse;
import org.skhuconnect.mcmbe.member.entity.Member;
import org.skhuconnect.mcmbe.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public DesignerNameResponse setDesignerName(Long memberId, String designerName) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getDesignerName() != null) {
            throw new BusinessException(ErrorCode.DESIGNER_NAME_ALREADY_SET);
        }

        String normalizedDesignerName = designerName.trim();
        member.setDesignerName(normalizedDesignerName);
        return new DesignerNameResponse(normalizedDesignerName);
    }
}
