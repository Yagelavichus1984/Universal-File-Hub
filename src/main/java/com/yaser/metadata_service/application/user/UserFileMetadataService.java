package com.yaser.metadata_service.application.user;

import com.yaser.metadata_service.application.access.UserAccessService;
import com.yaser.metadata_service.dto.FileMetadataResponseDTO;
import com.yaser.metadata_service.dto.FileUploadRequestDTO;
import com.yaser.metadata_service.dto.UpdateFileStatusRequestDTO;
import com.yaser.metadata_service.entity.FileMetadata;
import com.yaser.metadata_service.entity.Status;
import com.yaser.metadata_service.entity.User;
import com.yaser.metadata_service.exception.AccessDeniedException;
import com.yaser.metadata_service.mapper.FileMetadataMapper;
import com.yaser.metadata_service.repository.UserRepository;
import com.yaser.metadata_service.service.FileMetadataService;
import jakarta.persistence.EntityNotFoundException;
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
public class UserFileMetadataService {

    private static final Logger log = LoggerFactory.getLogger(UserFileMetadataService.class);

    private final FileMetadataService fileMetadataService;
    private final FileMetadataMapper fileMetadataMapper;
    private final UserAccessService userAccessService;
    private final UserRepository userRepository;

    @Autowired
    public UserFileMetadataService(
            FileMetadataService fileMetadataService,
            FileMetadataMapper fileMetadataMapper,
            UserAccessService userAccessService,
            UserRepository userRepository) {
        this.fileMetadataService = fileMetadataService;
        this.fileMetadataMapper = fileMetadataMapper;
        this.userAccessService = userAccessService;
        this.userRepository = userRepository;
    }

    public FileMetadataResponseDTO createFile(@Valid FileUploadRequestDTO requestDTO, UUID currentUserId) {
        log.info("UserService: Creating file '{}' for user: {}", requestDTO.getFileName(), currentUserId);

        // Упрощенная проверка
        if (!requestDTO.getOwnerId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only create files for yourself");
        }

        // Вызов Domain Service
        FileMetadata fileMetadata = fileMetadataService.createFile(
                requestDTO.getFileName(),
                requestDTO.getContentType(),
                requestDTO.getSize(),
                currentUserId
        );

        log.info("UserService: File created successfully - ID: {}", fileMetadata.getId());

        return fileMetadataMapper.toResponseDTO(fileMetadata);
    }

    @Transactional(readOnly = true)
    public FileMetadataResponseDTO getFileById(UUID fileId, UUID currentUserId) {
        log.info("UserService: Getting file by ID: {}", fileId);

        FileMetadata fileMetadata = fileMetadataService.getFileById(fileId);

        // Упрощенная проверка владения
        if (!fileMetadata.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only access your own files");
        }

        return fileMetadataMapper.toResponseDTO(fileMetadata);
    }

    @Transactional(readOnly = true)
    public List<FileMetadataResponseDTO> getMyFiles(UUID currentUserId) {
        log.info("UserService: Getting files for current user: {}", currentUserId);

        List<FileMetadata> files = fileMetadataService.getFilesByOwner(currentUserId);
        log.info("UserService: Retrieved {} files", files.size());

        return files.stream()
                .map(fileMetadataMapper::toResponseDTO)
                .toList();
    }

    public void deleteFile(UUID fileId, UUID currentUserId) {
        log.info("UserService: Deleting file: {}", fileId);

        FileMetadata fileMetadata = fileMetadataService.getFileById(fileId);

        if (!fileMetadata.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only delete your own files");
        }

        fileMetadataService.deleteFile(fileId);
        log.info("UserService: File deleted successfully");
    }

    public FileMetadataResponseDTO updateFileStatus(UUID fileId, @Valid UpdateFileStatusRequestDTO request, UUID currentUserId) {
        log.info("UserService: Updating file {} status to {}", fileId, request.getStatus());

        FileMetadata fileMetadata = fileMetadataService.getFileById(fileId);

        if (!fileMetadata.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only update your own files");
        }

        Status newStatus;
        try {
            newStatus = Status.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + request.getStatus());
        }

        FileMetadata updatedFile = fileMetadataService.updateFileStatus(fileId, newStatus);
        log.info("UserService: File status updated");

        return fileMetadataMapper.toResponseDTO(updatedFile);
    }

    /**
     * Получение статистики файлов пользователя
     */
    @Transactional(readOnly = true)
    public UserFileStatisticsDTO getUserFileStatistics(UUID currentUserId) {
        log.info("UserService: Getting file statistics for user: {}", currentUserId);

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

        log.info("UserService: Statistics retrieved for user {} - Total: {}", currentUserId, totalFiles);

        return new UserFileStatisticsDTO(
                totalFiles, uploadedFiles, processingFiles, readyFiles, failedFiles
        );
    }

    /**
     * DTO для статистики файлов пользователя
     */
    public static class UserFileStatisticsDTO {
        private final long totalFiles;
        private final long uploadedFiles;
        private final long processingFiles;
        private final long readyFiles;
        private final long failedFiles;

        public UserFileStatisticsDTO(long totalFiles, long uploadedFiles, long processingFiles,
                                     long readyFiles, long failedFiles) {
            this.totalFiles = totalFiles;
            this.uploadedFiles = uploadedFiles;
            this.processingFiles = processingFiles;
            this.readyFiles = readyFiles;
            this.failedFiles = failedFiles;
        }

        // Геттеры
        public long getTotalFiles() { return totalFiles; }
        public long getUploadedFiles() { return uploadedFiles; }
        public long getProcessingFiles() { return processingFiles; }
        public long getReadyFiles() { return readyFiles; }
        public long getFailedFiles() { return failedFiles; }

        // Билдер для удобства
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

            public UserFileStatisticsDTO build() {
                return new UserFileStatisticsDTO(
                        totalFiles, uploadedFiles, processingFiles, readyFiles, failedFiles
                );
            }
        }
    }
}