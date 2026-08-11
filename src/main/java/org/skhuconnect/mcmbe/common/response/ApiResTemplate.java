package org.skhuconnect.mcmbe.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.common.exception.SuccessCode;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResTemplate<T>(
        boolean success,
        String code,
        String message,
        T data
) {

    public static <T> ApiResTemplate<T> success(SuccessCode successCode, T data) {
        return new ApiResTemplate<>(
                true,
                successCode.getCode(),
                successCode.getMessage(),
                data
        );
    }

    public static ApiResTemplate<Void> success(SuccessCode successCode) {
        return success(successCode, null);
    }

    public static <T> ApiResTemplate<T> error(ErrorCode errorCode, T data) {
        return new ApiResTemplate<>(
                false,
                errorCode.getCode(),
                errorCode.getMessage(),
                data
        );
    }

    public static ApiResTemplate<Void> error(ErrorCode errorCode) {
        return error(errorCode, null);
    }
}
