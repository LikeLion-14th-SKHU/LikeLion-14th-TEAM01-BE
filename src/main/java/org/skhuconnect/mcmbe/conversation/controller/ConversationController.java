package org.skhuconnect.mcmbe.conversation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.skhuconnect.mcmbe.auth.jwt.AuthenticatedMember;
import org.skhuconnect.mcmbe.common.exception.SuccessCode;
import org.skhuconnect.mcmbe.common.response.ApiResTemplate;
import org.skhuconnect.mcmbe.conversation.dto.ConversationResponse;
import org.skhuconnect.mcmbe.conversation.entity.CharacterType;
import org.skhuconnect.mcmbe.conversation.service.ConversationQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "용의자 대화 API",
        description = "현재 사건에 속한 캐릭터의 저장된 대화 상태와 메시지 내역 조회 API"
)
@RestController
@RequestMapping("/detective/conversations")
public class ConversationController {

    private final ConversationQueryService conversationQueryService;

    public ConversationController(ConversationQueryService conversationQueryService) {
        this.conversationQueryService = conversationQueryService;
    }

    @Operation(
            summary = "캐릭터별 대화 내역 조회",
            description = """
                    현재 게임이 IN_PROGRESS인 로그인 사용자만 호출할 수 있습니다.

                    사건별 조회 가능한 CharacterType:
                    - SIGNATURE: CLARA(패턴 장인), JOHANNES(사진작가)
                    - FUNCTION: FELIX(제품 설계자), EMIL(테스트 담당자)

                    ConversationStatus:
                    - NOT_STARTED: 아직 질문하지 않은 상태
                    - IN_PROGRESS: 1~2회 질문과 답변이 저장된 상태
                    - COMPLETED: 3회 질문과 답변 저장이 완료된 상태

                    캐릭터별 최대 질문 수는 3회입니다. questionCount는 완료한 사용자 질문 수이며,
                    remainingQuestionCount는 maxQuestionCount(3)에서 questionCount를 뺀 값입니다.
                    messages는 USER(사용자 질문), CHARACTER(캐릭터 답변)를 sequenceNumber 오름차순으로 반환합니다.
                    현재 사건에 속하지 않은 캐릭터는 조회할 수 없습니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "대화 상태 및 저장된 메시지 내역 조회 성공. 대화 전이면 NOT_STARTED와 빈 messages 반환"),
            @ApiResponse(responseCode = "400", description = "CharacterType에 없는 경로 값 (INVALID_INPUT_VALUE)"),
            @ApiResponse(responseCode = "401", description = "Access Token이 없거나 유효하지 않음 (UNAUTHORIZED, INVALID_TOKEN, EXPIRED_TOKEN)"),
            @ApiResponse(responseCode = "403", description = "현재 사건에 속하지 않은 캐릭터 (CHARACTER_NOT_AVAILABLE_FOR_CURRENT_CASE)"),
            @ApiResponse(responseCode = "409", description = "게임 또는 사건이 진행 중이 아님 (GAME_NOT_IN_PROGRESS)")
    })
    @GetMapping("/{characterType}")
    public ResponseEntity<ApiResTemplate<ConversationResponse>> getConversation(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember,
            @Parameter(
                    description = "조회할 캐릭터. CLARA: 패턴 장인(SIGNATURE), JOHANNES: 사진작가(SIGNATURE), "
                            + "FELIX: 제품 설계자(FUNCTION), EMIL: 테스트 담당자(FUNCTION)",
                    schema = @Schema(
                            allowableValues = {"CLARA", "JOHANNES", "FELIX", "EMIL"},
                            example = "CLARA"
                    )
            )
            @PathVariable CharacterType characterType
    ) {
        return ResponseEntity.ok(ApiResTemplate.success(
                SuccessCode.OK,
                conversationQueryService.getConversation(
                        authenticatedMember.memberId(),
                        characterType
                )
        ));
    }
}
