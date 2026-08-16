package org.skhuconnect.mcmbe.ai.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class AiServerConfigTest {

    @Test
    void configuresExplicitAiConnectAndReadTimeouts() {
        assertThat(AiServerConfig.CONNECT_TIMEOUT).isEqualTo(Duration.ofSeconds(3));
        assertThat(AiServerConfig.READ_TIMEOUT).isEqualTo(Duration.ofSeconds(30));
    }
}
