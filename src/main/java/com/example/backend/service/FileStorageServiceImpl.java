package com.example.backend.service;

import com.example.backend.domain.UploadedFile;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.jpa.UploadedFileRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final UploadedFileRepository uploadedFileRepository;
    private final Path storageRoot;

    public FileStorageServiceImpl(
            UploadedFileRepository uploadedFileRepository,
            @Value("${app.file-storage.root-path}") String rootPath
    ) {
        this.uploadedFileRepository = uploadedFileRepository;
        this.storageRoot = Paths.get(rootPath).toAbsolutePath().normalize();
    }

    @PostConstruct
    void ensureRootExists() throws IOException {
        Files.createDirectories(storageRoot);
    }

    @Override
    @Transactional
    public UploadedFile upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
        }
        return persistUpload(file);
    }

    @Override
    @Transactional
    public List<UploadedFile> uploadAll(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }
        List<UploadedFile> saved = new ArrayList<>();
        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) {
                continue;
            }
            saved.add(persistUpload(f));
        }
        if (saved.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
        }
        return saved;
    }

    private UploadedFile persistUpload(MultipartFile file) {
        String original = file.getOriginalFilename();
        String safeOriginal = original != null && !original.isBlank()
                ? Paths.get(original).getFileName().toString()
                : "unnamed";

        String extension = "";
        int dot = safeOriginal.lastIndexOf('.');
        if (dot >= 0) {
            extension = safeOriginal.substring(dot);
        }

        String storedFilename = UUID.randomUUID() + extension;
        String subDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        Path relative = Paths.get(subDir, storedFilename);
        Path absolute = storageRoot.resolve(relative).normalize();
        if (!absolute.startsWith(storageRoot)) {
            throw new IllegalArgumentException("잘못된 저장 경로입니다.");
        }

        try {
            Files.createDirectories(absolute.getParent());
            file.transferTo(absolute);
        } catch (IOException e) {
            throw new IllegalStateException("파일 저장에 실패했습니다.", e);
        }

        String relativePath = relative.toString().replace('\\', '/');
        UploadedFile entity = new UploadedFile();
        entity.setOriginalFilename(safeOriginal);
        entity.setStoredFilename(storedFilename);
        entity.setContentType(file.getContentType());
        entity.setFileSize(file.getSize());
        entity.setRelativePath(relativePath);
        entity.setCreatedAt(LocalDateTime.now());

        try {
            return uploadedFileRepository.save(entity);
        } catch (RuntimeException e) {
            try {
                Files.deleteIfExists(absolute);
            } catch (IOException ignored) {
                // best-effort cleanup
            }
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UploadedFile getById(Long id) {
        return uploadedFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("파일을 찾을 수 없습니다: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UploadedFile> findAll(Pageable pageable) {
        return uploadedFileRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource loadAsResource(UploadedFile meta) {
        Path absolute = storageRoot.resolve(meta.getRelativePath()).normalize();
        if (!absolute.startsWith(storageRoot)) {
            throw new ResourceNotFoundException("파일 경로가 올바르지 않습니다.");
        }
        if (!Files.isRegularFile(absolute)) {
            throw new ResourceNotFoundException("저장소에서 파일을 찾을 수 없습니다: " + meta.getUploadedFileId());
        }
        return new FileSystemResource(absolute);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        UploadedFile meta = uploadedFileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("파일을 찾을 수 없습니다: " + id));
        Path absolute = storageRoot.resolve(meta.getRelativePath()).normalize();
        if (!absolute.startsWith(storageRoot)) {
            throw new ResourceNotFoundException("파일 경로가 올바르지 않습니다.");
        }
        try {
            Files.deleteIfExists(absolute);
        } catch (IOException e) {
            throw new IllegalStateException("디스크에서 파일 삭제에 실패했습니다.", e);
        }
        uploadedFileRepository.deleteById(id);
    }
}
