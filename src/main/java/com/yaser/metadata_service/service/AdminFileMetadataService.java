package com.yaser.metadata_service.service;

import com.yaser.metadata_service.dto.FileMetadataResponseDTO;
import com.yaser.metadata_service.dto.UpdateFileStatusRequestDTO;
import com.yaser.metadata_service.entity.FileMetadata;
import com.yaser.metadata_service.entity.Status;
import com.yaser.metadata_service.entity.User;
import com.yaser.metadata_service.mapper.FileMetadataMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@Transactional
public class AdminFileMetadataService {

    private final FileMetadataService fileMetadataService;
    private final FileMetadataMapper fileMetadataMapper;
    private final UserAccessService userAccessService;

    /**
     * Получение файлов по владельцу (административная функция)
     */
    @Transactional(readOnly = true)
    public List<FileMetadataResponseDTO> getFilesByOwner(UUID ownerId, UUID currentUserId) {
        log.info("AdminService: Getting files for owner: {} by admin: {}", ownerId, currentUserId);

        // 1. Проверка прав администратора
        User currentUser = userAccessService.getUserOrThrow(currentUserId);
        userAccessService.validateIsAdmin(currentUser);

        // 2. Проверка существования запрашиваемого пользователя
        userAccessService.getUserOrThrow(ownerId);

        // 3. Вызов Domain Service
        List<FileMetadata> files = fileMetadataService.getFilesByOwner(ownerId);

        // 4. Логирование
        log.info("AdminService: Retrieved {} files for owner: {}", files.size(), ownerId);

        // 5. Маппинг результата
        return files.stream()
                .map(fileMetadataMapper::toResponseDTO)
                .toList();
    }

    /**
     * Административное обновление статуса файла
     */
    public FileMetadataResponseDTO updateFileStatus(UUID fileId, @Valid UpdateFileStatusRequestDTO request, UUID currentUserId) {
        log.info("AdminService: Admin updating file {} status to {}", fileId, request.getStatus());

        // 1. Проверка прав администратора
        User currentUser = userAccessService.getUserOrThrow(currentUserId);
        userAccessService.validateIsAdmin(currentUser);

        // 2. Проверка существования файла
        if (!fileMetadataService.existsById(fileId)) {
            throw new jakarta.persistence.EntityNotFoundException("File not found with id: " + fileId);
        }

        // 3. Конвертация статуса
        Status newStatus = Status.valueOf(request.getStatus().toUpperCase());

        // 4. Вызов Domain Service
        FileMetadata updatedFile = fileMetadataService.updateFileStatus(fileId, newStatus);

        // 5. Логирование
        log.info("AdminService: Admin file status updated - ID: {}, Status: {}, Updated by: {}",
                fileId, newStatus, currentUserId);

        // 6. Маппинг результата
        return fileMetadataMapper.toResponseDTO(updatedFile);
    }

    /**
     * Получение файлов по статусу
     */
    @Transactional(readOnly = true)
    public List<FileMetadataResponseDTO> getFilesByStatus(String status, UUID currentUserId) {
        log.info("AdminService: Getting files by status: {}", status);

        // 1. Проверка прав администратора
        User currentUser = userAccessService.getUserOrThrow(currentUserId);
        userAccessService.validateIsAdmin(currentUser);

        // 2. Конвертация статуса
        Status statusEnum = Status.valueOf(status.toUpperCase());

        // 3. Вызов Domain Service
        List<FileMetadata> files = fileMetadataService.getFilesByStatus(statusEnum);

        // 4. Логирование
        log.info("AdminService: Retrieved {} files with status: {}", files.size(), status);

        // 5. Маппинг результата
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

        // 1. Проверка прав администратора
        User currentUser = userAccessService.getUserOrThrow(currentUserId);
        userAccessService.validateIsAdmin(currentUser);

        // 2. Сбор статистики через Domain Service
        long totalFiles = fileMetadataService.countAllFiles();
        long uploadedFiles = fileMetadataService.countFilesByStatus(Status.UPLOADED);
        long processingFiles = fileMetadataService.countFilesByStatus(Status.PROCESSING);
        long readyFiles = fileMetadataService.countFilesByStatus(Status.READY);
        long failedFiles = fileMetadataService.countFilesByStatus(Status.FAILED);

        // 3. Логирование
        log.info("AdminService: Statistics retrieved - Total: {}, UPLOADED: {}, PROCESSING: {}, READY: {}, FAILED: {}",
                totalFiles, uploadedFiles, processingFiles, readyFiles, failedFiles);

        // 4. Возврат DTO со статистикой
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

        // 1. Проверка прав администратора
        User currentUser = userAccessService.getUserOrThrow(currentUserId);
        userAccessService.validateIsAdmin(currentUser);

        // 2. Получаем информацию о файле для логирования
        FileMetadata fileMetadata = fileMetadataService.getFileById(fileId);

        // 3. Вызов Domain Service
        fileMetadataService.deleteFile(fileId);

        // 4. Логирование
        log.info("AdminService: File deleted by admin - ID: {}, Name: {}, Owner: {}, Deleted by: {}",
                fileId, fileMetadata.getFileName(), fileMetadata.getOwner().getId(), currentUserId);
    }

    /**
     * Обновление storage key (административная функция)
     */
    public FileMetadataResponseDTO updateStorageKey(UUID fileId, String newStorageKey, UUID currentUserId) {
        log.info("AdminService: Admin updating storage key for file: {}", fileId);

        // 1. Проверка прав администратора
        User currentUser = userAccessService.getUserOrThrow(currentUserId);
        userAccessService.validateIsAdmin(currentUser);

        // 2. Вызов Domain Service
        FileMetadata updatedFile = fileMetadataService.updateStorageKey(fileId, newStorageKey);

        // 3. Логирование
        log.info("AdminService: Storage key updated for file {} by admin {}", fileId, currentUserId);

        // 4. Маппинг результата
        return fileMetadataMapper.toResponseDTO(updatedFile);
    }

    /**
     * DTO для статистики файлов (административная)
     */
    public record AdminFileStatisticsDTO(
            long totalFiles,
            long uploadedFiles,
            long processingFiles,
            long readyFiles,
            long failedFiles
    ) {
        public static AdminFileStatisticsDTOBuilder builder() {
            return new AdminFileStatisticsDTOBuilder();
        }

        public static class AdminFileStatisticsDTOBuilder {
            private long totalFiles;
            private long uploadedFiles;
            private long processingFiles;
            private long readyFiles;
            private long failedFiles;

            public AdminFileStatisticsDTOBuilder totalFiles(long totalFiles) {
                this.totalFiles = totalFiles;
                return this;
            }

            public AdminFileStatisticsDTOBuilder uploadedFiles(long uploadedFiles) {
                this.uploadedFiles = uploadedFiles;
                return this;
            }

            public AdminFileStatisticsDTOBuilder processingFiles(long processingFiles) {
                this.processingFiles = processingFiles;
                return this;
            }

            public AdminFileStatisticsDTOBuilder readyFiles(long readyFiles) {
                this.readyFiles = readyFiles;
                return this;
            }

            public AdminFileStatisticsDTOBuilder failedFiles(long failedFiles) {
                this.failedFiles = failedFiles;
                return this;
            }

            public AdminFileStatisticsDTO build() {
                return new AdminFileStatisticsDTO(
                        totalFiles, uploadedFiles, processingFiles, readyFiles, failedFiles
                );
            }
        }
    }
}
