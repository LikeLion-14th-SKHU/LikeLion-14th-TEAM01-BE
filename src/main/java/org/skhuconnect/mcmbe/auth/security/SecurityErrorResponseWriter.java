package org.skhuconnect.mcmbe.auth.security;

import jakarta.servlet.http.HttpServletResponse;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;

import java.io.IOException;

public final class SecurityErrorResponseWriter {

    private SecurityErrorResponseWriter() {
    }

    public static void write(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write("""
                {"success":false,"code":"%s","message":"%s"}
                """.formatted(errorCode.getCode(), errorCode.getMessage()).trim());
    }
}
