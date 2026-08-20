package org.skhuconnect.mcmbe.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.skhuconnect.mcmbe.auth.dto.JudgeLoginRequest;
import org.skhuconnect.mcmbe.auth.dto.LoginExchangeResponse;
import org.skhuconnect.mcmbe.auth.service.AuthService;
import org.skhuconnect.mcmbe.common.exception.SuccessCode;
import org.skhuconnect.mcmbe.common.response.ApiResTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/detective/auth")
@Tag(
        name = "Kakao & JWT API",
        description = "카카오 로그인 및 JWT 인증 관련 API이다."
)
public class JudgeAuthController {

    private static final String FRONTEND_AUTH_CALLBACK_URL =
            "https://seongju-detective.vercel.app/auth/callback";

    private final AuthService authService;

    public JudgeAuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "심사위원 전용 로그인",
            description = "심사위원 계정 아이디와 비밀번호가 일치하면 Access Token과 Refresh Token을 발급합니다."
    )
    @ApiResponse(responseCode = "200", description = "JWT 발급 성공")
    @ApiResponse(responseCode = "401", description = "심사위원 계정 정보 불일치 (INVALID_JUDGE_CREDENTIALS)")
    @PostMapping("/judge-login")
    public ResponseEntity<ApiResTemplate<LoginExchangeResponse>> judgeLogin(
            @Valid @RequestBody JudgeLoginRequest request
    ) {
        return ResponseEntity.ok(ApiResTemplate.success(
                SuccessCode.OK,
                authService.loginAsJudge(request)
        ));
    }

    @Operation(
            summary = "심사위원 전용 로그인 후 프론트 콜백 이동",
            description = "심사위원 계정 정보가 일치하면 일회용 Login Code를 발급하고 기존 프론트엔드 인증 callback으로 redirect합니다."
    )
    @ApiResponse(responseCode = "302", description = "프론트엔드 인증 화면으로 redirect")
    @ApiResponse(responseCode = "401", description = "심사위원 계정 정보 불일치 (INVALID_JUDGE_CREDENTIALS)")
    @PostMapping(
            value = "/judge-login/redirect",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public ResponseEntity<Void> judgeLoginRedirect(
            @Valid @ModelAttribute JudgeLoginRequest request
    ) {
        String loginCode = authService.createJudgeLoginCode(request);
        URI redirectUri = URI.create(UriComponentsBuilder
                .fromUriString(FRONTEND_AUTH_CALLBACK_URL)
                .queryParam("code", loginCode)
                .build()
                .encode()
                .toUriString());
        return ResponseEntity.status(302).location(redirectUri).build();
    }
}
