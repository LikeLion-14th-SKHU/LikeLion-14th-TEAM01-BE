package org.skhuconnect.mcmbe.mypage.service;

import org.skhuconnect.mcmbe.game.entity.GameProgress;
import org.skhuconnect.mcmbe.game.entity.GameStatus;
import org.skhuconnect.mcmbe.game.repository.GameProgressRepository;
import org.skhuconnect.mcmbe.member.entity.Member;
import org.skhuconnect.mcmbe.mypage.entity.DesignerPass;
import org.skhuconnect.mcmbe.mypage.repository.DesignerPassRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class DesignerPassIssuanceService {

    private static final String PASS_PREFIX = "MCM-";
    private static final String PASS_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int PASS_RANDOM_LENGTH = 8;

    private final GameProgressRepository gameProgressRepository;
    private final DesignerPassRepository designerPassRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public DesignerPassIssuanceService(
            GameProgressRepository gameProgressRepository,
            DesignerPassRepository designerPassRepository
    ) {
        this.gameProgressRepository = gameProgressRepository;
        this.designerPassRepository = designerPassRepository;
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
                generateUniquePassCode(),
                LocalDateTime.now()
        ));
    }

    private boolean hasCompletedBothCases(GameProgress gameProgress) {
        return gameProgress != null
                && gameProgress.isFunctionSucceeded()
                && gameProgress.isSignatureSucceeded()
                && gameProgress.getStatus() == GameStatus.COMPLETED;
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
