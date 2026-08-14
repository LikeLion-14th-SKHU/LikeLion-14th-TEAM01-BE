package org.skhuconnect.mcmbe.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.skhuconnect.mcmbe.mypage.entity.DesignerPass;

import java.time.LocalDate;

public record DesignerPassResponse(
        @Schema(description = "디자이너 패스 코드", example = "MCM-A1B2C3D4")
        String passCode,

        @Schema(description = "실제 발급일을 기준으로 50년 전으로 표시한 날짜", example = "1976-08-14")
        LocalDate issuedDate
) {

    public static DesignerPassResponse from(DesignerPass designerPass) {
        return new DesignerPassResponse(
                designerPass.getPassCode(),
                designerPass.getIssuedAt().toLocalDate().minusYears(50)
        );
    }
}
