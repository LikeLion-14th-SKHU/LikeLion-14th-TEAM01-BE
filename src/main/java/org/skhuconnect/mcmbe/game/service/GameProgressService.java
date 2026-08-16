package org.skhuconnect.mcmbe.game.service;

import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.conversation.entity.CharacterType;
import org.skhuconnect.mcmbe.game.dto.CaseSelectionRequest;
import org.skhuconnect.mcmbe.game.dto.DesignDirectionRequest;
import org.skhuconnect.mcmbe.game.dto.FinalDeductionRequest;
import org.skhuconnect.mcmbe.game.dto.FinalDeductionResponse;
import org.skhuconnect.mcmbe.game.dto.GameProgressResponse;
import org.skhuconnect.mcmbe.game.entity.CaseType;
import org.skhuconnect.mcmbe.game.entity.GameProgress;
import org.skhuconnect.mcmbe.game.entity.GameStatus;
import org.skhuconnect.mcmbe.game.repository.GameProgressRepository;
import org.skhuconnect.mcmbe.member.entity.Member;
import org.skhuconnect.mcmbe.member.repository.MemberRepository;
import org.skhuconnect.mcmbe.mypage.service.DesignerPassIssuanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameProgressService {

    private final MemberRepository memberRepository;
    private final GameProgressRepository gameProgressRepository;
    private final DesignerPassIssuanceService designerPassIssuanceService;

    public GameProgressService(
            MemberRepository memberRepository,
            GameProgressRepository gameProgressRepository,
            DesignerPassIssuanceService designerPassIssuanceService
    ) {
        this.memberRepository = memberRepository;
        this.gameProgressRepository = gameProgressRepository;
        this.designerPassIssuanceService = designerPassIssuanceService;
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

        GameProgress gameProgress = gameProgressRepository.findByMemberIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DESIGN_DIRECTION_REQUIRED));

        if (gameProgress.getStatus() != GameStatus.NOT_STARTED
                || gameProgress.getCurrentCase() != null) {
            throw new BusinessException(ErrorCode.GAME_ALREADY_STARTED);
        }

        validateCaseOrder(gameProgress, request.currentCase());

        gameProgress.selectCase(request.currentCase());
        return GameProgressResponse.from(gameProgress);
    }

    @Transactional
    public FinalDeductionResponse deduce(Long memberId, FinalDeductionRequest request) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        GameProgress gameProgress = gameProgressRepository.findByMemberIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DESIGN_DIRECTION_REQUIRED));

        if (gameProgress.getStatus() == GameStatus.FAILED
                || gameProgress.getStatus() == GameStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.FINAL_DEDUCTION_ALREADY_COMPLETED);
        }
        if (gameProgress.getStatus() != GameStatus.IN_PROGRESS
                || gameProgress.getCurrentCase() == null) {
            throw new BusinessException(ErrorCode.GAME_NOT_IN_PROGRESS);
        }

        CaseType currentCase = gameProgress.getCurrentCase();
        if (!request.characterType().belongsTo(currentCase)) {
            throw new BusinessException(ErrorCode.CHARACTER_NOT_AVAILABLE_FOR_CURRENT_CASE);
        }

        boolean correct = isCorrectAnswer(currentCase, request.characterType());
        gameProgress.completeCurrentCase(correct);
        designerPassIssuanceService.issueIfEligible(member, gameProgress);
        return FinalDeductionResponse.from(correct, gameProgress);
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

    private void validateCaseOrder(GameProgress gameProgress, CaseType selectedCase) {
        if (!gameProgress.isFunctionSucceeded() && selectedCase != CaseType.FUNCTION) {
            throw new BusinessException(ErrorCode.CASE_SELECTION_NOT_ALLOWED);
        }
        if (gameProgress.isFunctionSucceeded() && selectedCase != CaseType.SIGNATURE) {
            throw new BusinessException(ErrorCode.CASE_SELECTION_NOT_ALLOWED);
        }
    }

    private boolean isCorrectAnswer(CaseType caseType, CharacterType characterType) {
        return switch (caseType) {
            case FUNCTION -> characterType == CharacterType.EMIL;
            case SIGNATURE -> characterType == CharacterType.JOHANNES;
        };
    }
}
