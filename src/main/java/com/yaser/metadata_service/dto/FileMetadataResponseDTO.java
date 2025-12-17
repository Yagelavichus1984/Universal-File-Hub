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
    private String ownerUsername;
    private Status status;
    private String storageKey;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    // Ручные геттеры и сеттеры если нужно:
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}