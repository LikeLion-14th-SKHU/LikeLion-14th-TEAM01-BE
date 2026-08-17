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
import org.skhuconnect.mcmbe.conversation.dto.ConversationQuestionRequest;
import org.skhuconnect.mcmbe.conversation.dto.ConversationResponse;
import org.skhuconnect.mcmbe.conversation.entity.CharacterType;
import org.skhuconnect.mcmbe.conversation.service.ConversationCommandService;
import org.skhuconnect.mcmbe.conversation.service.ConversationQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final ConversationCommandService conversationCommandService;

    public ConversationController(
            ConversationQueryService conversationQueryService,
            ConversationCommandService conversationCommandService
    ) {
        this.conversationQueryService = conversationQueryService;
        this.conversationCommandService = conversationCommandService;
    }

    @Operation(
            summary = "용의자에게 질문",
            description = """
                    현재 게임이 IN_PROGRESS인 로그인 사용자가 현재 사건의 용의자에게 자유 질문을 보냅니다.
                    SIGNATURE에서는 CLARA, JOHANNES만, FUNCTION에서는 FELIX, EMIL만 질문할 수 있습니다.
                    캐릭터별 최대 질문 수는 3회이며 AI 응답까지 성공한 USER 질문 1건당 questionCount가 1 증가합니다.
                    CHARACTER 답변은 횟수에 포함되지 않지만 USER 질문 다음 순번으로 저장됩니다.
                    1~2회 완료 후 IN_PROGRESS, 3번째 AI 답변 저장 완료 후 COMPLETED가 되며 이후 질문은 차단됩니다.
                    remainingQuestionCount는 maxQuestionCount(3) - questionCount입니다.
                    기존 메시지는 sequenceNumber 순서대로 AI 컨텍스트에 포함됩니다.
                    AI 호출 실패 시 질문 횟수와 메시지는 변경되지 않아 재시도할 수 있습니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "AI 답변과 USER/CHARACTER 메시지 저장 성공"),
            @ApiResponse(responseCode = "400", description = "질문 입력 또는 CharacterType이 올바르지 않음 (INVALID_INPUT_VALUE)"),
            @ApiResponse(responseCode = "401", description = "인증 토큰 오류 (UNAUTHORIZED, INVALID_TOKEN, EXPIRED_TOKEN)"),
            @ApiResponse(responseCode = "403", description = "현재 사건에 속하지 않은 캐릭터 (CHARACTER_NOT_AVAILABLE_FOR_CURRENT_CASE)"),
            @ApiResponse(responseCode = "409", description = "게임 미진행 또는 질문 3회 소진 (GAME_NOT_IN_PROGRESS, QUESTION_LIMIT_EXCEEDED)"),
            @ApiResponse(responseCode = "502", description = "AI 서버 설정 누락·호출 실패·빈 응답. 질문 횟수와 메시지는 변경되지 않음 (AI_SERVICE_UNAVAILABLE)")
    })
    @PostMapping("/{characterType}/messages")
    public ResponseEntity<ApiResTemplate<ConversationResponse>> ask(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember,
            @Parameter(
                    description = "CLARA: 패턴 장인(SIGNATURE), JOHANNES: 사진작가(SIGNATURE), FELIX: 제품 설계자(FUNCTION), EMIL: 테스트 담당자(FUNCTION)",
                    schema = @Schema(allowableValues = {"CLARA", "JOHANNES", "FELIX", "EMIL"}, example = "CLARA")
            )
            @PathVariable CharacterType characterType,
            @Valid @RequestBody ConversationQuestionRequest request
    ) {
        return ResponseEntity.ok(ApiResTemplate.success(
                SuccessCode.OK,
                conversationCommandService.ask(authenticatedMember.memberId(), characterType, request)
        ));
    }

    @Operation(
            summary = "용의자 대화 조기 종료",
            description = """
                    현재 사건의 용의자 대화를 질문 횟수와 관계없이 종료합니다.
                    종료 후 대화 상태는 COMPLETED가 되며 추가 질문은 허용하지 않습니다.
                    질문을 하지 않은 대화도 종료할 수 있고, 현재 질문 수와 기존 메시지는 유지됩니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "대화 조기 종료 성공"),
            @ApiResponse(responseCode = "401", description = "인증 토큰 오류 (UNAUTHORIZED, INVALID_TOKEN, EXPIRED_TOKEN)"),
            @ApiResponse(responseCode = "403", description = "현재 사건에 속하지 않은 캐릭터 (CHARACTER_NOT_AVAILABLE_FOR_CURRENT_CASE)"),
            @ApiResponse(responseCode = "409", description = "게임 또는 사건이 진행 중이 아님 (GAME_NOT_IN_PROGRESS)")
    })
    @PostMapping("/{characterType}/complete")
    public ResponseEntity<ApiResTemplate<ConversationResponse>> completeEarly(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember,
            @PathVariable CharacterType characterType
    ) {
        return ResponseEntity.ok(ApiResTemplate.success(
                SuccessCode.OK,
                conversationCommandService.completeEarly(authenticatedMember.memberId(), characterType)
        ));
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
                    - COMPLETED: 3회 질문과 답변 저장이 완료되었거나 조기 종료된 상태

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
