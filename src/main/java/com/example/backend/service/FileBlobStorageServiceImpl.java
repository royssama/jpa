package com.example.backend.service;

import com.example.backend.domain.UploadedBlobFile;
import com.example.backend.dto.FileBlobResponse;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.jpa.UploadedBlobFileRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileBlobStorageServiceImpl implements FileBlobStorageService {

    private final UploadedBlobFileRepository repository;

    public FileBlobStorageServiceImpl(UploadedBlobFileRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public FileBlobResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
        }
        UploadedBlobFile saved = persistBlob(file);
        return FileBlobResponse.from(saved);
    }

    @Override
    @Transactional
    public List<FileBlobResponse> uploadAll(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        List<FileBlobResponse> out = new ArrayList<>();
        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) {
                continue;
            }
            out.add(FileBlobResponse.from(persistBlob(f)));
        }
        if (out.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
        }
        return out;
    }

    private UploadedBlobFile persistBlob(MultipartFile file) {
        String original = file.getOriginalFilename();
        String safeOriginal = original != null && !original.isBlank()
                ? Paths.get(original).getFileName().toString()
                : "unnamed";

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("파일을 읽는 데 실패했습니다.", e);
        }
        if (bytes.length == 0) {
            throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
        }

        UploadedBlobFile entity = new UploadedBlobFile();
        entity.setOriginalFilename(safeOriginal);
        entity.setContentType(file.getContentType());
        entity.setFileSize(bytes.length);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setFileData(bytes);

        return repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileBlobResponse> findAll(Pageable pageable) {
        return repository.findAllMeta(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public FileBlobResponse getMetaById(Long id) {
        return repository.findMetaById(id)
                .orElseThrow(() -> new ResourceNotFoundException("파일을 찾을 수 없습니다: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public UploadedBlobFile getWithBlobById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("파일을 찾을 수 없습니다: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Resource loadAsResource(UploadedBlobFile entity) {
        byte[] data = entity.getFileData();
        if (data == null || data.length == 0) {
            throw new ResourceNotFoundException("저장된 파일 데이터가 없습니다: " + entity.getUploadedBlobFileId());
        }
        return new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return entity.getOriginalFilename();
            }
        };
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("파일을 찾을 수 없습니다: " + id);
        }
        repository.deleteById(id);
    }
}
