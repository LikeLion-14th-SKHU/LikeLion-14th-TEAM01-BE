package org.skhuconnect.mcmbe.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.skhuconnect.mcmbe.game.entity.CaseType;

public record CaseSelectionRequest(
        @Schema(
                description = "처음 진행할 사건. SIGNATURE: 시그니처 사건, FUNCTION: 기능 사건",
                allowableValues = {"SIGNATURE", "FUNCTION"},
                example = "SIGNATURE"
        )
        @NotNull(message = "진행할 사건은 필수입니다.")
        CaseType currentCase
) {
}
