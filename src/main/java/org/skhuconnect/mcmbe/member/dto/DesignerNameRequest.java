package org.skhuconnect.mcmbe.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DesignerNameRequest(
        @Schema(
                description = "게임에서 사용할 디자이너 닉네임. 카카오 프로필 닉네임과 별도로 최초 1회만 저장됩니다.",
                example = "명탐정 디자이너",
                maxLength = 100
        )
        @NotBlank(message = "디자이너 닉네임은 필수입니다.")
        @Size(max = 100, message = "디자이너 닉네임은 100자 이하여야 합니다.")
        String designerName
) {
}
