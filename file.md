# 파일 업로드·다운로드 기능 가이드

이 문서는 백엔드에 **파일 메타데이터(DB) + 디스크 저장** 방식으로 파일 기능을 붙이기 위해 추가된 항목을 정리한 것입니다.

---

## 1. 개요

- **메타데이터**: JPA 엔티티 `UploadedFile` → 테이블 **`UploadedFile`**
- **실제 바이트**: OS 파일 시스템, 루트는 `application.yml`의 **`app.file-storage.root-path`**
- **API**: `FileController` — 업로드(`multipart`), 목록(페이징), 다운로드(바이너리)
- **보안**: 기존과 동일하게 `/api/**`는 인증 필요 (Basic 등 `SecurityConfig` 기준)

---

## 2. 추가·수정된 파일 목록

### 2.1 새로 추가된 Java 소스

| 경로 | 역할 |
|------|------|
| `src/main/java/com/example/backend/controller/FileController.java` | REST API (`/api/files`) |
| `src/main/java/com/example/backend/domain/UploadedFile.java` | JPA 엔티티, 테이블 `UploadedFile` 매핑 |
| `src/main/java/com/example/backend/repository/jpa/UploadedFileRepository.java` | Spring Data JPA 리포지토리 |
| `src/main/java/com/example/backend/service/FileStorageService.java` | 파일 서비스 인터페이스 |
| `src/main/java/com/example/backend/service/FileStorageServiceImpl.java` | 업로드·조회·디스크 로드 구현 |
| `src/main/java/com/example/backend/dto/FileResponse.java` | 목록·업로드 응답용 DTO |

### 2.2 기존 파일 수정

| 경로 | 변경 내용 |
|------|-----------|
| `src/main/resources/application.yml` | 멀티파트 크기 제한, `app.file-storage.root-path` |
| `src/main/resources/messages/messages.properties` | `response.file.uploaded` 메시지 키 |
| `src/main/resources/messages/messages_en.properties` | 동일 키 영문 |
| `src/main/java/com/example/backend/exception/GlobalExceptionHandler.java` | `IllegalArgumentException`(400), `IllegalStateException`(500) 처리 |

### 2.3 의존성(`build.gradle.kts`)

파일 기능 전용 라이브러리 추가는 **없음**. 기존 `spring-boot-starter-web`, `spring-boot-starter-data-jpa`로 충분합니다.

---

## 3. `application.yml`에 들어간 설정

### 3.1 멀티파트(업로드 크기)

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 55MB
```

- 한 요청에 포함되는 **파일 하나** 최대 `50MB`, **전체 요청** 최대 `55MB` (여러 파트·필드 포함 시 여유).
- 운영에서 더 크게 쓰려면 값만 조정하면 됩니다.

### 3.2 파일 저장 루트 디렉터리

```yaml
app:
  file-storage:
    root-path: ${user.home}/.jpa-file-storage
```

- 업로드된 실제 파일이 저장되는 **최상위 경로**입니다.
- `${user.home}`는 OS 사용자 홈으로 치환됩니다 (Windows/Linux 모두 가능).
- 애플리케이션 기동 시 `FileStorageServiceImpl`에서 디렉터리가 없으면 생성합니다.

### 3.3 JPA와 함께 쓰는 경우(참고)

현재 프로젝트에는 이미 다음이 있습니다.

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

- `update`이면 엔티티 기준으로 **`UploadedFile` 테이블이 자동 생성/갱신**될 수 있습니다.
- 운영에서 수동 DDL만 쓰는 정책이면 `ddl-auto`를 `validate` 등으로 바꾸고, 아래 **수동 DDL**로 테이블을 만듭니다.

---

## 4. 데이터베이스: 테이블 `UploadedFile`

### 4.1 컬럼과 엔티티 매핑

| DB 컬럼 (물리명) | Java 필드 | 설명 |
|------------------|-----------|------|
| `UploadedFileId` | `uploadedFileId` | PK, 자동 증가 |
| `OriginalFilename` | `originalFilename` | 사용자가 올린 원본 파일명(표시·다운로드 파일명에 사용) |
| `StoredFilename` | `storedFilename` | 디스크에 쓰인 파일명(UUID + 확장자 등) |
| `ContentType` | `contentType` | MIME 타입(없을 수 있음) |
| `FileSize` | `fileSize` | 바이트 크기 |
| `RelativePath` | `relativePath` | `root-path` 기준 상대 경로(예: `2026/04/uuid.png`) |
| `CreatedAt` | `createdAt` | 등록 시각 |

### 4.2 SQLite용 수동 DDL 예시

엔티티와 맞춘 예시입니다. (DB 제품에 맞게 타입만 조정하면 됩니다.)

```sql
CREATE TABLE UploadedFile (
  UploadedFileId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  OriginalFilename VARCHAR(500) NOT NULL,
  StoredFilename VARCHAR(500) NOT NULL,
  ContentType VARCHAR(255),
  FileSize INTEGER NOT NULL,
  RelativePath VARCHAR(1000) NOT NULL,
  CreatedAt TEXT NOT NULL
);
```

- Hibernate가 `LocalDateTime`을 SQLite에서 어떻게 저장하는지는 dialect 설정에 따릅니다. 수동으로 넣을 때는 프로젝트에서 실제로 생성된 테이블 정의를 한 번 확인하는 것이 안전합니다.

---

## 5. 디스크 저장 규칙

- 상대 경로 형식: **`yyyy/MM/` + 저장 파일명** (예: `2026/04/550e8400-e29b-41d4-a716-446655440000.pdf`)
- 최종 절대 경로: **`app.file-storage.root-path` + RelativePath**
- DB에는 **전체 절대 경로가 아니라 `RelativePath`만** 저장합니다 (환경 이동·백업에 유리).

---

## 6. REST API 요약

기본 경로: **`/api/files`**

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `POST` | `/api/files` | 업로드. `multipart/form-data`, 파트 이름 **`file`** |
| `GET` | `/api/files` | 목록. Spring Data `Pageable` (기본: `size=20`, `sort=createdAt,desc`) |
| `GET` | `/api/files/{id}/download` | 파일 바이너리 다운로드 (`Content-Disposition: attachment`) |

- JSON 래핑이 있는 응답은 기존과 동일하게 `CommonApiResponse` 형식을 따릅니다.
- 다운로드는 **바이너리**라 `CommonApiResponse`가 아니라 `ResponseEntity<Resource>`입니다.

### 6.1 인증

- `/api/**`는 `SecurityConfig`에서 인증이 필요합니다 (예: HTTP Basic `user` / `password` — 실제 값은 설정 확인).

### 6.2 Swagger

- 태그: **File**
- UI: `springdoc` 설정에 따름 (예: `/swagger-ui.html`).

---

## 7. 메시지(i18n)

| 키 | 용도 |
|----|------|
| `response.file.uploaded` | 업로드 성공 시 메시지 (`messages.properties` / `messages_en.properties`) |

`CommonApiResponseFactory.success("response.file.uploaded", ...)` 에서 사용합니다.

---

## 8. 예외·HTTP 상태 (파일 기능 관련)

`GlobalExceptionHandler`에서 다음이 파일 흐름과 연관될 수 있습니다.

| 예외 | HTTP | `code` (본문) | 설명 |
|------|------|----------------|------|
| `IllegalArgumentException` | 400 | `BAD_REQUEST` | 빈 파일 등 잘못된 요청 |
| `ResourceNotFoundException` | 404 | `NOT_FOUND` | ID 없음, 디스크에 파일 없음 등 |
| `IllegalStateException` | 500 | `FILE_STORAGE_ERROR` | 디스크 쓰기 실패 등 |

---

## 9. 빠른 점검 체크리스트

1. **`application.yml`**: `spring.servlet.multipart`, `app.file-storage.root-path` 확인
2. **DB**: `UploadedFile` 테이블 존재 (`ddl-auto: update` 또는 수동 DDL)
3. **디스크**: `root-path`에 쓰기 권한, 디스크 여유 공간
4. **API 호출**: `/api/files`는 인증 헤더 포함
5. **업로드 테스트**: `POST` 시 파트 이름이 반드시 **`file`**

---

## 10. 다른 환경에 옮길 때

- **`app.file-storage.root-path`**: 서버별로 공유 스토리지(NAS)·절대 경로로 변경
- **DB 백업**: `UploadedFile` 테이블 + **파일 디렉터리 전체**를 함께 백업해야 복구가 완전합니다.

이 문서는 저장소의 `file.md`로 유지하면, 파일 기능을 처음 보는 사람도 설정·DB·디스크·API를 한 번에 따라갈 수 있습니다.
