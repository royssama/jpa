package com.example.backend.dto;

import com.example.backend.domain.UploadedBlobFile;

import java.time.LocalDateTime;

/**
 * BLOB 본문 없이 메타데이터만 노출할 때 사용합니다.
 */
public record FileBlobResponse(
        Long uploadedBlobFileId,
        String originalFilename,
        String contentType,
        long fileSize,
        LocalDateTime createdAt
) {

    public static FileBlobResponse from(UploadedBlobFile e) {
        return new FileBlobResponse(
                e.getUploadedBlobFileId(),
                e.getOriginalFilename(),
                e.getContentType(),
                e.getFileSize(),
                e.getCreatedAt()
        );
    }
}
