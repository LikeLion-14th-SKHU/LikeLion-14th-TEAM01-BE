package org.skhuconnect.mcmbe.mypage.service;

import org.skhuconnect.mcmbe.game.entity.GameProgress;
import org.skhuconnect.mcmbe.game.entity.GameStatus;
import org.skhuconnect.mcmbe.game.repository.GameProgressRepository;
import org.skhuconnect.mcmbe.member.entity.Member;
import org.skhuconnect.mcmbe.mypage.entity.DesignerPass;
import org.skhuconnect.mcmbe.mypage.repository.DesignerPassRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DesignerPassIssuanceService {

    private final GameProgressRepository gameProgressRepository;
    private final DesignerPassRepository designerPassRepository;
    private final DesignerPassGradeSelector designerPassGradeSelector;

    public DesignerPassIssuanceService(
            GameProgressRepository gameProgressRepository,
            DesignerPassRepository designerPassRepository,
            DesignerPassGradeSelector designerPassGradeSelector
    ) {
        this.gameProgressRepository = gameProgressRepository;
        this.designerPassRepository = designerPassRepository;
        this.designerPassGradeSelector = designerPassGradeSelector;
    }

    public DesignerPass issueIfEligible(Member member) {
        GameProgress gameProgress = gameProgressRepository.findByMemberId(member.getId())
                .orElse(null);
        return issueIfEligible(member, gameProgress);
    }

    public DesignerPass issueIfEligible(Member member, GameProgress gameProgress) {
        DesignerPass existingPass = designerPassRepository.findByMemberId(member.getId())
                .orElse(null);
        if (existingPass != null) {
            return existingPass;
        }
        if (!hasCompletedBothCases(gameProgress)) {
            return null;
        }

        return designerPassRepository.save(DesignerPass.issue(
                member,
                generatePassCode(member.getId()),
                LocalDateTime.now(),
                designerPassGradeSelector.select()
        ));
    }

    private boolean hasCompletedBothCases(GameProgress gameProgress) {
        return gameProgress != null
                && gameProgress.isFunctionSucceeded()
                && gameProgress.isSignatureSucceeded()
                && gameProgress.getStatus() == GameStatus.COMPLETED;
    }

    private String generatePassCode(Long memberId) {
        return "MCM-%06d".formatted(memberId);
    }
}
