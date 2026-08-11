package org.skhuconnect.mcmbe.auth.kakao;

import org.skhuconnect.mcmbe.auth.config.AuthProperties;
import org.skhuconnect.mcmbe.common.exception.BusinessException;
import org.skhuconnect.mcmbe.common.exception.ErrorCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class KakaoApiClient {

    private static final String AUTHORIZATION_URL = "https://kauth.kakao.com/oauth/authorize";
    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    private final AuthProperties.Kakao properties;
    private final RestClient restClient;

    public KakaoApiClient(AuthProperties authProperties) {
        this.properties = authProperties.kakao();
        this.restClient = RestClient.create();
    }

    public String buildAuthorizationUrl(String state) {
        return UriComponentsBuilder.fromUriString(AUTHORIZATION_URL)
                .queryParam("response_type", "code")
                .queryParam("client_id", properties.clientId())
                .queryParam("redirect_uri", properties.redirectUri())
                .queryParam("state", state)
                .build()
                .encode()
                .toUriString();
    }

    public KakaoUserResponse getUser(String authorizationCode) {
        try {
            KakaoTokenResponse token = requestToken(authorizationCode);
            KakaoUserResponse user = restClient.get()
                    .uri(USER_INFO_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.accessToken())
                    .retrieve()
                    .body(KakaoUserResponse.class);

            if (user == null || user.id() == null) {
                throw new BusinessException(ErrorCode.KAKAO_LOGIN_FAILED);
            }
            return user;
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new BusinessException(ErrorCode.KAKAO_LOGIN_FAILED, exception);
        }
    }

    private KakaoTokenResponse requestToken(String authorizationCode) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.clientId());
        form.add("redirect_uri", properties.redirectUri());
        form.add("code", authorizationCode);
        if (properties.clientSecret() != null && !properties.clientSecret().isBlank()) {
            form.add("client_secret", properties.clientSecret());
        }

        KakaoTokenResponse token = restClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(KakaoTokenResponse.class);

        if (token == null || token.accessToken() == null || token.accessToken().isBlank()) {
            throw new BusinessException(ErrorCode.KAKAO_LOGIN_FAILED);
        }
        return token;
    }
}
