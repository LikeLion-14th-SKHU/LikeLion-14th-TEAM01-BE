package org.skhuconnect.mcmbe.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.skhuconnect.mcmbe.ai.config.OpenAiProperties;
import org.skhuconnect.mcmbe.ai.prompt.CharacterPromptProvider;
import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.conversation.entity.CharacterType;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiSuspectClient implements SuspectAiClient {

    private final RestClient restClient;
    private final OpenAiProperties properties;
    private final CharacterPromptProvider promptProvider;

    public OpenAiSuspectClient(
            RestClient openAiRestClient,
            OpenAiProperties properties,
            CharacterPromptProvider promptProvider
    ) {
        this.restClient = openAiRestClient;
        this.properties = properties;
        this.promptProvider = promptProvider;
    }

    @Override
    public String answer(
            CharacterType characterType,
            List<AiConversationMessage> messages,
            String question
    ) {
        if (!properties.isConfigured()) {
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }

        List<Map<String, String>> input = new ArrayList<>();
        for (AiConversationMessage message : messages) {
            input.add(Map.of("role", message.role(), "content", message.content()));
        }
        input.add(Map.of("role", "user", "content", question));

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", properties.model());
        request.put("instructions", promptProvider.getPrompt(characterType));
        request.put("input", input);

        try {
            JsonNode response = restClient.post()
                    .uri("/v1/responses")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);

            String answer = extractOutputText(response);
            if (answer == null || answer.isBlank()) {
                throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
            }
            return answer.trim();
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE, exception);
        }
    }

    private String extractOutputText(JsonNode response) {
        if (response == null) {
            return null;
        }

        JsonNode output = response.path("output");
        if (!output.isArray()) {
            return null;
        }

        for (JsonNode item : output) {
            JsonNode content = item.path("content");
            if (!content.isArray()) {
                continue;
            }
            for (JsonNode contentItem : content) {
                if ("output_text".equals(contentItem.path("type").asText())) {
                    return contentItem.path("text").asText(null);
                }
            }
        }
        return null;
    }
}
