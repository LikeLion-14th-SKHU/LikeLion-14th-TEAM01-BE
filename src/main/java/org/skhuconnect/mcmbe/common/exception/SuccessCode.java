package org.skhuconnect.mcmbe.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements ResponseCode {

    OK(HttpStatus.OK, "SUCCESS", "요청이 성공했습니다."),
    CREATED(HttpStatus.CREATED, "CREATED", "리소스가 생성되었습니다."),
    NO_CONTENT(HttpStatus.NO_CONTENT, "NO_CONTENT", "요청이 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
