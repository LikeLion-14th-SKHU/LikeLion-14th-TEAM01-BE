package org.skhuconnect.mcmbe.game.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사건 유형: SIGNATURE(시그니처 사건), FUNCTION(기능 사건)")
public enum CaseType {
    SIGNATURE,
    FUNCTION
}
