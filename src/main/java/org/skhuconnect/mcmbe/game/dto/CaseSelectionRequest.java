package org.skhuconnect.mcmbe.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.skhuconnect.mcmbe.game.entity.CaseType;

public record CaseSelectionRequest(
        @Schema(
                description = "진행할 사건. FUNCTION 성공 후에만 SIGNATURE를 선택할 수 있습니다.",
                allowableValues = {"SIGNATURE", "FUNCTION"},
                example = "FUNCTION"
        )
        @NotNull(message = "진행할 사건은 필수입니다.")
        CaseType currentCase
) {
}
