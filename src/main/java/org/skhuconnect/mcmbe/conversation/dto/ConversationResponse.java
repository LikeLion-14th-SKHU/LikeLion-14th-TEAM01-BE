package org.skhuconnect.mcmbe.conversation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.skhuconnect.mcmbe.conversation.entity.CharacterType;
import org.skhuconnect.mcmbe.conversation.entity.Conversation;
import org.skhuconnect.mcmbe.conversation.entity.ConversationMessage;
import org.skhuconnect.mcmbe.conversation.entity.ConversationStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ConversationResponse(
        @Schema(
                description = "조회한 캐릭터. CLARA: 패턴 장인(SIGNATURE), JOHANNES: 사진작가(SIGNATURE), "
                        + "FELIX: 제품 설계자(FUNCTION), EMIL: 테스트 담당자(FUNCTION)",
                allowableValues = {"CLARA", "JOHANNES", "FELIX", "EMIL"},
                example = "CLARA"
        )
        CharacterType characterType,

        @Schema(
                description = "캐릭터별 대화 상태. NOT_STARTED: 질문 전, IN_PROGRESS: 1~2회 질문 완료, "
                        + "COMPLETED: 3회 질문 완료 또는 조기 종료",
                allowableValues = {"NOT_STARTED", "IN_PROGRESS", "COMPLETED"},
                example = "NOT_STARTED"
        )
        ConversationStatus status,

        @Schema(description = "해당 캐릭터에게 완료한 사용자 질문 수", example = "0")
        int questionCount,

        @Schema(description = "캐릭터별 허용되는 최대 질문 수. 항상 3입니다.", example = "3")
        int maxQuestionCount,

        @Schema(description = "남은 질문 수. maxQuestionCount - questionCount 값입니다.", example = "3")
        int remainingQuestionCount,

        @Schema(description = "대화 시작 시각. 질문 전에는 null입니다.", nullable = true)
        LocalDateTime startedAt,

        @Schema(description = "3회 질문 완료 또는 조기 종료 시각. 완료 전에는 null입니다.", nullable = true)
        LocalDateTime completedAt,

        @Schema(description = "sequenceNumber 오름차순으로 정렬된 사용자 질문과 캐릭터 답변")
        List<ConversationMessageResponse> messages
) {

    public static ConversationResponse notStarted(CharacterType characterType) {
        return new ConversationResponse(
                characterType,
                ConversationStatus.NOT_STARTED,
                0,
                Conversation.MAX_QUESTION_COUNT,
                Conversation.MAX_QUESTION_COUNT,
                null,
                null,
                List.of()
        );
    }

    public static ConversationResponse from(
            Conversation conversation,
            List<ConversationMessage> messages
    ) {
        return new ConversationResponse(
                conversation.getCharacterType(),
                conversation.getStatus(),
                conversation.getQuestionCount(),
                Conversation.MAX_QUESTION_COUNT,
                conversation.getRemainingQuestionCount(),
                conversation.getStartedAt(),
                conversation.getCompletedAt(),
                messages.stream().map(ConversationMessageResponse::from).toList()
        );
    }
}
