package com.example.backend.service;

import com.example.backend.domain.UploadedFile;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {

    UploadedFile upload(MultipartFile file);

    /**
     * 비어 있지 않은 파일만 저장합니다. 최소 1개 이상이어야 합니다.
     */
    List<UploadedFile> uploadAll(List<MultipartFile> files);

    UploadedFile getById(Long id);

    Page<UploadedFile> findAll(Pageable pageable);

    Resource loadAsResource(UploadedFile meta);

    /**
     * 디스크의 저장 파일과 DB 메타데이터를 함께 삭제합니다.
     */
    void deleteById(Long id);
}
