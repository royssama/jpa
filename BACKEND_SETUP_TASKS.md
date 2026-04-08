버전2

DBeaver·SQLite 관련 확인은 아래 **답변 요약**을 기준으로 반영했습니다.

---

## 확인 완료 (답변 요약)

| 항목 | 내용 |
|------|------|
| **1. DBeaver에서 쓰던 SQLite (참고)** | `C:\Users\phy\AppData\Roaming\DBeaverData\workspace6\.metadata\sample-database-sqlite-1\Chinook.db` |
| **2. 위 DB의 JDBC URL (참고)** | `jdbc:sqlite:C:\Users\phy\AppData\Roaming\DBeaverData\workspace6\.metadata\sample-database-sqlite-1\Chinook.db` |
| **3. 이 프로젝트 DB** | **새로 구축** — Chinook(샘플 스키마)과 **같은 파일을 쓰지 않음**. Spring Boot·JPA·MyBatis용 DB는 프로젝트 루트 기준 `data/app.db` 로 분리 (`application.yml`의 `spring.datasource.url`). DBeaver에서는 동일 경로의 파일을 **새 연결**로 열면 됨. |
| **4. 로그인 정책** | 당장은 미정. **대규모·실무 스타일**(JWT·세션 등)로 갈 예정이면, 이후 단계에서 선택 후 `SecurityConfig` 등을 교체하면 됨. 지금은 HTTP Basic 샘플 유지. |

---

## 단계별 할 일 리스트

### 1. 사전 준비 (IntelliJ 기준 · JDK/Gradle 별도 설치 없음)

- [ ] **IntelliJ IDEA**에서 `c:\projects\jpa` 폴더를 **Open** (Gradle 프로젝트로 인식)
- [ ] **Gradle**은 **Wrapper**만 사용: *Settings → Build Tools → Gradle* → *Gradle distribution*: **Wrapper** (기본값)
- [ ] **Gradle JVM**: IntelliJ **Embedded JDK** 또는 **21**이 포함된 JDK 선택 (별도 시스템 설치 없이 IDE에서 JDK 내려받기 가능)
- [ ] 이 프로젝트는 **Java 21 툴체인**; PC에 21이 없으면 Gradle(Foojay)이 빌드 시 **자동으로 JDK 21을 내려받을 수 있음** (`settings.gradle.kts` 참고)
- [ ] **QueryDSL Q타입**: *Settings → Build, Execution, Deployment → Compiler → Annotation Processors* → **Enable annotation processing** 켜기
- [x] DBeaver·SQLite 답변 반영 완료 (위 **확인 완료** 표 참고)  

### 2. 프로젝트 뼈대

- [ ] Gradle 기반 Spring Boot 3.x 프로젝트 생성  
- [ ] 패키지 구조 정리 (`config`, `domain`, `repository`, `service`, `controller` 등)  

### 3. 의존성 (Gradle)

- [ ] Spring Web, Spring Data JPA, Validation  
- [ ] Spring Security (로그인·API 보호)  
- [ ] SQLite JDBC 드라이버  
- [ ] Hibernate Community Dialects (SQLite용)  
- [ ] QueryDSL (`jakarta` 분류자) + APT  
- [ ] MyBatis Spring Boot Starter  
- [ ] SpringDoc OpenAPI (Swagger UI)  

### 4. 데이터베이스 연결

- [ ] `application.yml`에 `spring.datasource.url`·`driver-class-name`·`username`(SQLite는 보통 불필요) 설정  
- [ ] JPA: `ddl-auto`, dialect(SQLite), 로깅 레벨(선택)  
- [ ] MyBatis: `mapper-locations`, `type-aliases-package`  
- [ ] 앱 기동 후 DBeaver에서 테이블 생성 여부 확인  

### 5. 도메인·JPA

- [ ] 엔티티 작성 (예: `Item`)  
- [ ] `JpaRepository` 기본 CRUD  

### 6. QueryDSL

- [ ] `JPAQueryFactory` 빈 등록  
- [ ] 커스텀 조회용 Repository 인터페이스 + 구현체 (Q타입 사용)  
- [ ] Gradle 빌드 시 Q클래스 생성 경로가 소스셋에 포함되는지 확인  

### 7. MyBatis

- [ ] Mapper 인터페이스 + XML (`select`, `insert`, `update`, `delete`)  
- [ ] 서비스에서 Mapper 호출로 CRUD  

### 8. API 레이어

- [ ] JPA/QueryDSL용 REST 컨트롤러 (예: `/api/jpa/items`)  
- [ ] MyBatis용 REST 컨트롤러 (예: `/api/mybatis/items`)  
- [ ] 요청/응답 DTO 및 검증(선택)  

### 9. Spring Security + Swagger 연동

- [ ] `/swagger-ui/**`, `/v3/api-docs/**` 등 Swagger 경로 허용  
- [ ] `/api/**`는 인증 필요  
- [ ] HTTP Basic(또는 선택한 방식)으로 Swagger **Authorize**에서 호출 가능하도록 OpenAPI 보안 스키마 설정  
- [ ] 브라우저에서 Swagger UI 열고, 인증 후 각 API Try it out 성공 확인  

### 10. 검증·마무리

- [ ] IntelliJ에서 `BackendApplication` **Run** 또는 Gradle 탭에서 `bootRun` 실행  
- [ ] Swagger에서 JPA·MyBatis CRUD 각각 호출 테스트  
- [ ] 401 발생 시: 인증 헤더·경로 permit 규칙 재확인  

---

## 완료 후 기본 접속 정보 (이 프로젝트 샘플 기준)

| 항목 | 값 |
|------|-----|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| HTTP Basic 사용자 | `user` |
| HTTP Basic 비밀번호 | `password` |

앱 전용 DB 파일은 프로젝트 루트의 `data/app.db` (`jdbc:sqlite:${user.dir}/data/app.db`)입니다. DBeaver에서 이 파일을 열면 `items` 테이블 등을 확인할 수 있습니다.

---

## 이 저장소에 이미 만들어 둔 것 (요약)

| 구분 | 경로 / 설명 |
|------|----------------|
| Gradle | `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `gradlew.bat`, `gradle/wrapper/` (Foojay 툴체인 자동 해석) |
| 엔티티 | `Item` → 테이블 `items` |
| JPA CRUD | `ItemJpaRepository` + `ItemJpaController` (`/api/jpa/items`) |
| QueryDSL | `ItemQueryRepositoryImpl`, 검색 API `GET /api/jpa/items/search?keyword=` |
| MyBatis CRUD | `ItemMyBatisMapper` + XML + `ItemMyBatisController` (`/api/mybatis/items`) |
| 보안 | HTTP Basic, 계정 `user` / `password`, `/api/**` 인증 필요 |
| Swagger | SpringDoc — UI: `http://localhost:8080/swagger-ui.html` (Authorize에 Basic 입력) |

**실행 (IntelliJ 권장):**

1. `BackendApplication.java` 우클릭 → **Run**  
2. 또는 Gradle 창에서 **Tasks → application → bootRun**

터미널에서 Wrapper만 쓸 때(선택): `.\gradlew.bat bootRun` — 시스템에 JDK를 따로 깔지 않아도, IntelliJ가 연 Gradle JVM 또는 툴체인 자동 다운로드로 빌드됩니다.

Chinook 샘플 DB는 참고용일 뿐이며, 이 앱은 `data/app.db`를 사용합니다. 경로를 바꾸려면 `application.yml`의 `spring.datasource.url`만 수정하면 됩니다.
