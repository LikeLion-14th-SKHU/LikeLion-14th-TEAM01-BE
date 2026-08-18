package org.skhuconnect.mcmbe.mypage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.skhuconnect.mcmbe.mypage.entity.DesignerPass;
import org.skhuconnect.mcmbe.mypage.entity.DesignerPassGrade;

import java.time.LocalDate;

public record DesignerPassResponse(
        @Schema(description = "회원 PK 기반 디자이너 패스 코드", example = "MCM-000027")
        String passCode,

        @Schema(description = "실제 발급일을 기준으로 50년 전으로 표시한 날짜", example = "1976-08-14")
        LocalDate issuedDate,

        @Schema(
                description = "패스 디자인 색상 식별값. BROWN: Archive Brown (32%), IVORY: Ivory Atelier (32%), NAVY: München Navy (32%), GOLDEN: Golden 1976 (4%). 기존 패스 데이터에는 null일 수 있습니다.",
                allowableValues = {"BROWN", "IVORY", "NAVY", "GOLDEN"},
                example = "NAVY",
                nullable = true
        )
        DesignerPassGrade grade,

        @Schema(description = "사용자에게 표시할 패스 디자인명. Archive Brown, Ivory Atelier, München Navy, Golden 1976 중 하나를 반환합니다. 기존 패스 데이터에는 null일 수 있습니다.", example = "München Navy", nullable = true)
        String displayName
) {

    public static DesignerPassResponse from(DesignerPass designerPass) {
        return new DesignerPassResponse(
                designerPass.getPassCode(),
                designerPass.getIssuedAt().toLocalDate().minusYears(50),
                designerPass.getGrade(),
                designerPass.getGrade() == null ? null : designerPass.getGrade().getDisplayName()
        );
    }
}
