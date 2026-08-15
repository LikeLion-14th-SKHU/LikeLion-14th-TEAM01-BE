package org.skhuconnect.mcmbe.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.skhuconnect.mcmbe.game.entity.GameProgress;

public record FinalDeductionResponse(
        @Schema(description = "제출한 최종 추리의 정답 여부", example = "true")
        boolean correct,

        @Schema(description = "최종 추리 반영 후 게임 진행 상태")
        GameProgressResponse progress
) {
    public static FinalDeductionResponse from(boolean correct, GameProgress gameProgress) {
        return new FinalDeductionResponse(correct, GameProgressResponse.from(gameProgress));
    }
}
