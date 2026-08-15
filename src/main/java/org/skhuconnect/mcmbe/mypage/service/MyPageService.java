package org.skhuconnect.mcmbe.mypage.service;

import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.game.entity.GameProgress;
import org.skhuconnect.mcmbe.game.repository.GameProgressRepository;
import org.skhuconnect.mcmbe.member.entity.Member;
import org.skhuconnect.mcmbe.member.repository.MemberRepository;
import org.skhuconnect.mcmbe.mypage.dto.DesignerPassResponse;
import org.skhuconnect.mcmbe.mypage.dto.MyPageResponse;
import org.skhuconnect.mcmbe.mypage.entity.DesignerPass;
import org.skhuconnect.mcmbe.mypage.repository.DesignerPassRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class MyPageService {

    private static final String PASS_PREFIX = "MCM-";
    private static final String PASS_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int PASS_RANDOM_LENGTH = 8;

    private final MemberRepository memberRepository;
    private final GameProgressRepository gameProgressRepository;
    private final DesignerPassRepository designerPassRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public MyPageService(
            MemberRepository memberRepository,
            GameProgressRepository gameProgressRepository,
            DesignerPassRepository designerPassRepository
    ) {
        this.memberRepository = memberRepository;
        this.gameProgressRepository = gameProgressRepository;
        this.designerPassRepository = designerPassRepository;
    }

    @Transactional
    public MyPageResponse getMyPage(Long memberId) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        DesignerPass designerPass = designerPassRepository.findByMemberId(memberId)
                .orElseGet(() -> issueIfEligible(member));

        return new MyPageResponse(
                member.getDesignerName(),
                designerPass == null ? null : DesignerPassResponse.from(designerPass)
        );
    }

    private DesignerPass issueIfEligible(Member member) {
        boolean eligible = gameProgressRepository.findByMemberId(member.getId())
                .map(this::hasCompletedBothGames)
                .orElse(false);
        if (!eligible) {
            return null;
        }

        return designerPassRepository.save(DesignerPass.issue(
                member,
                generateUniquePassCode(),
                LocalDateTime.now()
        ));
    }

    private boolean hasCompletedBothGames(GameProgress gameProgress) {
        return gameProgress.isSignatureSucceeded() && gameProgress.isFunctionSucceeded();
    }

    private String generateUniquePassCode() {
        String passCode;
        do {
            StringBuilder randomPart = new StringBuilder(PASS_RANDOM_LENGTH);
            for (int index = 0; index < PASS_RANDOM_LENGTH; index++) {
                randomPart.append(PASS_CHARACTERS.charAt(
                        secureRandom.nextInt(PASS_CHARACTERS.length())
                ));
            }
            passCode = PASS_PREFIX + randomPart;
        } while (designerPassRepository.existsByPassCode(passCode));
        return passCode;
    }
}
