# AGENTS.md

## Project

- 프로젝트명: MCM
- Backend: Java 17, Spring Boot 4.1.0, Gradle
- Database: MySQL
- 기본 API prefix는 `/detective`를 사용한다.
- 이 프로젝트는 SKHU Connect와 별개의 프로젝트다.
- 기존 코드와 현재 프로젝트 구조를 우선 기준으로 작업한다.

## Service

- 사용자가 여러 용의자와 AI 기반 자유 대화를 진행하며 범인을 추리하는 게임 서비스다.
- AI 연동은 OpenAI API를 사용한다.
- 용의자의 성격, 설정, 게임 데이터 등은 가능한 한 DB에서 관리하고 백엔드가 조회하여 사용한다.
- AI 관련 세부 정책이 확정되지 않은 경우 임의로 설계하지 말고 먼저 확인한다.

## Architecture

- Controller / Service / Repository의 책임을 분리한다.
- 기존 패키지 구조와 구현 방식을 우선 따른다.
- 요청하지 않은 대규모 리팩터링이나 구조 변경은 하지 않는다.
- 기존 기능을 수정할 때 다른 기능에 미치는 영향을 먼저 확인한다.
- 정책이나 요구사항이 불명확하면 추측하여 구현하지 않는다.

## API

- 공통 응답은 `ApiResTemplate`을 사용한다.
- 성공/실패 응답은 기존 `SuccessCode`, `ErrorCode` 체계를 따른다.
- 비즈니스 예외는 기존 `BusinessException` 및 전역 예외 처리 구조를 사용한다.
- Validation은 기존 예외 처리 방식을 따른다.
- 새 API는 기존 `/detective` prefix와 URL 규칙을 따른다.

## Swagger

- API 추가 또는 변경 시 Swagger 문서도 함께 반영한다.
- `@Tag`, `@Operation`의 설명은 이해하기 쉬운 한국어로 작성한다.
- 한국어는 실제 UTF-8 문자로 작성하고 `\uXXXX` 형태로 작성하지 않는다.
- JWT 인증이 필요한 API는 기존 Bearer 인증 설정을 따른다.

## Authentication

- 인증은 Kakao OAuth + 서비스 자체 JWT 구조를 사용한다.
- Access Token과 Refresh Token의 기존 역할을 유지한다.
- Spring Security는 Stateless 방식을 유지한다.
- 인증/인가 구조를 요청 없이 변경하지 않는다.

## Security

- 비밀번호, API Key, Client Secret, JWT Secret 등 민감정보를 코드에 작성하거나 출력하지 않는다.
- `application-dev.yml`의 실제 비밀값을 읽거나 출력하거나 커밋하지 않는다.
- 운영 비밀값은 환경변수를 사용한다.
- 로그, 테스트 코드, README, 커밋 메시지에도 실제 비밀값을 남기지 않는다.

## Verification

- 구현 후 변경 범위에 맞는 테스트를 확인한다.
- 전체 테스트가 필요한 경우 기본 명령은 `./gradlew test`이다.
- 배포 가능한 빌드 확인은 `./gradlew clean build`를 사용한다.
- 테스트 실패 시 원인을 먼저 분석하고 관련 없는 코드를 임의 수정하지 않는다.

## Git

- 기능 개발은 `dev`에서 별도 브랜치를 생성하여 진행한다.
- 기능은 `feat/*`, 문서는 `docs/*`, 수정은 `fix/*` 형태의 브랜치를 사용한다.
- 작업 완료 후 PR을 통해 `dev`에 반영한다.
- 안정화된 `dev`를 PR을 통해 `main`에 반영한다.
- 요청 없이 직접 `main`의 기존 코드를 변경하거나 강제 푸시하지 않는다.

## Agent Rules

- 작업 전 관련 코드만 필요한 범위에서 확인한다.
- 이미 확인된 파일을 이유 없이 반복해서 전체 탐색하지 않는다.
- 불필요하게 긴 설명이나 문서를 생성하지 않는다.
- 실제 코드와 문서를 근거로 판단하고 없는 정책을 만들어내지 않는다.
- 요구사항 밖의 기능은 추가하지 않는다.
- 수정한 파일과 핵심 변경사항을 작업 완료 후 짧게 보고한다.