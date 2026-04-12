package com.example.backend.controller;

import com.example.backend.api.CommonApiResponse;
import com.example.backend.api.CommonApiResponseFactory;
import com.example.backend.domain.UploadedFile;
import com.example.backend.dto.FileResponse;
import com.example.backend.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Tag(name = "File2 (멀티 업로드)")
@RestController
@RequestMapping("/api/files2")
public class File2Controller {

    private final FileStorageService fileStorageService;
    private final CommonApiResponseFactory responseFactory;

    public File2Controller(FileStorageService fileStorageService, CommonApiResponseFactory responseFactory) {
        this.fileStorageService = fileStorageService;
        this.responseFactory = responseFactory;
    }

    @Operation(summary = "파일 다건 업로드 (multipart 파트 이름: files)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonApiResponse<List<FileResponse>> upload(
            @RequestPart("files") List<MultipartFile> files,
            Locale locale
    ) {
        Locale l = locale == null ? Locale.getDefault() : locale;
        List<UploadedFile> saved = fileStorageService.uploadAll(
                files == null ? List.of() : files
        );
        List<FileResponse> body = saved.stream().map(FileResponse::from).toList();
        return responseFactory.success("response.file.uploaded_multi", body, l);
    }

    @Operation(summary = "파일 단건 조회 (메타데이터만)")
    @GetMapping("/{id}")
    public CommonApiResponse<FileResponse> get(@PathVariable Long id, Locale locale) {
        Locale l = locale == null ? Locale.getDefault() : locale;
        UploadedFile entity = fileStorageService.getById(id);
        return responseFactory.success(FileResponse.from(entity), l);
    }

    @Operation(summary = "파일 목록 조회")
    @GetMapping
    public CommonApiResponse<Page<FileResponse>> list(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Locale locale
    ) {
        Locale l = locale == null ? Locale.getDefault() : locale;
        Page<FileResponse> page = fileStorageService.findAll(pageable).map(FileResponse::from);
        return responseFactory.success(page, l);
    }

    @Operation(summary = "파일 다운로드")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        UploadedFile meta = fileStorageService.getById(id);
        Resource resource = fileStorageService.loadAsResource(meta);

        String contentType = meta.getContentType() != null && !meta.getContentType().isBlank()
                ? meta.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(meta.getOriginalFilename(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }

    @Operation(summary = "파일 삭제 (디스크 파일 + DB 메타데이터)")
    @DeleteMapping("/{id}")
    public CommonApiResponse<Void> delete(@PathVariable Long id, Locale locale) {
        Locale l = locale == null ? Locale.getDefault() : locale;
        fileStorageService.deleteById(id);
        return responseFactory.success("response.file2.deleted", null, l);
    }
}
