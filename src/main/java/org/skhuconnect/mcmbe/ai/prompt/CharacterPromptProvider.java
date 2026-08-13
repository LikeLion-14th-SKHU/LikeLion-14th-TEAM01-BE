package org.skhuconnect.mcmbe.ai.prompt;

import org.skhuconnect.mcmbe.conversation.entity.CharacterType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CharacterPromptProvider {

    private static final Map<CharacterType, String> CHARACTER_ROLES = Map.of(
            CharacterType.CLARA, "SIGNATURE 사건의 패턴 장인 클라라",
            CharacterType.JOHANNES, "SIGNATURE 사건의 사진작가 요하네스",
            CharacterType.FELIX, "FUNCTION 사건의 제품 설계자 펠릭스",
            CharacterType.EMIL, "FUNCTION 사건의 테스트 담당자 에밀"
    );

    public String getPrompt(CharacterType characterType) {
        return "당신은 " + CHARACTER_ROLES.get(characterType) + "입니다. "
                + "용의자 심문에서 해당 인물의 관점으로 한국어로 답변하세요. "
                + "제공되지 않은 구체적인 사건 사실이나 설정은 임의로 만들지 말고, "
                + "알 수 없는 내용은 알 수 없다고 답변하세요. 답변 텍스트만 반환하세요.";
    }
}
