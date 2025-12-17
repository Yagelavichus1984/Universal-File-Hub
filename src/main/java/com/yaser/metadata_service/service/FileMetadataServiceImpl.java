package com.yaser.metadata_service.service;

import com.yaser.metadata_service.entity.FileMetadata;
import com.yaser.metadata_service.command.CreateFileCommand;
import com.yaser.metadata_service.entity.Status;
import com.yaser.metadata_service.entity.User;
import com.yaser.metadata_service.repository.FileMetadataRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FileMetadataServiceImpl implements FileMetadataService {

    private final FileMetadataRepository fileMetadataRepository;

    @Override
    public FileMetadata createFile(CreateFileCommand command, User owner) {
        log.info("Creating file metadata for owner: {}", owner.getId());

        // Бизнес-правило: валидация команды создания
        validateCreateCommand(command);

        // Бизнес-правило: генерация storage key
        String storageKey = generateStorageKey(command.getFilename(), owner.getId());

        // Бизнес-правило: создание метаданных
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileName(command.getFilename());
        fileMetadata.setContentType(command.getContentType());
        fileMetadata.setSize(command.getSize());
        fileMetadata.setOwner(owner); // User entity передается извне
        fileMetadata.setStatus(Status.UPLOADED); // Бизнес-правило: начальный статус
        fileMetadata.setStorageKey(storageKey); // Бизнес-правило: уникальный ключ хранилища

        // createdAt и updatedAt устанавливаются автоматически через @PrePersist

        FileMetadata savedFile = fileMetadataRepository.save(fileMetadata);
        log.info("File metadata created with id: {}", savedFile.getId());

        return savedFile;
    }

    @Override
    @Transactional(readOnly = true)
    public FileMetadata getFileById(UUID fileId) {
        log.info("Getting file metadata by id: {}", fileId);

        return fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("File not found with id: " + fileId));
    }

    @Transactional(readOnly = true)
    @Override
    public List<FileMetadata> getFilesByOwner(User owner) {
        log.info("Getting files metadata for owner: {}", owner.getId());

        // Бизнес-операция: получение файлов по владельцу
        return fileMetadataRepository.findByOwner(owner);
    }

    @Override
    public void deleteFile(UUID fileId) {
        log.info("Deleting file metadata: {}", fileId);

        // Бизнес-правило: проверяем существование файла перед удалением
        if (!fileMetadataRepository.existsById(fileId)) {
            throw new EntityNotFoundException("File not found with id: " + fileId);
        }

        // БЕЗ ПРОВЕРКИ ВЛАДЕЛЬЦА - это ответственность вызывающего кода
        fileMetadataRepository.deleteById(fileId);
        log.info("File metadata deleted: {}", fileId);
    }

    @Override
    public FileMetadata updateFileStatus(UUID fileId, Status status) {
        log.info("Updating file {} status to {}", fileId, status);

        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("File not found with id: " + fileId));

        // Бизнес-правила: валидация перехода статусов
        validateStatusTransition(fileMetadata.getStatus(), status);

        // Бизнес-правило: обновление статуса
        fileMetadata.setStatus(status);

        // updatedAt обновится автоматически через @PreUpdate

        FileMetadata updatedFile = fileMetadataRepository.save(fileMetadata);
        log.info("File {} status updated to {}", fileId, status);

        return updatedFile;
    }

    /**
     * Бизнес-правило: валидация команды создания файла
     */
    private void validateCreateCommand(CreateFileCommand command) {
        if (command.getFilename() == null || command.getFilename().trim().isEmpty()) {
            throw new IllegalArgumentException("File name is required");
        }

        if (command.getFilename().length() > 255) {
            throw new IllegalArgumentException("File name cannot exceed 255 characters");
        }

        if (command.getContentType() == null || command.getContentType().trim().isEmpty()) {
            throw new IllegalArgumentException("Content type is required");
        }

        if (command.getContentType().length() > 100) {
            throw new IllegalArgumentException("Content type cannot exceed 100 characters");
        }

        if (command.getSize() < 0) {
            throw new IllegalArgumentException("File size cannot be negative");
        }

        // Бизнес-правило: максимальный размер файла
        if (command.getSize() > 10L * 1024 * 1024 * 1024) { // 10GB
            throw new IllegalArgumentException("File size exceeds maximum limit of 10GB");
        }

        // Бизнес-правило: валидация расширения файла
        validateFileExtension(command.getFilename(), command.getContentType());
    }

    /**
     * Бизнес-правило: генерация уникального storage key
     */
    private String generateStorageKey(String filename, UUID ownerId) {
        // Бизнес-правило: формат ключа хранилища
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String fileExtension = extractFileExtension(filename);

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
    private String extractFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Бизнес-правило: валидация соответствия расширения файла и content type
     */
    private void validateFileExtension(String filename, String contentType) {
        String extension = extractFileExtension(filename);

        if (extension.isEmpty()) {
            log.warn("File {} has no extension", filename);
            return;
        }

        // Бизнес-правила проверки соответствия
        switch (extension) {
            case "pdf" -> {
                if (!"application/pdf".equals(contentType)) {
                    log.warn("Filename has .pdf extension but content type is {}", contentType);
                }
            }
            case "jpg", "jpeg" -> {
                if (!"image/jpeg".equals(contentType)) {
                    log.warn("Filename has .{} extension but content type is {}", extension, contentType);
                }
            }
            case "png" -> {
                if (!"image/png".equals(contentType)) {
                    log.warn("Filename has .png extension but content type is {}", contentType);
                }
            }
            case "txt" -> {
                if (!"text/plain".equals(contentType)) {
                    log.warn("Filename has .txt extension but content type is {}", contentType);
                }
            }
            // Можно добавить другие правила
        }
    }

    /**
     * Бизнес-правило: валидация допустимых переходов между статусами
     * UPLOADED -> PROCESSING -> READY/FAILED
     */
    private void validateStatusTransition(Status currentStatus, Status newStatus) {
        // Бизнес-правила переходов статусов
        switch (currentStatus) {
            case UPLOADED -> {
                // Из UPLOADED можно перейти только в PROCESSING
                if (newStatus != Status.PROCESSING) {
                    throw new IllegalStateException(
                            String.format("UPLOADED files can only transition to PROCESSING, not to %s", newStatus)
                    );
                }
            }
            case PROCESSING -> {
                // Из PROCESSING можно перейти в READY или FAILED
                if (newStatus != Status.READY && newStatus != Status.FAILED) {
                    throw new IllegalStateException(
                            String.format("PROCESSING files can only transition to READY or FAILED, not to %s", newStatus)
                    );
                }
            }
            case READY, FAILED -> {
                // READY и FAILED - конечные статусы, нельзя изменить
                throw new IllegalStateException(
                        String.format("Cannot change status from %s - it's a final status", currentStatus)
                );
            }
        }

        log.debug("Valid status transition: {} -> {}", currentStatus, newStatus);
    }

    /**
     * Дополнительный доменный метод: получение файлов по статусу
     */
    @Transactional(readOnly = true)
    public List<FileMetadata> getFilesByStatus(Status status) {
        log.info("Getting files with status: {}", status);
        return fileMetadataRepository.findByStatus(status);
    }

    /**
     * Дополнительный доменный метод: получение устаревших файлов
     * Бизнес-правило: файлы в статусе UPLOADED, созданные более 24 часов назад
     */
    @Transactional(readOnly = true)
    public List<FileMetadata> getStaleUploadedFiles() {
        log.info("Getting stale uploaded files");
        OffsetDateTime threshold = OffsetDateTime.now().minusHours(24);
        return fileMetadataRepository.findByStatusAndCreatedAtBefore(Status.UPLOADED, threshold);
    }

    /**
     * Дополнительный доменный метод: обновление storage key
     * Бизнес-правило: можно обновить ключ хранилища при миграции
     */
    public FileMetadata updateStorageKey(UUID fileId, String newStorageKey) {
        log.info("Updating storage key for file: {}", fileId);

        if (newStorageKey == null || newStorageKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Storage key cannot be empty");
        }

        if (newStorageKey.length() > 500) {
            throw new IllegalArgumentException("Storage key cannot exceed 500 characters");
        }

        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("File not found with id: " + fileId));

        // Бизнес-правило: проверка уникальности storage key
        if (fileMetadataRepository.existsByStorageKeyAndIdNot(newStorageKey, fileId)) {
            throw new IllegalStateException("Storage key already exists: " + newStorageKey);
        }

        fileMetadata.setStorageKey(newStorageKey);

        return fileMetadataRepository.save(fileMetadata);
    }
}