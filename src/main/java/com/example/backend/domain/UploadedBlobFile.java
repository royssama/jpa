package com.example.backend.domain;

import com.example.backend.domain.converter.LocalDateTimeIsoAttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "UploadedBlobFile")
public class UploadedBlobFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UploadedBlobFileId")
    private Long uploadedBlobFileId;

    @Column(name = "OriginalFilename", nullable = false, length = 500)
    private String originalFilename;

    @Column(name = "ContentType", length = 255)
    private String contentType;

    @Column(name = "FileSize", nullable = false)
    private long fileSize;

    @Convert(converter = LocalDateTimeIsoAttributeConverter.class)
    @Column(name = "CreatedAt", nullable = false, length = 40)
    private LocalDateTime createdAt;

    /** SQLite JDBC Blob(getBlob) 미지원 → VARBINARY로 getBytes 경로 사용 */
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Column(name = "FileData", nullable = false)
    private byte[] fileData;

    public UploadedBlobFile() {
    }

    public Long getUploadedBlobFileId() {
        return uploadedBlobFileId;
    }

    public void setUploadedBlobFileId(Long uploadedBlobFileId) {
        this.uploadedBlobFileId = uploadedBlobFileId;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }
}
