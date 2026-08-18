package org.skhuconnect.mcmbe.ai.client;

import org.skhuconnect.mcmbe.ai.config.AiServerProperties;
import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.skhuconnect.mcmbe.conversation.entity.CharacterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.Map;

@Component
public class AiServerSuspectClient implements SuspectAiClient {

    private static final Logger log = LoggerFactory.getLogger(AiServerSuspectClient.class);

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
    public SuspectAiInitialization initialize(CharacterType characterType, String aiSessionId) {
        String path = CHARACTER_PATHS.get(characterType) + "/init";
        if (!properties.isConfigured()) {
            log.error("AI server call unavailable: method={}, path={}, reason=AI_SERVER_BASE_URL_NOT_CONFIGURED",
                    HttpMethod.GET, path);
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }

        try {
            AiServerInitializationResponse response = restClient.get()
                    .uri(path + "?session_id={sessionId}", aiSessionId)
                    .retrieve()
                    .body(AiServerInitializationResponse.class);

            if (response == null || response.reply() == null || response.reply().isBlank()) {
                log.error("AI server call failed: method={}, path={}, category=EMPTY_RESPONSE",
                        HttpMethod.GET, path);
                throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
            }
            return new SuspectAiInitialization(
                    response.reply().trim(),
                    response.recommendedQuestion() == null ? null : response.recommendedQuestion().trim()
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            logAiFailure(HttpMethod.GET, path, exception);
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE, exception);
        }
    }

    @Override
    public String answer(CharacterType characterType, String aiSessionId, String question) {
        String path = CHARACTER_PATHS.get(characterType);
        if (!properties.isConfigured()) {
            log.error("AI server call unavailable: method={}, path={}, reason=AI_SERVER_BASE_URL_NOT_CONFIGURED",
                    HttpMethod.POST, path);
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
        }

        AiServerRequest request = new AiServerRequest(
                aiSessionId,
                question
        );

        try {
            AiServerResponse response = restClient.post()
                    .uri(path)
                    .body(request)
                    .retrieve()
                    .body(AiServerResponse.class);

            if (response == null || response.reply() == null || response.reply().isBlank()) {
                log.error("AI server call failed: method={}, path={}, category=EMPTY_RESPONSE",
                        HttpMethod.POST, path);
                throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE);
            }
            return response.reply().trim();
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            logAiFailure(HttpMethod.POST, path, exception);
            throw new BusinessException(ErrorCode.AI_SERVICE_UNAVAILABLE, exception);
        }
    }

    private void logAiFailure(HttpMethod method, String path, Exception exception) {
        Throwable rootCause = rootCauseOf(exception);
        RestClientResponseException responseException = findCause(exception, RestClientResponseException.class);
        Integer statusCode = responseException == null ? null : responseException.getStatusCode().value();

        log.error(
                "AI server call failed: method={}, path={}, category={}, exceptionType={}, exceptionMessage={}, status={}",
                method,
                path,
                failureCategoryOf(exception, statusCode),
                rootCause.getClass().getName(),
                rootCause.getMessage(),
                statusCode
        );
    }

    private String failureCategoryOf(Exception exception, Integer statusCode) {
        if (statusCode != null) {
            return statusCode >= 400 && statusCode < 500 ? "HTTP_4XX" : "HTTP_5XX";
        }
        if (findCause(exception, HttpConnectTimeoutException.class) != null) {
            return "CONNECT_TIMEOUT";
        }
        if (findCause(exception, HttpTimeoutException.class) != null) {
            return "READ_TIMEOUT";
        }
        if (findCause(exception, ConnectException.class) != null
                || findCause(exception, UnknownHostException.class) != null
                || findCause(exception, ResourceAccessException.class) != null) {
            return "CONNECTION_FAILURE";
        }
        if (findCause(exception, HttpMessageConversionException.class) != null
                || findCause(exception, com.fasterxml.jackson.core.JsonProcessingException.class) != null) {
            return "JSON_MAPPING_FAILURE";
        }
        return "UNEXPECTED_ERROR";
    }

    private Throwable rootCauseOf(Throwable exception) {
        Throwable current = exception;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private <T extends Throwable> T findCause(Throwable exception, Class<T> type) {
        Throwable current = exception;
        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }

    private record AiServerRequest(String session_id, String message) {
    }

    private record AiServerResponse(String reply) {
    }

    private record AiServerInitializationResponse(
            String reply,
            @com.fasterxml.jackson.annotation.JsonProperty("recommended_question") String recommendedQuestion
    ) {
    }
}
