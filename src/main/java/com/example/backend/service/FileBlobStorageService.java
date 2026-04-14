package com.example.backend.service;

import com.example.backend.domain.UploadedBlobFile;
import com.example.backend.dto.FileBlobResponse;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileBlobStorageService {

    FileBlobResponse upload(MultipartFile file);

    List<FileBlobResponse> uploadAll(List<MultipartFile> files);

    Page<FileBlobResponse> findAll(Pageable pageable);

    FileBlobResponse getMetaById(Long id);

    UploadedBlobFile getWithBlobById(Long id);

    Resource loadAsResource(UploadedBlobFile entity);

    void deleteById(Long id);
}
