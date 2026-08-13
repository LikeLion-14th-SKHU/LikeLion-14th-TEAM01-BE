package org.skhuconnect.mcmbe.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.skhuconnect.mcmbe.game.entity.DesignDirection;

public record DesignDirectionRequest(
        @Schema(
                description = "저장할 디자인 방향. TRAVEL: 이동이 많은 여행자를 위한 제품, "
                        + "HANDS_FREE: 두 손을 자유롭게 사용할 수 있는 제품, "
                        + "DAILY_TRAVEL: 일상과 여행에서 모두 사용하는 제품",
                allowableValues = {"TRAVEL", "HANDS_FREE", "DAILY_TRAVEL"},
                example = "TRAVEL"
        )
        @NotNull(message = "디자인 방향은 필수입니다.")
        DesignDirection designDirection
) {
}
