package org.skhuconnect.mcmbe.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skhuconnect.mcmbe.auth.jwt.AuthenticatedMember;
import org.skhuconnect.mcmbe.common.exception.SuccessCode;
import org.skhuconnect.mcmbe.common.response.ApiResTemplate;
import org.skhuconnect.mcmbe.member.dto.DesignerNameRequest;
import org.skhuconnect.mcmbe.member.dto.DesignerNameResponse;
import org.skhuconnect.mcmbe.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "디자이너 닉네임 API",
        description = "로그인 사용자의 게임용 디자이너 닉네임을 관리하는 API"
)
@RestController
@RequestMapping("/detective")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(
            summary = "디자이너 닉네임 최초 설정",
            description = "JWT로 인증된 사용자의 디자이너 닉네임을 게임 시작 전에 최초 1회 저장합니다. "
                    + "userId는 요청하지 않으며, 카카오 프로필 닉네임과 별도로 관리됩니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "디자이너 닉네임 저장 성공"),
            @ApiResponse(responseCode = "400", description = "닉네임이 비어 있거나 100자를 초과함 (INVALID_INPUT_VALUE)"),
            @ApiResponse(responseCode = "401", description = "Access Token이 없거나 유효하지 않음 (UNAUTHORIZED, INVALID_TOKEN, EXPIRED_TOKEN)"),
            @ApiResponse(responseCode = "404", description = "JWT의 회원 정보를 찾을 수 없음 (MEMBER_NOT_FOUND)"),
            @ApiResponse(responseCode = "409", description = "이미 디자이너 닉네임을 설정함 (DESIGNER_NAME_ALREADY_SET)")
    })
    @PostMapping("/designer-name")
    public ResponseEntity<ApiResTemplate<DesignerNameResponse>> setDesignerName(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember,
            @Valid @RequestBody DesignerNameRequest request
    ) {
        DesignerNameResponse response = memberService.setDesignerName(
                authenticatedMember.memberId(),
                request.designerName()
        );
        return ResponseEntity.status(SuccessCode.CREATED.getHttpStatus())
                .body(ApiResTemplate.success(SuccessCode.CREATED, response));
    }
}
