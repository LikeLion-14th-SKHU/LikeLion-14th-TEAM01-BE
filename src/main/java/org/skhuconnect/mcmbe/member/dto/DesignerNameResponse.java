package org.skhuconnect.mcmbe.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record DesignerNameResponse(
        @Schema(description = "저장된 디자이너 닉네임", example = "명탐정 디자이너")
        String designerName
) {
}
