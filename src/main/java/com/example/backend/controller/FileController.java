package com.example.backend.controller;

import com.example.backend.api.CommonApiResponse;
import com.example.backend.api.CommonApiResponseFactory;
import com.example.backend.domain.UploadedFile;
import com.example.backend.dto.FileResponse;
import com.example.backend.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Tag(name = "File")
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;
    private final CommonApiResponseFactory responseFactory;

    public FileController(FileStorageService fileStorageService, CommonApiResponseFactory responseFactory) {
        this.fileStorageService = fileStorageService;
        this.responseFactory = responseFactory;
    }

    @Operation(summary = "파일 업로드")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonApiResponse<FileResponse> upload(
            @Parameter(description = "업로드 파일", schema = @Schema(type = "string", format = "binary"))
            @RequestPart("file") MultipartFile file,
            Locale locale
    ) {
        Locale l = locale == null ? Locale.getDefault() : locale;
        UploadedFile saved = fileStorageService.upload(file);
        return responseFactory.success("response.file.uploaded", FileResponse.from(saved), l);
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
}
