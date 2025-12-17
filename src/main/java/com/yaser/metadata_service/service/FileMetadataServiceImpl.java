package com.yaser.metadata_service.service;

import com.yaser.metadata_service.entity.FileMetadata;
import com.yaser.metadata_service.entity.Status;
import com.yaser.metadata_service.repository.FileMetadataRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FileMetadataServiceImpl implements FileMetadataService {

    private final FileMetadataRepository fileMetadataRepository;

    @Autowired
    public FileMetadataServiceImpl(FileMetadataRepository fileMetadataRepository) {
        this.fileMetadataRepository = fileMetadataRepository;
    }

    @Override
    public FileMetadata createFile(String fileName, String contentType, long size, UUID ownerId) {
        // Бизнес-правило: валидация входных данных
        validateFileData(fileName, contentType, size);

        // Бизнес-правило: генерация storage key
        String storageKey = generateStorageKey(fileName, ownerId);

        // Создаем FileMetadata
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileName(fileName);
        fileMetadata.setContentType(contentType);
        fileMetadata.setSize(size);
        fileMetadata.setStatus(Status.UPLOADED);
        fileMetadata.setStorageKey(storageKey);

        // Создаем минимальный объект User с только ID
        var owner = new com.yaser.metadata_service.entity.User();
        owner.setId(ownerId);
        fileMetadata.setOwner(owner);

        return fileMetadataRepository.save(fileMetadata);
    }

        @Override
    @Transactional(readOnly = true)
    public FileMetadata getFileById(UUID fileId) {
        return fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("File not found with id: " + fileId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileMetadata> getFilesByOwner(UUID ownerId) {
        return fileMetadataRepository.findByOwnerId(ownerId);
    }

    @Override
    public void deleteFile(UUID fileId) {
        if (!fileMetadataRepository.existsById(fileId)) {
            throw new EntityNotFoundException("File not found with id: " + fileId);
        }

        fileMetadataRepository.deleteById(fileId);
    }

    @Override
    public FileMetadata updateFileStatus(UUID fileId, Status status) {
        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("File not found with id: " + fileId));

        // Бизнес-правило: валидация перехода статусов
        validateStatusTransition(fileMetadata.getStatus(), status);

        fileMetadata.setStatus(status);

        return fileMetadataRepository.save(fileMetadata);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID fileId) {
        return fileMetadataRepository.existsById(fileId);
    }

    /**
     * Бизнес-правило: валидация данных файла
     */
    private void validateFileData(String fileName, String contentType, long size) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name is required");
        }

        if (fileName.length() > 255) {
            throw new IllegalArgumentException("File name cannot exceed 255 characters");
        }

        if (contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("Content type is required");
        }

        if (contentType.length() > 100) {
            throw new IllegalArgumentException("Content type cannot exceed 100 characters");
        }

        if (size <= 0) {
            throw new IllegalArgumentException("File size must be positive");
        }
    }

    /**
     * Бизнес-правило: генерация storage key
     */
    private String generateStorageKey(String fileName, UUID ownerId) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String fileExtension = extractFileExtension(fileName);

        return String.format("users/%s/files/%s_%s%s",
                ownerId,
                timestamp,
                uniqueId,
                fileExtension.isEmpty() ? "" : "." + fileExtension
        );
    }

    /**
     * Бизнес-правило: извлечение расширения файла
     */
    private String extractFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Бизнес-правило: валидация перехода статусов
     */
    private void validateStatusTransition(Status currentStatus, Status newStatus) {
        switch (currentStatus) {
            case UPLOADED -> {
                if (newStatus != Status.PROCESSING) {
                    throw new IllegalStateException(
                            String.format("UPLOADED files can only transition to PROCESSING, not to %s", newStatus)
                    );
                }
            }
            case PROCESSING -> {
                if (newStatus != Status.READY && newStatus != Status.FAILED) {
                    throw new IllegalStateException(
                            String.format("PROCESSING files can only transition to READY or FAILED, not to %s", newStatus)
                    );
                }
            }
            case READY, FAILED -> {
                throw new IllegalStateException(
                        String.format("Cannot change status from %s - it's a final status", currentStatus)
                );
            }
        }
    }

    /**
     * Дополнительные доменные методы
     */
    @Override
    @Transactional(readOnly = true)
    public List<FileMetadata> getFilesByStatus(Status status) {
        return fileMetadataRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countFilesByOwner(UUID ownerId) {
        return fileMetadataRepository.countByOwnerId(ownerId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countFilesByStatus(Status status) {
        return fileMetadataRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAllFiles() {
        return fileMetadataRepository.count();
    }

    @Override
    public FileMetadata updateStorageKey(UUID fileId, String newStorageKey) {
        if (newStorageKey == null || newStorageKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Storage key cannot be empty");
        }

        if (newStorageKey.length() > 500) {
            throw new IllegalArgumentException("Storage key cannot exceed 500 characters");
        }

        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("File not found with id: " + fileId));

        if (fileMetadataRepository.existsByStorageKeyAndIdNot(newStorageKey, fileId)) {
            throw new IllegalStateException("Storage key already exists: " + newStorageKey);
        }

        fileMetadata.setStorageKey(newStorageKey);

        return fileMetadataRepository.save(fileMetadata);
    }
}