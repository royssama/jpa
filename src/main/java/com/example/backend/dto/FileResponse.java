package com.example.backend.dto;

import com.example.backend.domain.UploadedFile;

import java.time.LocalDateTime;

public record FileResponse(
        Long uploadedFileId,
        String originalFilename,
        String contentType,
        long fileSize,
        LocalDateTime createdAt
) {

    public static FileResponse from(UploadedFile f) {
        return new FileResponse(
                f.getUploadedFileId(),
                f.getOriginalFilename(),
                f.getContentType(),
                f.getFileSize(),
                f.getCreatedAt()
        );
    }
}
