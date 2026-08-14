package org.skhuconnect.mcmbe.ai.client;

import org.skhuconnect.mcmbe.ai.config.AiServerProperties;
import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.conversation.entity.CharacterType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class AiServerSuspectClient implements SuspectAiClient {

    private static final Map<CharacterType, String> CHARACTER_PATHS = Map.of(
            CharacterType.FELIX, "/chat/felix",
            CharacterType.EMIL, "/chat/emil",
            CharacterType.JOHANNES, "/chat/johannes",
            CharacterType.CLARA, "/chat/klara"
    );

    private final RestClient restClient;
    private final AiServerProperties properties;

    public AiServerSuspectClient(RestClient aiServerRestClient, AiServerProperties properties) {
        this.restClient = aiServerRestClient;
        this.properties = properties;
    }

    @Override
    public String answer(CharacterType characterType, Long conversationId, String question) {
        if (!properties.isConfigured()) {
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }

        AiServerRequest request = new AiServerRequest(
                "conversation-" + conversationId,
                question
        );

        try {
            AiServerResponse response = restClient.post()
                    .uri(CHARACTER_PATHS.get(characterType))
                    .body(request)
                    .retrieve()
                    .body(AiServerResponse.class);

            if (response == null || response.reply() == null || response.reply().isBlank()) {
                throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
            }
            return response.reply().trim();
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE, exception);
        }
    }

    private record AiServerRequest(String session_id, String message) {
    }

    private record AiServerResponse(String reply) {
    }
}
