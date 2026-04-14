package com.example.backend.controller;

import com.example.backend.api.CommonApiResponse;
import com.example.backend.api.CommonApiResponseFactory;
import com.example.backend.domain.UploadedBlobFile;
import com.example.backend.dto.FileBlobResponse;
import com.example.backend.service.FileBlobStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Tag(name = "File3 (DB BLOB 저장)")
@RestController
@RequestMapping("/api/files3")
public class File3Controller {

    private final FileBlobStorageService fileBlobStorageService;
    private final CommonApiResponseFactory responseFactory;

    public File3Controller(FileBlobStorageService fileBlobStorageService, CommonApiResponseFactory responseFactory) {
        this.fileBlobStorageService = fileBlobStorageService;
        this.responseFactory = responseFactory;
    }

    @Operation(summary = "파일 업로드 (단일: file, 다건: files — 둘 중 하나 이상)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonApiResponse<List<FileBlobResponse>> upload(
            @Parameter(
                    description = "여러 파일 (같은 이름으로 여러 파트)",
                    array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))
            )
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @Parameter(description = "단일 파일", schema = @Schema(type = "string", format = "binary"))
            @RequestParam(value = "file", required = false) MultipartFile file,
            Locale locale
    ) {
        Locale l = locale == null ? Locale.getDefault() : locale;
        List<MultipartFile> parts = mergeParts(files, file);
        List<FileBlobResponse> body = fileBlobStorageService.uploadAll(parts);
        String key = body.size() == 1 ? "response.file3.uploaded" : "response.file3.uploaded_multi";
        return responseFactory.success(key, body, l);
    }

    @Operation(summary = "파일 목록 조회 (BLOB 본문 제외)")
    @GetMapping
    public CommonApiResponse<Page<FileBlobResponse>> list(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Locale locale
    ) {
        Locale l = locale == null ? Locale.getDefault() : locale;
        return responseFactory.success(fileBlobStorageService.findAll(pageable), l);
    }

    @Operation(summary = "파일 메타데이터 단건 조회 (BLOB 본문 제외)")
    @GetMapping("/{id}")
    public CommonApiResponse<FileBlobResponse> getMeta(@PathVariable Long id, Locale locale) {
        Locale l = locale == null ? Locale.getDefault() : locale;
        return responseFactory.success(fileBlobStorageService.getMetaById(id), l);
    }

    @Operation(summary = "파일 다운로드 (DB BLOB)")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        UploadedBlobFile entity = fileBlobStorageService.getWithBlobById(id);
        Resource resource = fileBlobStorageService.loadAsResource(entity);

        String contentType = entity.getContentType() != null && !entity.getContentType().isBlank()
                ? entity.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(entity.getOriginalFilename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }

    @Operation(summary = "파일 삭제 (DB 행 및 BLOB 제거)")
    @DeleteMapping("/{id}")
    public CommonApiResponse<Void> delete(@PathVariable Long id, Locale locale) {
        Locale l = locale == null ? Locale.getDefault() : locale;
        fileBlobStorageService.deleteById(id);
        return responseFactory.success("response.file3.deleted", null, l);
    }

    private static List<MultipartFile> mergeParts(MultipartFile[] files, MultipartFile single) {
        List<MultipartFile> out = new ArrayList<>();
        if (files != null) {
            for (MultipartFile f : files) {
                if (f != null && !f.isEmpty()) {
                    out.add(f);
                }
            }
        }
        if (single != null && !single.isEmpty()) {
            out.add(single);
        }
        return out;
    }
}
