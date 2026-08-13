package org.skhuconnect.mcmbe.conversation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.skhuconnect.mcmbe.conversation.entity.ConversationMessage;
import org.skhuconnect.mcmbe.conversation.entity.MessageSenderType;

import java.time.LocalDateTime;

public record ConversationMessageResponse(
        @Schema(description = "메시지 작성자. USER: 사용자 질문, CHARACTER: 캐릭터 답변", allowableValues = {"USER", "CHARACTER"}, example = "USER")
        MessageSenderType senderType,

        @Schema(description = "대화 안에서 메시지가 저장된 순서. 1부터 증가합니다.", example = "1")
        int sequenceNumber,

        @Schema(description = "저장된 질문 또는 답변 내용", example = "사건 당시 어디에 있었나요?")
        String content,

        @Schema(description = "메시지 저장 시각", example = "2026-08-13T20:30:00")
        LocalDateTime createdAt
) {

    public static ConversationMessageResponse from(ConversationMessage message) {
        return new ConversationMessageResponse(
                message.getSenderType(),
                message.getSequenceNumber(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
