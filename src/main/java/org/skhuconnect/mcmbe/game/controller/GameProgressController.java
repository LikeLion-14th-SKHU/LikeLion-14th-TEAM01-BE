package org.skhuconnect.mcmbe.game.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skhuconnect.mcmbe.auth.jwt.AuthenticatedMember;
import org.skhuconnect.mcmbe.common.exception.SuccessCode;
import org.skhuconnect.mcmbe.common.response.ApiResTemplate;
import org.skhuconnect.mcmbe.game.dto.CaseSelectionRequest;
import org.skhuconnect.mcmbe.game.dto.DesignDirectionRequest;
import org.skhuconnect.mcmbe.game.dto.FinalDeductionRequest;
import org.skhuconnect.mcmbe.game.dto.FinalDeductionResponse;
import org.skhuconnect.mcmbe.game.dto.GameProgressResponse;
import org.skhuconnect.mcmbe.game.service.GameProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "게임 진행 상태 API",
        description = "로그인 사용자의 디자인 방향, 사건 선택, 최종 추리와 저장된 진행 상태 조회 API"
)
@RestController
@RequestMapping("/detective/games")
public class GameProgressController {

    private final GameProgressService gameProgressService;

    public GameProgressController(GameProgressService gameProgressService) {
        this.gameProgressService = gameProgressService;
    }

    @Operation(
            summary = "디자인 방향 최초 선택",
            description = """
                디자이너 닉네임 설정 후 디자인 방향을 최초 1회 저장합니다.

                가능한 DesignDirection 값:
                - TRAVEL: 이동이 많은 여행자를 위한 제품
                - HANDS_FREE: 두 손을 자유롭게 사용할 수 있는 제품
                - DAILY_TRAVEL: 일상과 여행에서 모두 사용하는 제품

                저장 직후 status는 NOT_STARTED이고 currentCase는 null이며,
                사건 선택 전 이탈해도 상태 조회로 복구할 수 있습니다.
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "디자인 방향 저장 성공"),
            @ApiResponse(responseCode = "400", description = "디자인 방향이 누락되거나 허용된 Enum 값이 아님 (INVALID_INPUT_VALUE)"),
            @ApiResponse(responseCode = "401", description = "Access Token이 없거나 유효하지 않음 (UNAUTHORIZED, INVALID_TOKEN, EXPIRED_TOKEN)"),
            @ApiResponse(responseCode = "404", description = "JWT의 회원 정보를 찾을 수 없음 (MEMBER_NOT_FOUND)"),
            @ApiResponse(responseCode = "409", description = "디자이너 닉네임 미설정 또는 디자인 방향을 이미 선택함 (DESIGNER_NAME_REQUIRED, DESIGN_DIRECTION_ALREADY_SELECTED)")
    })
    @PostMapping("/design-direction")
    public ResponseEntity<ApiResTemplate<GameProgressResponse>> selectDesignDirection(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember,
            @Valid @RequestBody DesignDirectionRequest request
    ) {
        GameProgressResponse response = gameProgressService.selectDesignDirection(
                authenticatedMember.memberId(),
                request
        );
        return ResponseEntity.status(SuccessCode.CREATED.getHttpStatus())
                .body(ApiResTemplate.success(SuccessCode.CREATED, response));
    }

    @Operation(
            summary = "진행할 사건 선택",
            description = """
                저장된 디자인 방향이 있는 NOT_STARTED 상태에서 진행할 사건을 선택합니다.

                사건은 FUNCTION부터 시작해야 하며, FUNCTION 최종 추리 성공 후에만 SIGNATURE를 선택할 수 있습니다.

                선택 즉시 status가 IN_PROGRESS로 변경되며,
                진행 중이거나 이미 종료된 사건의 재선택은 차단됩니다.
                """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "사건 선택 및 시작 성공"),
            @ApiResponse(responseCode = "400", description = "사건이 누락되거나 허용된 Enum 값이 아님 (INVALID_INPUT_VALUE)"),
            @ApiResponse(responseCode = "401", description = "Access Token이 없거나 유효하지 않음 (UNAUTHORIZED, INVALID_TOKEN, EXPIRED_TOKEN)"),
            @ApiResponse(responseCode = "404", description = "JWT의 회원 정보를 찾을 수 없음 (MEMBER_NOT_FOUND)"),
            @ApiResponse(responseCode = "409", description = "선택 순서 위반, 디자인 방향 미선택 또는 이미 사건 진행 중 (CASE_SELECTION_NOT_ALLOWED, DESIGN_DIRECTION_REQUIRED, GAME_ALREADY_STARTED)")
    })
    @PostMapping("/current-case")
    public ResponseEntity<ApiResTemplate<GameProgressResponse>> selectCase(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember,
            @Valid @RequestBody CaseSelectionRequest request
    ) {
        return ResponseEntity.ok(ApiResTemplate.success(
                SuccessCode.OK,
                gameProgressService.selectCase(authenticatedMember.memberId(), request)
        ));
    }

    @Operation(
            summary = "현재 사건 최종 추리",
            description = """
                    현재 사건에 속한 두 용의자와 각각 3회 대화를 완료한 뒤 범인 한 명을 제출합니다.
                    현재 사건 밖의 용의자는 제출할 수 없고, 성공 또는 실패로 종료된 사건은 다시 추리할 수 없습니다.
                    FUNCTION 성공 시 사건 선택 가능 상태로 돌아가며, 이후 SIGNATURE를 선택할 수 있습니다.
                    오답이면 전체 게임이 FAILED, SIGNATURE 정답이면 전체 게임이 COMPLETED가 되고
                    두 사건 성공 플래그를 확인하여 디자이너 패스를 최초 1회 발급합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "최종 추리 판정 및 진행 상태 반영 성공"),
            @ApiResponse(responseCode = "400", description = "용의자가 누락되거나 허용된 Enum 값이 아님 (INVALID_INPUT_VALUE)"),
            @ApiResponse(responseCode = "401", description = "인증 토큰 오류 (UNAUTHORIZED, INVALID_TOKEN, EXPIRED_TOKEN)"),
            @ApiResponse(responseCode = "403", description = "현재 사건에 속하지 않은 용의자 (CHARACTER_NOT_AVAILABLE_FOR_CURRENT_CASE)"),
            @ApiResponse(responseCode = "409", description = "사건 미진행, 대화 미완료 또는 종료된 사건 재추리 (GAME_NOT_IN_PROGRESS, CONVERSATIONS_NOT_COMPLETED, FINAL_DEDUCTION_ALREADY_COMPLETED)")
    })
    @PostMapping("/final-deduction")
    public ResponseEntity<ApiResTemplate<FinalDeductionResponse>> deduce(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember,
            @Valid @RequestBody FinalDeductionRequest request
    ) {
        return ResponseEntity.ok(ApiResTemplate.success(
                SuccessCode.OK,
                gameProgressService.deduce(authenticatedMember.memberId(), request)
        ));
    }

    @Operation(
            summary = "내 게임 진행 상태 조회",
            description = "저장된 게임 상태를 조회합니다. 디자인 방향 선택 전에는 designDirection과 currentCase가 null입니다. "
                    + "디자인 방향만 선택한 경우 status는 NOT_STARTED, designDirection은 저장값, currentCase는 null입니다. "
                    + "FUNCTION 성공 후에는 다음 사건 선택을 위해 status는 NOT_STARTED, currentCase는 null이 됩니다. "
                    + "실패 또는 전체 완료 후에는 마지막으로 진행한 사건을 currentCase로 반환합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게임 진행 상태 조회 성공"),
            @ApiResponse(responseCode = "401", description = "Access Token이 없거나 유효하지 않음 (UNAUTHORIZED, INVALID_TOKEN, EXPIRED_TOKEN)"),
            @ApiResponse(responseCode = "404", description = "JWT의 회원 정보를 찾을 수 없음 (MEMBER_NOT_FOUND)")
    })
    @GetMapping
    public ResponseEntity<ApiResTemplate<GameProgressResponse>> getProgress(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    ) {
        return ResponseEntity.ok(ApiResTemplate.success(
                SuccessCode.OK,
                gameProgressService.getProgress(authenticatedMember.memberId())
        ));
    }
}
