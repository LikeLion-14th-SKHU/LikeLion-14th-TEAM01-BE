package org.skhuconnect.mcmbe.conversation.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "메시지 작성자: USER(사용자 질문), CHARACTER(캐릭터 답변)")
public enum MessageSenderType {
    USER,
    CHARACTER
}
