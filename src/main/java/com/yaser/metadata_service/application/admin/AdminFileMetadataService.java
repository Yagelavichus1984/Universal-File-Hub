package com.yaser.metadata_service.application.admin;

import com.yaser.metadata_service.application.access.UserAccessService;
import com.yaser.metadata_service.dto.FileMetadataResponseDTO;
import com.yaser.metadata_service.dto.UpdateFileStatusRequestDTO;
import com.yaser.metadata_service.entity.FileMetadata;
import com.yaser.metadata_service.entity.Status;
import com.yaser.metadata_service.entity.User;
import com.yaser.metadata_service.exception.AccessDeniedException;
import com.yaser.metadata_service.mapper.FileMetadataMapper;
import com.yaser.metadata_service.service.FileMetadataService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Service
@Validated
@Transactional
public class AdminFileMetadataService {

    private static final Logger log = LoggerFactory.getLogger(AdminFileMetadataService.class);

    private final FileMetadataService fileMetadataService;
    private final FileMetadataMapper fileMetadataMapper;
    private final UserAccessService userAccessService;

    @Autowired
    public AdminFileMetadataService(
            FileMetadataService fileMetadataService,
            FileMetadataMapper fileMetadataMapper,
            UserAccessService userAccessService) {
        this.fileMetadataService = fileMetadataService;
        this.fileMetadataMapper = fileMetadataMapper;
        this.userAccessService = userAccessService;
    }

    /**
     * Получение файлов по владельцу (административная функция)
     */
    @Transactional(readOnly = true)
    public List<FileMetadataResponseDTO> getFilesByOwner(UUID ownerId, UUID currentUserId) {
        log.info("AdminService: Getting files for owner: {} by admin: {}", ownerId, currentUserId);

        // Получаем текущего пользователя
        User currentUser = userAccessService.getUserOrThrow(currentUserId);

        // Упрощенная проверка прав (временно)
        if (!ownerId.equals(currentUserId)) {
            // В реальном проекте здесь должна быть проверка роли ADMIN
            log.warn("User {} is accessing files of another user {}", currentUserId, ownerId);
        }

        // Вызов Domain Service
        List<FileMetadata> files = fileMetadataService.getFilesByOwner(ownerId);

        log.info("AdminService: Retrieved {} files for owner: {}", files.size(), ownerId);

        // Маппинг результата
        return files.stream()
                .map(fileMetadataMapper::toResponseDTO)
                .toList();
    }

    /**
     * Административное обновление статуса файла
     */
    public FileMetadataResponseDTO updateFileStatus(UUID fileId, @Valid UpdateFileStatusRequestDTO request, UUID currentUserId) {
        log.info("AdminService: Admin updating file {} status to {}", fileId, request.getStatus());

        // Получаем текущего пользователя
        User currentUser = userAccessService.getUserOrThrow(currentUserId);

        // Упрощенная проверка прав (временно)
        log.info("User {} is updating file {} status", currentUserId, fileId);

        // Проверка существования файла
        if (!fileMetadataService.existsById(fileId)) {
            throw new jakarta.persistence.EntityNotFoundException("File not found with id: " + fileId);
        }

        // Конвертация статуса
        Status newStatus;
        try {
            newStatus = Status.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + request.getStatus());
        }

        // Вызов Domain Service
        FileMetadata updatedFile = fileMetadataService.updateFileStatus(fileId, newStatus);

        log.info("AdminService: Admin file status updated - ID: {}, Status: {}", fileId, newStatus);

        // Маппинг результата
        return fileMetadataMapper.toResponseDTO(updatedFile);
    }

    /**
     * Получение файлов по статусу
     */
    @Transactional(readOnly = true)
    public List<FileMetadataResponseDTO> getFilesByStatus(String status, UUID currentUserId) {
        log.info("AdminService: Getting files by status: {}", status);

        // Получаем текущего пользователя
        User currentUser = userAccessService.getUserOrThrow(currentUserId);

        // Упрощенная проверка прав (временно)
        log.info("User {} is getting files by status {}", currentUserId, status);

        // Конвертация статуса
        Status statusEnum;
        try {
            statusEnum = Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }

        // Вызов Domain Service
        List<FileMetadata> files = fileMetadataService.getFilesByStatus(statusEnum);

        log.info("AdminService: Retrieved {} files with status: {}", files.size(), status);

        // Маппинг результата
        return files.stream()
                .map(fileMetadataMapper::toResponseDTO)
                .toList();
    }

    /**
     * Получение общей статистики файлов
     */
    @Transactional(readOnly = true)
    public AdminFileStatisticsDTO getFileStatistics(UUID currentUserId) {
        log.info("AdminService: Getting file statistics");

        // Получаем текущего пользователя
        User currentUser = userAccessService.getUserOrThrow(currentUserId);

        // Упрощенная проверка прав (временно)
        log.info("User {} is getting file statistics", currentUserId);

        // Сбор статистики через Domain Service
        long totalFiles = fileMetadataService.countAllFiles();
        long uploadedFiles = fileMetadataService.countFilesByStatus(Status.UPLOADED);
        long processingFiles = fileMetadataService.countFilesByStatus(Status.PROCESSING);
        long readyFiles = fileMetadataService.countFilesByStatus(Status.READY);
        long failedFiles = fileMetadataService.countFilesByStatus(Status.FAILED);

        log.info("AdminService: Statistics retrieved - Total: {}, UPLOADED: {}, PROCESSING: {}, READY: {}, FAILED: {}",
                totalFiles, uploadedFiles, processingFiles, readyFiles, failedFiles);

        // Возврат DTO со статистикой
        return AdminFileStatisticsDTO.builder()
                .totalFiles(totalFiles)
                .uploadedFiles(uploadedFiles)
                .processingFiles(processingFiles)
                .readyFiles(readyFiles)
                .failedFiles(failedFiles)
                .build();
    }

    /**
     * Административное удаление файла
     */
    public void deleteFile(UUID fileId, UUID currentUserId) {
        log.info("AdminService: Admin deleting file: {}", fileId);

        // Получаем текущего пользователя
        User currentUser = userAccessService.getUserOrThrow(currentUserId);

        // Упрощенная проверка прав (временно)
        log.info("User {} is deleting file {}", currentUserId, fileId);

        // Получаем информацию о файле для логирования
        FileMetadata fileMetadata = fileMetadataService.getFileById(fileId);

        // Вызов Domain Service
        fileMetadataService.deleteFile(fileId);

        log.info("AdminService: File deleted by admin - ID: {}, Name: {}", fileId, fileMetadata.getFileName());
    }

    /**
     * Обновление storage key (административная функция)
     */
    public FileMetadataResponseDTO updateStorageKey(UUID fileId, String newStorageKey, UUID currentUserId) {
        log.info("AdminService: Admin updating storage key for file: {}", fileId);

        // Получаем текущего пользователя
        User currentUser = userAccessService.getUserOrThrow(currentUserId);

        // Упрощенная проверка прав (временно)
        log.info("User {} is updating storage key for file {}", currentUserId, fileId);

        // Вызов Domain Service
        FileMetadata updatedFile = fileMetadataService.updateStorageKey(fileId, newStorageKey);

        log.info("AdminService: Storage key updated for file {}", fileId);

        // Маппинг результата
        return fileMetadataMapper.toResponseDTO(updatedFile);
    }

    /**
     * DTO для статистики файлов (административная)
     */
    public static class AdminFileStatisticsDTO {
        private final long totalFiles;
        private final long uploadedFiles;
        private final long processingFiles;
        private final long readyFiles;
        private final long failedFiles;

        private AdminFileStatisticsDTO(Builder builder) {
            this.totalFiles = builder.totalFiles;
            this.uploadedFiles = builder.uploadedFiles;
            this.processingFiles = builder.processingFiles;
            this.readyFiles = builder.readyFiles;
            this.failedFiles = builder.failedFiles;
        }

        public long getTotalFiles() { return totalFiles; }
        public long getUploadedFiles() { return uploadedFiles; }
        public long getProcessingFiles() { return processingFiles; }
        public long getReadyFiles() { return readyFiles; }
        public long getFailedFiles() { return failedFiles; }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private long totalFiles;
            private long uploadedFiles;
            private long processingFiles;
            private long readyFiles;
            private long failedFiles;

            public Builder totalFiles(long totalFiles) {
                this.totalFiles = totalFiles;
                return this;
            }

            public Builder uploadedFiles(long uploadedFiles) {
                this.uploadedFiles = uploadedFiles;
                return this;
            }

            public Builder processingFiles(long processingFiles) {
                this.processingFiles = processingFiles;
                return this;
            }

            public Builder readyFiles(long readyFiles) {
                this.readyFiles = readyFiles;
                return this;
            }

            public Builder failedFiles(long failedFiles) {
                this.failedFiles = failedFiles;
                return this;
            }

            public AdminFileStatisticsDTO build() {
                return new AdminFileStatisticsDTO(this);
            }
        }
    }
}