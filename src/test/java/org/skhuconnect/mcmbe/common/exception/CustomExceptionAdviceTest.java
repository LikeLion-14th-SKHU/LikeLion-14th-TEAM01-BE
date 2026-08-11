package org.skhuconnect.mcmbe.common.exception;

import org.junit.jupiter.api.Test;
import org.skhuconnect.mcmbe.common.response.ApiResTemplate;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class CustomExceptionAdviceTest {

    private final CustomExceptionAdvice advice = new CustomExceptionAdvice();

    @Test
    void businessExceptionUsesCommonErrorResponse() {
        ResponseEntity<ApiResTemplate<Void>> response = advice.handleBusinessException(
                new BusinessException(ErrorCode.RESOURCE_NOT_FOUND)
        );

        assertThat(response.getStatusCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND.getHttpStatus());
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().code()).isEqualTo("RESOURCE_NOT_FOUND");
    }
}
