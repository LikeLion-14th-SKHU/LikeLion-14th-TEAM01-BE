package org.skhuconnect.mcmbe.game.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게임 상태: NOT_STARTED(사건 선택 전), IN_PROGRESS(사건 진행 중), FAILED(최종 추리 실패로 종료), COMPLETED(두 사건 모두 성공하여 완료)")
public enum GameStatus {
    NOT_STARTED,
    IN_PROGRESS,
    FAILED,
    COMPLETED
}
