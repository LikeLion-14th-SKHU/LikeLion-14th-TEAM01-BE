package org.skhuconnect.mcmbe.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.skhuconnect.mcmbe.conversation.entity.CharacterType;

public record FinalDeductionRequest(
        @Schema(
                description = "현재 사건의 최종 범인으로 선택할 용의자",
                allowableValues = {"CLARA", "JOHANNES", "FELIX", "EMIL"},
                example = "EMIL"
        )
        @NotNull(message = "최종 추리할 용의자는 필수입니다.")
        CharacterType characterType
) {
}
