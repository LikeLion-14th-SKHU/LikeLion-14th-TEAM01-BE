package org.skhuconnect.mcmbe.mypage.service;

import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.member.entity.Member;
import org.skhuconnect.mcmbe.member.repository.MemberRepository;
import org.skhuconnect.mcmbe.mypage.dto.DesignerPassResponse;
import org.skhuconnect.mcmbe.mypage.dto.MyPageResponse;
import org.skhuconnect.mcmbe.mypage.entity.DesignerPass;
import org.skhuconnect.mcmbe.mypage.repository.DesignerPassRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MyPageService {

    private final MemberRepository memberRepository;
    private final DesignerPassRepository designerPassRepository;
    private final DesignerPassIssuanceService designerPassIssuanceService;

    public MyPageService(
            MemberRepository memberRepository,
            DesignerPassRepository designerPassRepository,
            DesignerPassIssuanceService designerPassIssuanceService
    ) {
        this.memberRepository = memberRepository;
        this.designerPassRepository = designerPassRepository;
        this.designerPassIssuanceService = designerPassIssuanceService;
    }

    @Transactional
    public MyPageResponse getMyPage(Long memberId) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        DesignerPass designerPass = designerPassRepository.findByMemberId(memberId)
                .orElseGet(() -> designerPassIssuanceService.issueIfEligible(member));

        return new MyPageResponse(
                member.getDesignerName(),
                designerPass == null ? null : DesignerPassResponse.from(designerPass)
        );
    }
}
