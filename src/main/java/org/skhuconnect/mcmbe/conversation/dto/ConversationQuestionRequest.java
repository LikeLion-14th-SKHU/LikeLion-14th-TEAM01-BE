package org.skhuconnect.mcmbe.conversation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConversationQuestionRequest(
        @Schema(
                description = "용의자에게 전달할 자유 질문. 성공한 USER 질문 1건당 questionCount가 1 증가합니다.",
                example = "사건 당시 어디에 있었나요?",
                maxLength = 2000
        )
        @NotBlank(message = "질문 내용은 필수입니다.")
        @Size(max = 2000, message = "질문 내용은 2000자 이하여야 합니다.")
        String content
) {
}
