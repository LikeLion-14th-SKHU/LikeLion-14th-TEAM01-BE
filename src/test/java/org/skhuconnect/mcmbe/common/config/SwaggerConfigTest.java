package org.skhuconnect.mcmbe.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerConfigTest {

    @Test
    void openApiContainsProjectInformation() {
        OpenAPI openAPI = new SwaggerConfig().mcmOpenApi();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("MCM API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("v1");
    }
}
