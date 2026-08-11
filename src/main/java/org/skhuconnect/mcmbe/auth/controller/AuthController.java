package org.skhuconnect.mcmbe.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.skhuconnect.mcmbe.auth.dto.AuthorizationUrlResponse;
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

@Validated
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "카카오 로그인 URL 조회")
    @GetMapping("/kakao/authorization-url")
    public ResponseEntity<ApiResTemplate<AuthorizationUrlResponse>> getAuthorizationUrl() {
        AuthorizationUrlResponse data = new AuthorizationUrlResponse(
                authService.getKakaoAuthorizationUrl()
        );
        return ResponseEntity.ok(ApiResTemplate.success(SuccessCode.OK, data));
    }

    @Operation(summary = "카카오 로그인 콜백")
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

    @Operation(summary = "JWT 재발급")
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
