# File3 API 가이드 (DB BLOB 저장)

`FileController`·`File2Controller`는 **디스크**에 바이트를 저장하고 DB에는 경로·메타만 둡니다. **`File3Controller`(`/api/files3`)**는 **파일 본문을 DB의 BLOB 컬럼**에 저장하는 방식입니다.

---

## 1. File / File2와 File3의 차이

| 구분 | File (`/api/files`), File2 (`/api/files2`) | File3 (`/api/files3`) |
|------|---------------------------------------------|------------------------|
| 본문 저장 위치 | OS 파일 시스템 (`app.file-storage.root-path`) | **DB `FileData` (BLOB)** |
| DB에 남는 것 | 메타 + 상대 경로 | 메타 + **`byte[]` 본문** |
| 목록·메타 조회 | File/File2와 동일 개념 | **JPQL로 BLOB 제외** 조회 (성능·메모리) |

### CLOB과 BLOB

- **BLOB**: 바이너리(이미지, PDF, ZIP 등). JPA에서는 `@Lob` + `byte[]`로 매핑하는 것이 일반적입니다.
- **CLOB**: 매우 긴 **문자열** 전용. 바이너리 파일을 “긴 문자열”로 넣는 방식과는 용도가 다릅니다.

File3는 **바이너리 파일**을 다루므로 **BLOB** 컬럼을 사용합니다.

---

## 2. 추가된 소스 (File3 기준)

### 2.1 새로 추가된 클래스

| 경로 | 역할 |
|------|------|
| `src/main/java/com/example/backend/controller/File3Controller.java` | REST API 베이스 경로 `/api/files3` |
| `src/main/java/com/example/backend/domain/UploadedBlobFile.java` | JPA 엔티티, 테이블 `UploadedBlobFile`, `@Lob byte[] fileData` |
| `src/main/java/com/example/backend/dto/FileBlobResponse.java` | BLOB 없이 메타만 응답할 때 사용 |
| `src/main/java/com/example/backend/repository/jpa/UploadedBlobFileRepository.java` | JPA 리포지토리, 목록·단건 메타는 생성자 표현식으로 BLOB 미조회 |
| `src/main/java/com/example/backend/service/FileBlobStorageService.java` | 인터페이스 |
| `src/main/java/com/example/backend/service/FileBlobStorageServiceImpl.java` | 업로드·조회·삭제·다운로드용 `Resource` 생성 |

### 2.2 메시지(i18n) 추가

| 경로 | 추가 키 |
|------|---------|
| `src/main/resources/messages/messages.properties` | `response.file3.uploaded`, `response.file3.uploaded_multi`, `response.file3.deleted` |
| `src/main/resources/messages/messages_en.properties` | 동일 키 영문 |

### 2.3 `application.yml`

File3 **전용** 설정 항목은 없습니다. 업로드 크기는 기존과 같이 **`spring.servlet.multipart`** (`max-file-size`, `max-request-size`)가 적용됩니다. 디스크 루트 `app.file-storage.root-path`는 **File3에서 사용하지 않습니다.**

---

## 3. 데이터베이스: 테이블 `UploadedBlobFile`

### 3.1 컬럼과 엔티티 매핑

| DB 컬럼 | Java 필드 | 설명 |
|---------|-----------|------|
| `UploadedBlobFileId` | `uploadedBlobFileId` | PK, 자동 증가 |
| `OriginalFilename` | `originalFilename` | 원본 파일명 |
| `ContentType` | `contentType` | MIME (없을 수 있음) |
| `FileSize` | `fileSize` | 바이트 길이 |
| `CreatedAt` | `createdAt` | 저장 시각 |
| `FileData` | `fileData` | **파일 본문 (BLOB), `@Lob byte[]`** |

### 3.2 SQLite용 수동 DDL 예시

```sql
CREATE TABLE UploadedBlobFile (
  UploadedBlobFileId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  OriginalFilename VARCHAR(500) NOT NULL,
  ContentType VARCHAR(255),
  FileSize INTEGER NOT NULL,
  CreatedAt TEXT NOT NULL,
  FileData BLOB NOT NULL
);
```

프로젝트에 `spring.jpa.hibernate.ddl-auto: update`가 있으면 엔티티 기준으로 테이블이 생성·변경될 수 있습니다. 운영에서 수동 DDL만 쓰는 경우 위 스키마를 DB에 맞게 조정하면 됩니다.

---

## 4. REST API (`/api/files3`)

기본 경로: **`/api/files3`** (`SecurityConfig`에 따라 `/api/**` 인증 필요)

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `POST` | `/api/files3` | 업로드. `multipart/form-data` — **`file`(단일)** 또는 **`files`(다건)** 중 하나 이상 |
| `GET` | `/api/files3` | 목록(페이징). **BLOB 제외** |
| `GET` | `/api/files3/{id}` | 메타데이터만. **BLOB 제외** |
| `GET` | `/api/files3/{id}/download` | DB BLOB → 바이너리 다운로드 |
| `DELETE` | `/api/files3/{id}` | 행 삭제(BLOB 포함) |

- JSON 래핑 응답은 `CommonApiResponse`를 사용합니다.
- 다운로드는 바이너리이므로 `ResponseEntity<Resource>`입니다.
- 업로드 성공 시 본문은 **`List<FileBlobResponse>`**이며, 저장 건수가 1건이면 메시지 키는 `response.file3.uploaded`, 2건 이상이면 `response.file3.uploaded_multi`를 쓰도록 구현되어 있습니다.

### 4.1 업로드 시 multipart 파트 이름

- **한 파일**: 파트 이름 **`file`**
- **여러 파일**: 파트 이름 **`files`** (같은 이름으로 여러 파트)

둘 다 보내면 합쳐서 처리됩니다.

### 4.2 인증·Swagger

- 인증: **`file.md`**의 File API와 동일하게 `/api/**` 규칙 적용.
- OpenAPI 태그: **`File3 (DB BLOB 저장)`**. Swagger UI 경로는 `springdoc` 설정을 따릅니다.

---

## 5. 서버 동작 요약

1. 업로드 시 `MultipartFile.getBytes()`로 읽어 **`UploadedBlobFile.fileData`**에 저장합니다.
2. 목록·`GET /{id}` 메타는 리포지토리의 **JPQL 생성자 표현식**으로 **BLOB 컬럼을 SELECT하지 않습니다.**
3. 다운로드 시에만 `findById` 등으로 **BLOB 포함 엔티티**를 읽습니다.
4. 삭제 시 해당 PK 행 전체가 삭제되어 BLOB도 함께 제거됩니다.

---

## 6. 운영·대용량 시 유의사항

- 업로드 처리에서 **파일 전체가 JVM 메모리에 올라갈 수 있습니다.** `spring.servlet.multipart`의 **파일당·요청당 크기** 안에서 운용하는 것이 안전합니다.
- DB에 큰 BLOB을 많이 쌓으면 **DB 크기·백업 시간·조회 부하**가 커집니다. 초대용량·대량 트래픽에는 **디스크 저장(File/File2)** 또는 객체 스토리지(S3 등)를 검토하는 경우가 많습니다.

---

## 7. 관련 문서

- 디스크 저장 방식·공통 `application.yml`·`UploadedFile` 테이블: **`file.md`**
- 멀티 업로드(디스크): **`file2.md`**

이 문서(`file3.md`)는 **DB BLOB 방식(File3)** 만을 다룹니다.
