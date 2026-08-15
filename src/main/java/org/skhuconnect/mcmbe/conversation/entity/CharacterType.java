package org.skhuconnect.mcmbe.conversation.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import org.skhuconnect.mcmbe.game.entity.CaseType;

@Schema(description = "용의자 캐릭터: CLARA(패턴 장인, SIGNATURE), JOHANNES(사진작가, SIGNATURE), FELIX(제품 설계자, FUNCTION), EMIL(테스트 담당자, FUNCTION)")
public enum CharacterType {

    CLARA(CaseType.SIGNATURE),
    JOHANNES(CaseType.SIGNATURE),
    FELIX(CaseType.FUNCTION),
    EMIL(CaseType.FUNCTION);

    private final CaseType caseType;

    CharacterType(CaseType caseType) {
        this.caseType = caseType;
    }

    public boolean belongsTo(CaseType caseType) {
        return this.caseType == caseType;
    }
}
