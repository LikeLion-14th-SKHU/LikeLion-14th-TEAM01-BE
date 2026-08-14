package org.skhuconnect.mcmbe.ai.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.skhuconnect.mcmbe.ai.config.AiServerProperties;
import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.conversation.entity.CharacterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

class AiServerSuspectClientTest {

    private static final String BASE_URL = "http://ai-server.test";

    @ParameterizedTest
    @EnumSource(CharacterType.class)
    void sendsConversationSessionAndQuestionToCharacterEndpoint(CharacterType characterType) {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AiServerSuspectClient client = new AiServerSuspectClient(
                builder.build(),
                new AiServerProperties(BASE_URL)
        );

        server.expect(once(), requestTo(BASE_URL + pathOf(characterType)))
                .andExpect(method(POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"session_id":"conversation-42","message":"어디에 있었나요?"}
                        """))
                .andRespond(withSuccess("""
                        {"reply":"  답변입니다.  "}
                        """, MediaType.APPLICATION_JSON));

        String answer = client.answer(characterType, 42L, "어디에 있었나요?");

        assertThat(answer).isEqualTo("답변입니다.");
        server.verify();
    }

    @Test
    void rejectsBlankReply() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AiServerSuspectClient client = new AiServerSuspectClient(
                builder.build(),
                new AiServerProperties(BASE_URL)
        );
        server.expect(requestTo(BASE_URL + "/chat/felix"))
                .andRespond(withSuccess("{\"reply\":\" \"}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.answer(CharacterType.FELIX, 1L, "질문"))
                .isInstanceOf(BusinessException.class);
    }

    private String pathOf(CharacterType characterType) {
        return switch (characterType) {
            case FELIX -> "/chat/felix";
            case EMIL -> "/chat/emil";
            case JOHANNES -> "/chat/johannes";
            case CLARA -> "/chat/klara";
        };
    }
}
