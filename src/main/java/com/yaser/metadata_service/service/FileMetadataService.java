package com.yaser.metadata_service.service;

import com.yaser.metadata_service.entity.FileMetadata;
import com.yaser.metadata_service.entity.Status;

import java.util.List;
import java.util.UUID;

public interface FileMetadataService {
    // Основные операции
    FileMetadata createFile(String fileName, String contentType, long size, UUID ownerId);
    FileMetadata getFileById(UUID fileId);
    List<FileMetadata> getFilesByOwner(UUID ownerId);
    void deleteFile(UUID fileId);
    FileMetadata updateFileStatus(UUID fileId, Status status);

    // Вспомогательные операции
    boolean existsById(UUID fileId);

    // Дополнительные доменные операции
    List<FileMetadata> getFilesByStatus(Status status);
    long countFilesByOwner(UUID ownerId);
    long countFilesByStatus(Status status);
    long countAllFiles();
    FileMetadata updateStorageKey(UUID fileId, String newStorageKey);
}