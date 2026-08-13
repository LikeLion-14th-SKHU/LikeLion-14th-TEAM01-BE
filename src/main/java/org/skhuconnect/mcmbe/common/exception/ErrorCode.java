package org.skhuconnect.mcmbe.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements ResponseCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "입력값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메서드입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "요청한 리소스를 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "회원 정보를 찾을 수 없습니다."),
    DESIGNER_NAME_ALREADY_SET(HttpStatus.CONFLICT, "DESIGNER_NAME_ALREADY_SET", "디자이너 닉네임은 최초 1회만 설정할 수 있습니다."),
    DESIGNER_NAME_REQUIRED(HttpStatus.CONFLICT, "DESIGNER_NAME_REQUIRED", "게임 시작 전에 디자이너 닉네임을 설정해야 합니다."),
    DESIGN_DIRECTION_ALREADY_SELECTED(HttpStatus.CONFLICT, "DESIGN_DIRECTION_ALREADY_SELECTED", "디자인 방향은 최초 1회만 선택할 수 있습니다."),
    DESIGN_DIRECTION_REQUIRED(HttpStatus.CONFLICT, "DESIGN_DIRECTION_REQUIRED", "사건 선택 전에 디자인 방향을 선택해야 합니다."),
    GAME_ALREADY_STARTED(HttpStatus.CONFLICT, "GAME_ALREADY_STARTED", "게임은 최초 1회만 시작할 수 있습니다."),
    GAME_NOT_IN_PROGRESS(HttpStatus.CONFLICT, "GAME_NOT_IN_PROGRESS", "진행 중인 사건이 없습니다."),
    CHARACTER_NOT_AVAILABLE_FOR_CURRENT_CASE(HttpStatus.FORBIDDEN, "CHARACTER_NOT_AVAILABLE_FOR_CURRENT_CASE", "현재 사건에 속하지 않은 캐릭터입니다."),
    KAKAO_LOGIN_FAILED(HttpStatus.BAD_GATEWAY, "KAKAO_LOGIN_FAILED", "카카오 로그인 처리에 실패했습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 인증 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN", "만료된 인증 토큰입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
