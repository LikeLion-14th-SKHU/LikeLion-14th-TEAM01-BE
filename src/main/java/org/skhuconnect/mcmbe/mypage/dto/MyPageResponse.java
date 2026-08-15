package org.skhuconnect.mcmbe.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record MyPageResponse(
        @Schema(description = "게임 시작 전에 설정한 디자이너 닉네임", example = "MCM 디자이너")
        String designerName,

        @Schema(description = "두 사건 완료 시 발급된 디자이너 패스. 기존 완료 데이터는 최초 조회 시 보완 발급", nullable = true)
        DesignerPassResponse designerPass
) {
}
