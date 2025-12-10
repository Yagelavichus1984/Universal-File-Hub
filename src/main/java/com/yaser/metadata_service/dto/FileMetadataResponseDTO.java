package com.yaser.metadata_service.dto;

import com.yaser.metadata_service.entity.Status;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class FileMetadataResponseDTO {

    private UUID id;
    private String fileName;
    private String contentType;
    private Long size;
    private UUID ownerId;
    private String ownerUsername; // Добавляем имя владельца для удобства
    private Status status;
    private String storageKey;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
