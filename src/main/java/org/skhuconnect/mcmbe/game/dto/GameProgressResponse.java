package org.skhuconnect.mcmbe.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.skhuconnect.mcmbe.game.entity.CaseType;
import org.skhuconnect.mcmbe.game.entity.DesignDirection;
import org.skhuconnect.mcmbe.game.entity.GameProgress;
import org.skhuconnect.mcmbe.game.entity.GameStatus;

public record GameProgressResponse(
        @Schema(
                description = "전체 게임 상태. NOT_STARTED: 사건 선택 전, IN_PROGRESS: 사건 진행 중, "
                        + "FAILED: 최종 추리 실패로 종료, COMPLETED: 두 사건 모두 성공하여 완료",
                allowableValues = {"NOT_STARTED", "IN_PROGRESS", "FAILED", "COMPLETED"},
                example = "NOT_STARTED"
        )
        GameStatus status,

        @Schema(
                description = "저장된 디자인 방향. 미선택 시 null. TRAVEL: 이동이 많은 여행자를 위한 제품, "
                        + "HANDS_FREE: 두 손을 자유롭게 사용할 수 있는 제품, "
                        + "DAILY_TRAVEL: 일상과 여행에서 모두 사용하는 제품",
                allowableValues = {"TRAVEL", "HANDS_FREE", "DAILY_TRAVEL"},
                example = "TRAVEL",
                nullable = true
        )
        DesignDirection designDirection,

        @Schema(
                description = "선택된 사건. 최초 선택 전과 FUNCTION 성공 후 다음 사건 선택 전에는 null이며, "
                        + "실패 또는 전체 완료 후에는 마지막 사건을 유지합니다.",
                allowableValues = {"SIGNATURE", "FUNCTION"},
                example = "SIGNATURE",
                nullable = true
        )
        CaseType currentCase,

        @Schema(description = "SIGNATURE 사건 성공 여부", example = "false")
        boolean signatureSucceeded,

        @Schema(description = "FUNCTION 사건 성공 여부", example = "false")
        boolean functionSucceeded
) {

    public static GameProgressResponse notStarted() {
        return new GameProgressResponse(GameStatus.NOT_STARTED, null, null, false, false);
    }

    public static GameProgressResponse from(GameProgress gameProgress) {
        return new GameProgressResponse(
                gameProgress.getStatus(),
                gameProgress.getDesignDirection(),
                gameProgress.getCurrentCase(),
                gameProgress.isSignatureSucceeded(),
                gameProgress.isFunctionSucceeded()
        );
    }
}
