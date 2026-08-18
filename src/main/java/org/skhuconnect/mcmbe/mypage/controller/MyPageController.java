package org.skhuconnect.mcmbe.mypage.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.skhuconnect.mcmbe.auth.jwt.AuthenticatedMember;
import org.skhuconnect.mcmbe.common.exception.SuccessCode;
import org.skhuconnect.mcmbe.common.response.ApiResTemplate;
import org.skhuconnect.mcmbe.mypage.dto.MyPageResponse;
import org.skhuconnect.mcmbe.mypage.service.MyPageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "마이페이지 API", description = "디자이너 닉네임과 디자이너 패스를 조회하는 API")
@RestController
@RequestMapping("/detective/mypage")
public class MyPageController {

    private final MyPageService myPageService;

    public MyPageController(MyPageService myPageService) {
        this.myPageService = myPageService;
    }

    @Operation(
            summary = "마이페이지 조회",
            description = """
        저장된 디자이너 닉네임과 게임 완료 시 발급된 디자이너 패스를 조회합니다.
        기존 완료 데이터에 패스가 없으면 최초 조회 시 보완 발급하며, 이후 동일한 패스를 반환합니다.

        enum명 | 디자이너 패스 확률 등급 | 확률 
        - BROWN: Archive Brown (32%)
        - IVORY: Ivory Atelier (32%)
        - NAVY: München Navy (32%)
        - GOLDEN: Golden 1976 (4%)
        """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "마이페이지 조회 성공"),
            @ApiResponse(responseCode = "401", description = "Access Token이 없거나 유효하지 않음 (UNAUTHORIZED, INVALID_TOKEN, EXPIRED_TOKEN)"),
            @ApiResponse(responseCode = "404", description = "JWT의 회원 정보를 찾을 수 없음 (MEMBER_NOT_FOUND)")
    })
    @GetMapping
    public ResponseEntity<ApiResTemplate<MyPageResponse>> getMyPage(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    ) {
        return ResponseEntity.ok(ApiResTemplate.success(
                SuccessCode.OK,
                myPageService.getMyPage(authenticatedMember.memberId())
        ));
    }
}
