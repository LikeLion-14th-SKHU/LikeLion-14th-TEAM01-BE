# MCM Backend

MCM은 AI 용의자와 자유 대화를 진행해 두 사건의 범인을 추리하는 게임 API 서버입니다.

## 기술 및 실행 환경

- Java 17, Spring Boot 4.1, Gradle, JPA, MySQL
- 기본 API 경로: `/detective`
- API 문서: `/swagger-ui.html`
- 인증: Kakao OAuth + Stateless JWT

운영 환경의 DB·인증·AI 설정은 환경 변수로 주입하며, 운영 JPA 스키마 모드는 `validate`입니다.

## 구현 완료 기능

- 카카오 로그인, Access/Refresh JWT 발급 및 Refresh Token Rotation
- 로그아웃(서버 Refresh Token 폐기), 회원탈퇴
- 디자이너 닉네임 및 디자인 방향 최초 1회 설정
- 1회성 게임 진행, 사건 선택·최종 추리·진행 상태 조회
- AI 용의자 자유 심문, 대화 기록 저장·조회, 캐릭터별 질문 3회 제한
- 게임 완료 시 디자이너 패스 발급 및 마이페이지 조회
- 게임 완료자의 저장된 디자인 방향 기반 MCM 상품 추천

## 주요 API

| 영역 | API |
| --- | --- |
| 인증 | `GET /auth/kakao/login`, `GET /auth/kakao/callback`, `POST /auth/exchange`, `POST /auth/refresh`, `POST /auth/logout` |
| 회원 | `POST /designer-name`, `DELETE /members/me` |
| 게임 | `POST /games/design-direction`, `POST /games/current-case`, `POST /games/final-deduction`, `GET /games` |
| 대화 | `POST /conversations/{characterType}/messages`, `POST /conversations/{characterType}/complete`, `GET /conversations/{characterType}` |
| 마이페이지·상품 | `GET /mypage`, `GET /products/recommendation` |

위 경로에는 모두 `/detective` 접두사가 붙습니다. 로그인·토큰 재발급을 제외한 API는 Bearer Access Token이 필요합니다.

## 주요 엔티티와 상태

- `Member`, `RefreshToken`, `GameProgress`, `Conversation`, `ConversationMessage`, `DesignerPass`, `RecommendedProduct`
- 게임 상태: `NOT_STARTED`, `IN_PROGRESS`, `FAILED`, `COMPLETED`
- 사건: `FUNCTION`, `SIGNATURE`
- 디자인 방향: `TRAVEL`, `DAILY_TRAVEL`, `HANDS_FREE`
- 대화 상태: `NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`

자세한 게임 규칙은 [GAME_POLICY.md](GAME_POLICY.md)를 참고합니다.
