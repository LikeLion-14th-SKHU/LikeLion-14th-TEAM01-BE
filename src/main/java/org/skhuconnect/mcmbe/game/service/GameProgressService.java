package org.skhuconnect.mcmbe.game.service;

import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.game.dto.CaseSelectionRequest;
import org.skhuconnect.mcmbe.game.dto.DesignDirectionRequest;
import org.skhuconnect.mcmbe.game.dto.GameProgressResponse;
import org.skhuconnect.mcmbe.game.entity.GameProgress;
import org.skhuconnect.mcmbe.game.entity.GameStatus;
import org.skhuconnect.mcmbe.game.repository.GameProgressRepository;
import org.skhuconnect.mcmbe.member.entity.Member;
import org.skhuconnect.mcmbe.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameProgressService {

    private final MemberRepository memberRepository;
    private final GameProgressRepository gameProgressRepository;

    public GameProgressService(
            MemberRepository memberRepository,
            GameProgressRepository gameProgressRepository
    ) {
        this.memberRepository = memberRepository;
        this.gameProgressRepository = gameProgressRepository;
    }

    @Transactional
    public GameProgressResponse selectDesignDirection(
            Long memberId,
            DesignDirectionRequest request
    ) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getDesignerName() == null) {
            throw new BusinessException(ErrorCode.DESIGNER_NAME_REQUIRED);
        }

        if (gameProgressRepository.findByMemberId(memberId).isPresent()) {
            throw new BusinessException(ErrorCode.DESIGN_DIRECTION_ALREADY_SELECTED);
        }

        GameProgress gameProgress = gameProgressRepository.save(GameProgress.selectDesign(
                member,
                request.designDirection()
        ));
        return GameProgressResponse.from(gameProgress);
    }

    @Transactional
    public GameProgressResponse selectCase(Long memberId, CaseSelectionRequest request) {
        memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        GameProgress gameProgress = gameProgressRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DESIGN_DIRECTION_REQUIRED));

        if (gameProgress.getStatus() != GameStatus.NOT_STARTED
                || gameProgress.getCurrentCase() != null) {
            throw new BusinessException(ErrorCode.GAME_ALREADY_STARTED);
        }

        gameProgress.selectCase(request.currentCase());
        return GameProgressResponse.from(gameProgress);
    }

    @Transactional(readOnly = true)
    public GameProgressResponse getProgress(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        return gameProgressRepository.findByMemberId(memberId)
                .map(GameProgressResponse::from)
                .orElseGet(GameProgressResponse::notStarted);
    }
}
