# File2 API 가이드 (멀티 파일 업로드)

`FileController`(`/api/files`)는 **단일 파트 `file`** 업로드에 맞춰져 있고, **`File2Controller`(`/api/files2`)**는 **한 요청에 여러 파일**을 올릴 수 있도록 만든 엔드포인트입니다. DB·디스크 저장 방식은 File과 **동일**합니다.

---

## 1. File과 File2의 차이

| 구분 | File (`/api/files`) | File2 (`/api/files2`) |
|------|---------------------|------------------------|
| 업로드 `POST` | 파트 이름 **`file`** (한 개) | 파트 이름 **`files`** (여러 개) |
| 응답 본문 | `CommonApiResponse<FileResponse>` | `CommonApiResponse<List<FileResponse>>` |
| 성공 메시지 키 | `response.file.uploaded` | `response.file.uploaded_multi` |
| 목록·다운로드 | 동일한 데이터 소스 사용 | 동일 |

엔티티·테이블·저장 경로·멀티파트 크기 제한은 **공유**합니다. 자세한 스키마·`application.yml` 설명은 **`file.md`**를 참고하면 됩니다.

---

## 2. 추가·수정된 소스 (File2 기준)

### 2.1 File2 전용으로 추가된 클래스

| 경로 | 역할 |
|------|------|
| `src/main/java/com/example/backend/controller/File2Controller.java` | REST API 베이스 경로 `/api/files2` |

### 2.2 File2를 위해 확장된 기존 코드

| 경로 | 변경 내용 |
|------|-----------|
| `src/main/java/com/example/backend/service/FileStorageService.java` | `uploadAll(List<MultipartFile> files)` 메서드 추가 |
| `src/main/java/com/example/backend/service/FileStorageServiceImpl.java` | `uploadAll` 구현, 공통 저장 로직을 `persistUpload`로 분리 |
| `src/main/resources/messages/messages.properties` | `response.file.uploaded_multi` |
| `src/main/resources/messages/messages_en.properties` | `response.file.uploaded_multi` |

`application.yml`에 File2 **전용** 키는 없습니다. `spring.servlet.multipart`와 `app.file-storage.root-path`는 File과 같습니다.

---

## 3. REST API (`/api/files2`)

기본 경로: **`/api/files2`** (`SecurityConfig`상 `/api/**` 인증 필요)

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `POST` | `/api/files2` | **다건 업로드** — `multipart/form-data`, 파라미터 이름 **`files`** (`MultipartFile[]`) |
| `GET` | `/api/files2` | 목록 — Spring Data `Pageable` (기본 `size=20`, `sort=createdAt,desc`) |
| `GET` | `/api/files2/{id}/download` | 바이너리 다운로드 (`Content-Disposition: attachment`) |

- 업로드 응답: 저장된 각 파일마다 `FileResponse`가 리스트로 들어갑니다.
- 비어 있는 배열·모든 파트가 빈 파일이면 `IllegalArgumentException` → HTTP **400** (`BAD_REQUEST`).

---

## 4. 멀티파트 클라이언트 예시

### 4.1 HTML

같은 이름 `files`로 `multiple` 속성을 씁니다.

```html
<input type="file" name="files" multiple />
```

### 4.2 curl

`-F`를 `files`로 여러 번 지정합니다.

```bash
curl -u user:password ^
  -F "files=@C:\path\first.png" ^
  -F "files=@C:\path\second.txt" ^
  http://localhost:8080/api/files2
```

(한 파일만 보내도 됩니다. 그 경우에도 파트 이름은 **`files`**를 사용합니다.)

---

## 5. 서버 동작 요약 (`uploadAll`)

1. 요청에서 `files` 배열을 받아 `List<MultipartFile>`로 넘깁니다.
2. `null`이거나 빈 항목은 건너뛰고, **비어 있지 않은 파일만** 저장합니다.
3. 유효한 파일이 **하나도 없으면** 예외를 던집니다.
4. 각 파일은 File과 동일하게 디스크(`app.file-storage.root-path` 하위 `yyyy/MM/…`)에 쓰고, **`UploadedFile` 행**을 한 건씩 저장합니다.

---

## 6. 데이터베이스·테이블

- **새 테이블은 없습니다.** File2도 **`UploadedFile`** 테이블과 엔티티 `com.example.backend.domain.UploadedFile`을 사용합니다.
- 수동 DDL이 필요하면 **`file.md`**의 `UploadedFile` 생성 예시를 그대로 쓰면 됩니다.

---

## 7. 메시지(i18n)

| 키 | 용도 |
|----|------|
| `response.file.uploaded_multi` | File2 다건 업로드 성공 시 메시지 (한국어/영어 `messages*.properties`) |

---

## 8. Swagger

- OpenAPI 태그: **`File2 (멀티 업로드)`**
- Swagger UI 경로는 프로젝트 `springdoc` 설정을 따릅니다 (예: `/swagger-ui.html`).

---

## 9. 정리

- **File2** = **멀티 업로드 전용 API** + 동일 목록·다운로드.
- 인프라(DB·디스크·설정)는 **`file.md`**와 공유하므로, 운영·백업 시에도 **테이블 + 파일 디렉터리**를 함께 다루는 점은 File과 동일합니다.
