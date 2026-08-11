package org.skhuconnect.mcmbe.auth.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoUserResponse(
        Long id,
        @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KakaoAccount(
            String email,
            Profile profile
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Profile(
            String nickname,
            @JsonProperty("profile_image_url") String profileImageUrl
    ) {
    }

    public String email() {
        return kakaoAccount == null ? null : kakaoAccount.email();
    }

    public String nickname() {
        if (kakaoAccount == null || kakaoAccount.profile() == null) {
            return "카카오 사용자";
        }
        String nickname = kakaoAccount.profile().nickname();
        return nickname == null || nickname.isBlank() ? "카카오 사용자" : nickname;
    }

    public String profileImageUrl() {
        if (kakaoAccount == null || kakaoAccount.profile() == null) {
            return null;
        }
        return kakaoAccount.profile().profileImageUrl();
    }
}
