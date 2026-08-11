package org.skhuconnect.mcmbe.common.exception;

import org.springframework.http.HttpStatus;

public interface ResponseCode {

    HttpStatus getHttpStatus();

    String getCode();

    String getMessage();
}
