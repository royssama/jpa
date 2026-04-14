# Swagger DB 인증/권한 적용 가이드

이 문서는 `tb_com_user` 테이블 기반으로 Swagger 로그인 및 API 권한 제어를 적용한 현재 소스 기준 설명입니다.

## 1) 요구사항 반영 요약

- Swagger 접속 시 로그인 화면 먼저 노출
- 로그인 성공 후 Swagger UI(`/swagger-ui.html`)로 이동
- 로그인 검증 데이터: `tb_com_user.user_id`, `tb_com_user.password`
- 권한 데이터: `tb_com_user.role`
- 사용 권한: `ROLE_ADMIN`, `ROLE_USER`

## 2) 테이블 스키마

대상 테이블: `tb_com_user`

| 컬럼명 | 타입 | 설명 |
|---|---|---|
| ID | number | PK |
| user_id | varchar2(50) | 로그인 ID |
| password | varchar2(255) | 비밀번호(권장: BCrypt 해시) |
| user_name | varchar2(100) | 사용자명 |
| role | varchar2(20) | 권한(`ROLE_ADMIN`/`ROLE_USER` 또는 `ADMIN`/`USER`) |
| created_at | timestamp(6) | 생성일시 |

## 3) 주요 변경 파일

### 3-1. 사용자 엔티티
- `src/main/java/com/example/backend/domain/ComUser.java`
  - `@Table(name = "tb_com_user")`
  - 컬럼 매핑: `ID`, `user_id`, `password`, `user_name`, `role`, `created_at`

### 3-2. 사용자 조회 리포지토리
- `src/main/java/com/example/backend/repository/jpa/ComUserRepository.java`
  - `Optional<ComUser> findByUserId(String userId)`

### 3-3. 인증용 UserDetailsService
- `src/main/java/com/example/backend/service/ComUserDetailsService.java`
  - `user_id`로 사용자 조회
  - role 문자열 정규화:
    - `ADMIN` -> `ROLE_ADMIN`
    - `USER` -> `ROLE_USER`
    - 그 외/빈값은 기본 `ROLE_USER`

### 3-4. Spring Security 설정
- `src/main/java/com/example/backend/config/SecurityConfig.java`
  - 폼 로그인 활성화
  - 로그인 성공 시 `/swagger-ui.html` 이동
  - 접근 제어:
    - `/swagger-ui.html`, `/swagger-ui/**`, `/v3/api-docs/**` -> `ROLE_USER` 또는 `ROLE_ADMIN`
    - `/api/**` -> `ROLE_USER` 또는 `ROLE_ADMIN`
  - 비밀번호 매칭:
    - BCrypt 해시 문자열(`$2a$/$2b$/$2y$`)은 BCrypt로 검증
    - 그 외 문자열은 평문 비교(이행 단계 호환용)

### 3-5. OpenAPI 설명
- `src/main/java/com/example/backend/config/OpenApiConfig.java`
  - Swagger 설명 문구를 DB 기반 로그인 방식으로 갱신

## 4) 동작 흐름

1. 브라우저에서 `/swagger-ui.html` 접근
2. Spring Security가 인증 여부 확인
3. 미인증이면 로그인 페이지(`/login`) 표시
4. `tb_com_user.user_id/password`로 인증
5. 성공 시 `/swagger-ui.html`로 이동
6. `ROLE_USER` 또는 `ROLE_ADMIN`이면 Swagger/`/api/**` 접근 가능

## 5) 권한/비밀번호 운영 권장사항

- 운영 환경에서는 `password`를 BCrypt 해시로 저장 권장
- role 값은 `ROLE_ADMIN`, `ROLE_USER`로 통일 권장
- 평문 비교 허용은 데이터 이행 완료 후 제거 권장

## 6) 점검 체크리스트

- [ ] `tb_com_user`에 테스트 계정 존재
- [ ] role 값이 `ROLE_ADMIN`/`ROLE_USER` 또는 `ADMIN`/`USER` 형태
- [ ] `/swagger-ui.html` 진입 시 로그인 페이지로 리다이렉트
- [ ] 로그인 성공 후 Swagger UI 접근 가능
- [ ] 권한 없는 사용자 접근 시 403 확인
