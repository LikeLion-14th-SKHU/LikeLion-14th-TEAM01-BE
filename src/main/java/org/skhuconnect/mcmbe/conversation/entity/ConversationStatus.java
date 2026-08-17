package org.skhuconnect.mcmbe.conversation.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대화 상태: NOT_STARTED(질문 전), IN_PROGRESS(1~2회 질문 완료), COMPLETED(3회 질문 완료 또는 조기 종료)")
public enum ConversationStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED
}
