package org.skhuconnect.mcmbe.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.skhuconnect.mcmbe.auth.dto.KakaoLoginResponse;
import org.skhuconnect.mcmbe.auth.dto.RefreshTokenRequest;
import org.skhuconnect.mcmbe.auth.dto.TokenResponse;
import org.skhuconnect.mcmbe.auth.service.AuthService;
import org.skhuconnect.mcmbe.common.exception.SuccessCode;
import org.skhuconnect.mcmbe.common.response.ApiResTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Tag(
        name = "Kakao & JWT API",
        description = "카카오 로그인 및 JWT 인증 관련 API이다."
)
@Validated
@RestController
@RequestMapping("/detective/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "카카오 로그인 시작",
            description = "카카오 로그인 화면으로 이동합니다."
    )
    @GetMapping("/kakao/login")
    public ResponseEntity<Void> kakaoLogin() {
        return ResponseEntity.status(302)
                .location(URI.create(authService.getKakaoAuthorizationUrl()))
                .build();
    }

    @Operation(
            summary = "카카오 로그인 결과 처리",
            description = "카카오 로그인 완료 후 인가 코드를 받아 사용자 정보를 조회하고 우리 서비스 JWT를 발급합니다."
    )
    @GetMapping("/kakao/callback")
    public ResponseEntity<ApiResTemplate<KakaoLoginResponse>> kakaoCallback(
            @RequestParam @NotBlank String code,
            @RequestParam @NotBlank String state
    ) {
        return ResponseEntity.ok(ApiResTemplate.success(
                SuccessCode.OK,
                authService.loginWithKakao(code, state)
        ));
    }

    @Operation(
            summary = "JWT 재발급",
            description = "저장된 Refresh Token을 검증한 뒤 기존 토큰을 폐기하고 새로운 Access Token과 Refresh Token을 발급합니다. 이전 Refresh Token은 재사용할 수 없습니다."
    )
    @PostMapping("/refresh")
    public ResponseEntity<ApiResTemplate<TokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(ApiResTemplate.success(
                SuccessCode.OK,
                authService.refresh(request.refreshToken())
        ));
    }
}
