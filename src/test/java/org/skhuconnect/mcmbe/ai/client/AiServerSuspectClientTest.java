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
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.GET;

class AiServerSuspectClientTest {

    private static final String BASE_URL = "http://ai-server.test";
    private static final String AI_SESSION_ID = "550e8400-e29b-41d4-a716-446655440000";

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
                        {"session_id":"550e8400-e29b-41d4-a716-446655440000","message":"어디에 있었나요?"}
                        """))
                .andRespond(withSuccess("""
                        {"reply":"  답변입니다.  "}
                        """, MediaType.APPLICATION_JSON));

        String answer = client.answer(characterType, AI_SESSION_ID, "어디에 있었나요?");

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

        assertThatThrownBy(() -> client.answer(CharacterType.FELIX, AI_SESSION_ID, "질문"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void keepsAiServiceUnavailableResponseWhenAiServerReturnsError() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AiServerSuspectClient client = new AiServerSuspectClient(
                builder.build(),
                new AiServerProperties(BASE_URL)
        );
        server.expect(requestTo(BASE_URL + "/chat/felix/init?session_id=" + AI_SESSION_ID))
                .andRespond(withStatus(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> client.initialize(CharacterType.FELIX, AI_SESSION_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(org.skhuconnect.mcmbe.common.exception.ErrorCode.AI_SERVICE_UNAVAILABLE);
        server.verify();
    }

    @ParameterizedTest
    @EnumSource(CharacterType.class)
    void sendsSessionToCharacterInitializationEndpoint(CharacterType characterType) {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AiServerSuspectClient client = new AiServerSuspectClient(
                builder.build(),
                new AiServerProperties(BASE_URL)
        );

        server.expect(once(), requestTo(BASE_URL + pathOf(characterType) + "/init?session_id=" + AI_SESSION_ID))
                .andExpect(method(GET))
                .andRespond(withSuccess("""
                        {"reply":"초기 증언", "recommended_question":"무엇을 보았나요?"}
                        """, MediaType.APPLICATION_JSON));

        SuspectAiInitialization initialization = client.initialize(characterType, AI_SESSION_ID);

        assertThat(initialization.initialMessage()).isEqualTo("초기 증언");
        assertThat(initialization.recommendedQuestion()).isEqualTo("무엇을 보았나요?");
        server.verify();
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
