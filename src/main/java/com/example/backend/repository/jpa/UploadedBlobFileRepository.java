package com.example.backend.repository.jpa;

import com.example.backend.domain.UploadedBlobFile;
import com.example.backend.dto.FileBlobResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UploadedBlobFileRepository extends JpaRepository<UploadedBlobFile, Long> {

    @Query("""
            SELECT new com.example.backend.dto.FileBlobResponse(
                e.uploadedBlobFileId, e.originalFilename, e.contentType, e.fileSize, e.createdAt)
            FROM UploadedBlobFile e
            """)
    Page<FileBlobResponse> findAllMeta(Pageable pageable);

    @Query("""
            SELECT new com.example.backend.dto.FileBlobResponse(
                e.uploadedBlobFileId, e.originalFilename, e.contentType, e.fileSize, e.createdAt)
            FROM UploadedBlobFile e
            WHERE e.uploadedBlobFileId = :id
            """)
    Optional<FileBlobResponse> findMetaById(@Param("id") Long id);
}
