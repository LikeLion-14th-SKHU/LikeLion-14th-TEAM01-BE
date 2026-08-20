package org.skhuconnect.mcmbe.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.skhuconnect.mcmbe.auth.dto.JudgeLoginRequest;
import org.skhuconnect.mcmbe.auth.dto.LoginExchangeResponse;
import org.skhuconnect.mcmbe.auth.service.AuthService;
import org.skhuconnect.mcmbe.common.exception.SuccessCode;
import org.skhuconnect.mcmbe.common.response.ApiResTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/detective/auth")
public class JudgeAuthController {

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
}
