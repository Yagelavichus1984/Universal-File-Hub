package com.yaser.metadata_service.service;

import com.yaser.metadata_service.dto.FileMetadataResponseDTO;
import com.yaser.metadata_service.dto.FileUploadRequestDTO;
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
public class UserFileMetadataService {

    private final FileMetadataService fileMetadataService;
    private final FileMetadataMapper fileMetadataMapper;
    private final UserAccessService userAccessService;

    /**
     * Создание метаданных файла пользователем
     */
    public FileMetadataResponseDTO createFile(@Valid FileUploadRequestDTO requestDTO, UUID currentUserId) {
        log.info("UserService: Creating file '{}' for user: {}", requestDTO.getFileName(), currentUserId);

        // 1. Получаем пользователя
        User currentUser = userAccessService.getUserOrThrow(currentUserId);

        // 2. Проверка согласованности ownerId
        userAccessService.validateOwnerConsistency(requestDTO.getOwnerId(), currentUser);

        // 3. Проверка прав на загрузку файлов
        userAccessService.validateCanUploadFiles(currentUser);

        // 4. Вызов Domain Service
        FileMetadata fileMetadata = fileMetadataService.createFile(
                requestDTO.getFileName(),
                requestDTO.getContentType(),
                requestDTO.getSize(),
                currentUserId
        );

        // 5. Логирование
        log.info("UserService: File created successfully - ID: {}, Name: {}, Owner: {}",
                fileMetadata.getId(), fileMetadata.getFileName(), currentUserId);

        // 6. Маппинг результата
        return fileMetadataMapper.toResponseDTO(fileMetadata);
    }

    /**
     * Получение метаданных файла по ID (только свои файлы)
     */
    @Transactional(readOnly = true)
    public FileMetadataResponseDTO getFileById(UUID fileId, UUID currentUserId) {
        log.info("UserService: Getting file by ID: {}", fileId);

        // 1. Вызов Domain Service
        FileMetadata fileMetadata = fileMetadataService.getFileById(fileId);

        // 2. Получаем пользователя для проверки прав
        User currentUser = userAccessService.getUserOrThrow(currentUserId);

        // 3. Проверка владения файлом
        userAccessService.validateOwnership(currentUser, fileMetadata.getOwner().getId(), "file");

        // 4. Логирование
        log.debug("UserService: File retrieved - ID: {}", fileId);

        // 5. Маппинг результата
        return fileMetadataMapper.toResponseDTO(fileMetadata);
    }

    /**
     * Получение файлов текущего пользователя
     */
    @Transactional(readOnly = true)
    public List<FileMetadataResponseDTO> getMyFiles(UUID currentUserId) {
        log.info("UserService: Getting files for current user: {}", currentUserId);

        // 1. Проверка существования пользователя
        userAccessService.getUserOrThrow(currentUserId);

        // 2. Вызов Domain Service
        List<FileMetadata> files = fileMetadataService.getFilesByOwner(currentUserId);

        // 3. Логирование
        log.info("UserService: Retrieved {} files for user: {}", files.size(), currentUserId);

        // 4. Маппинг результата
        return files.stream()
                .map(fileMetadataMapper::toResponseDTO)
                .toList();
    }

    /**
     * Удаление файла пользователем
     */
    public void deleteFile(UUID fileId, UUID currentUserId) {
        log.info("UserService: Deleting file: {}", fileId);

        // 1. Получаем файл для проверки прав
        FileMetadata fileMetadata = fileMetadataService.getFileById(fileId);

        // 2. Получаем пользователя
        User currentUser = userAccessService.getUserOrThrow(currentUserId);

        // 3. Проверка владения файлом
        userAccessService.validateOwnership(currentUser, fileMetadata.getOwner().getId(), "file");

        // 4. Вызов Domain Service
        fileMetadataService.deleteFile(fileId);

        // 5. Логирование
        log.info("UserService: File deleted successfully - ID: {}, Name: {}",
                fileId, fileMetadata.getFileName());
    }

    /**
     * Обновление статуса файла пользователем
     */
    public FileMetadataResponseDTO updateFileStatus(UUID fileId, @Valid UpdateFileStatusRequestDTO request, UUID currentUserId) {
        log.info("UserService: Updating file {} status to {}", fileId, request.getStatus());

        // 1. Получаем файл для проверки прав
        FileMetadata fileMetadata = fileMetadataService.getFileById(fileId);

        // 2. Получаем пользователя
        User currentUser = userAccessService.getUserOrThrow(currentUserId);

        // 3. Проверка владения файлом
        userAccessService.validateOwnership(currentUser, fileMetadata.getOwner().getId(), "file");

        // 4. Конвертация статуса (валидация должна быть в контроллере)
        Status newStatus = Status.valueOf(request.getStatus().toUpperCase());

        // 5. Вызов Domain Service
        FileMetadata updatedFile = fileMetadataService.updateFileStatus(fileId, newStatus);

        // 6. Логирование
        log.info("UserService: File status updated - ID: {}, Name: {}, Status: {} -> {}",
                fileId, fileMetadata.getFileName(), fileMetadata.getStatus(), newStatus);

        // 7. Маппинг результата
        return fileMetadataMapper.toResponseDTO(updatedFile);
    }

    /**
     * Получение статистики файлов пользователя
     */
    @Transactional(readOnly = true)
    public UserFileStatisticsDTO getUserFileStatistics(UUID currentUserId) {
        log.info("UserService: Getting file statistics for user: {}", currentUserId);

        // 1. Проверка существования пользователя
        userAccessService.getUserOrThrow(currentUserId);

        // 2. Сбор статистики
        long totalFiles = fileMetadataService.countFilesByOwner(currentUserId);
        List<FileMetadata> allFiles = fileMetadataService.getFilesByOwner(currentUserId);

        long uploadedFiles = allFiles.stream()
                .filter(f -> f.getStatus() == Status.UPLOADED)
                .count();
        long processingFiles = allFiles.stream()
                .filter(f -> f.getStatus() == Status.PROCESSING)
                .count();
        long readyFiles = allFiles.stream()
                .filter(f -> f.getStatus() == Status.READY)
                .count();
        long failedFiles = allFiles.stream()
                .filter(f -> f.getStatus() == Status.FAILED)
                .count();

        // 3. Логирование
        log.info("UserService: Statistics retrieved for user {} - Total: {}", currentUserId, totalFiles);

        // 4. Возврат DTO
        return UserFileStatisticsDTO.builder()
                .totalFiles(totalFiles)
                .uploadedFiles(uploadedFiles)
                .processingFiles(processingFiles)
                .readyFiles(readyFiles)
                .failedFiles(failedFiles)
                .build();
    }

    /**
     * DTO для статистики файлов пользователя
     */
    public record UserFileStatisticsDTO(
            long totalFiles,
            long uploadedFiles,
            long processingFiles,
            long readyFiles,
            long failedFiles
    ) {
        public static UserFileStatisticsDTOBuilder builder() {
            return new UserFileStatisticsDTOBuilder();
        }

        public static class UserFileStatisticsDTOBuilder {
            private long totalFiles;
            private long uploadedFiles;
            private long processingFiles;
            private long readyFiles;
            private long failedFiles;

            public UserFileStatisticsDTOBuilder totalFiles(long totalFiles) {
                this.totalFiles = totalFiles;
                return this;
            }

            public UserFileStatisticsDTOBuilder uploadedFiles(long uploadedFiles) {
                this.uploadedFiles = uploadedFiles;
                return this;
            }

            public UserFileStatisticsDTOBuilder processingFiles(long processingFiles) {
                this.processingFiles = processingFiles;
                return this;
            }

            public UserFileStatisticsDTOBuilder readyFiles(long readyFiles) {
                this.readyFiles = readyFiles;
                return this;
            }

            public UserFileStatisticsDTOBuilder failedFiles(long failedFiles) {
                this.failedFiles = failedFiles;
                return this;
            }

            public UserFileStatisticsDTO build() {
                return new UserFileStatisticsDTO(
                        totalFiles, uploadedFiles, processingFiles, readyFiles, failedFiles
                );
            }
        }
    }
}
