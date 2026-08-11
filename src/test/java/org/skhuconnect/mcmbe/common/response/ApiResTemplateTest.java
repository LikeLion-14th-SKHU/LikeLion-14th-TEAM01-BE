package org.skhuconnect.mcmbe.common.response;

import org.junit.jupiter.api.Test;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.common.exception.SuccessCode;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResTemplateTest {

    @Test
    void successResponseContainsCodeMessageAndData() {
        ApiResTemplate<String> response = ApiResTemplate.success(SuccessCode.OK, "data");

        assertThat(response.success()).isTrue();
        assertThat(response.code()).isEqualTo("SUCCESS");
        assertThat(response.message()).isEqualTo("요청이 성공했습니다.");
        assertThat(response.data()).isEqualTo("data");
    }

    @Test
    void errorResponseContainsCodeAndNoData() {
        ApiResTemplate<Void> response = ApiResTemplate.error(ErrorCode.RESOURCE_NOT_FOUND);

        assertThat(response.success()).isFalse();
        assertThat(response.code()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(response.data()).isNull();
    }
}
