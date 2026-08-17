package org.skhuconnect.mcmbe.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.skhuconnect.mcmbe.auth.dto.KakaoLoginResponse;
import org.skhuconnect.mcmbe.auth.dto.RefreshTokenRequest;
import org.skhuconnect.mcmbe.auth.dto.TokenResponse;
import org.skhuconnect.mcmbe.auth.jwt.AuthenticatedMember;
import org.skhuconnect.mcmbe.auth.service.AuthService;
import org.skhuconnect.mcmbe.common.exception.SuccessCode;
import org.skhuconnect.mcmbe.common.response.ApiResTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.util.UriComponentsBuilder;
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

    private static final String FRONTEND_AUTH_CALLBACK_URL =
            "https://seongju-detective.vercel.app/";

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
            description = "카카오 로그인 완료 후 JWT를 발급하고 프론트엔드 인증 callback으로 redirect합니다. "
                    + "인증 결과는 서버 로그와 Referer에 남지 않도록 URL fragment로 전달합니다."
    )
    @ApiResponse(responseCode = "302", description = "프론트엔드 인증 화면으로 redirect")
    @GetMapping("/kakao/callback")
    public ResponseEntity<Void> kakaoCallback(
            @RequestParam @NotBlank String code,
            @RequestParam @NotBlank String state
    ) {
        KakaoLoginResponse login = authService.loginWithKakao(code, state);
        URI redirectUri = URI.create(UriComponentsBuilder
                .fromUriString(FRONTEND_AUTH_CALLBACK_URL)
                .fragment("tokenType={tokenType}&accessToken={accessToken}"
                        + "&accessTokenExpiresIn={accessTokenExpiresIn}"
                        + "&refreshToken={refreshToken}"
                        + "&refreshTokenExpiresIn={refreshTokenExpiresIn}"
                        + "&memberId={memberId}&newMember={newMember}&nickname={nickname}")
                .buildAndExpand(
                        login.tokens().tokenType(),
                        login.tokens().accessToken(),
                        login.tokens().accessTokenExpiresIn(),
                        login.tokens().refreshToken(),
                        login.tokens().refreshTokenExpiresIn(),
                        login.memberId(),
                        login.newMember(),
                        login.nickname()
                )
                .encode()
                .toUriString());
        return ResponseEntity.status(302).location(redirectUri).build();
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

    @Operation(
            summary = "로그아웃",
            description = "로그인 사용자의 서버 저장 Refresh Token을 폐기합니다. "
                    + "현재 Access Token은 만료 전까지 클라이언트에서도 함께 삭제해야 합니다."
    )
    @ApiResponse(responseCode = "204", description = "로그아웃 성공")
    @ApiResponse(responseCode = "401", description = "Access Token이 없거나 유효하지 않음")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    ) {
        authService.logout(authenticatedMember.memberId());
        return ResponseEntity.noContent().build();
    }
}
