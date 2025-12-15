package com.yaser.metadata_service.service;

import com.yaser.metadata_service.dto.FileMetadataResponseDTO;
import com.yaser.metadata_service.dto.FileUploadRequestDTO;
import java.util.List;
import java.util.UUID;

public interface FileMetadataService {

    FileMetadataResponseDTO createFile(FileUploadRequestDTO requestDTO, UUID ownerId);

    FileMetadataResponseDTO getFileById(UUID fileId);

    List<FileMetadataResponseDTO> getFilesByOwner(UUID ownerId);

    void deleteFile(UUID fileId, UUID ownerId);

    FileMetadataResponseDTO updateFileStatus(UUID fileId, String status, UUID ownerId);
}