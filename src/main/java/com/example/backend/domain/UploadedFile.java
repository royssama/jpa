package com.example.backend.domain;

import com.example.backend.domain.converter.LocalDateTimeIsoAttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "UploadedFile")
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UploadedFileId")
    private Long uploadedFileId;

    @Column(name = "OriginalFilename", nullable = false, length = 500)
    private String originalFilename;

    @Column(name = "StoredFilename", nullable = false, length = 500)
    private String storedFilename;

    @Column(name = "ContentType", length = 255)
    private String contentType;

    @Column(name = "FileSize", nullable = false)
    private long fileSize;

    @Column(name = "RelativePath", nullable = false, length = 1000)
    private String relativePath;

    @Convert(converter = LocalDateTimeIsoAttributeConverter.class)
    @Column(name = "CreatedAt", nullable = false, length = 40)
    private LocalDateTime createdAt;

    public UploadedFile() {
    }

    public Long getUploadedFileId() {
        return uploadedFileId;
    }

    public void setUploadedFileId(Long uploadedFileId) {
        this.uploadedFileId = uploadedFileId;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
