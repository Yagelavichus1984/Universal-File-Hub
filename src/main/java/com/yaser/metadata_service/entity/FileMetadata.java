package com.yaser.metadata_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "file_metadata")
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.UPLOADED;

    @Column(name = "storage_key", nullable = false, unique = true, length = 500)
    private String storageKey;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    // === КОНСТРУКТОРЫ ===

    public FileMetadata() {
        // Конструктор по умолчанию для JPA
    }

    public FileMetadata(String fileName, String contentType, Long size, User owner, Status status, String storageKey) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.owner = owner;
        this.status = status;
        this.storageKey = storageKey;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    // === ГЕТТЕРЫ ===

    public UUID getId() {
        return this.id;
    }

    public String getFileName() {  // ← ЭТОТ МЕТОД НУЖЕН!
        return this.fileName;
    }

    public String getContentType() {
        return this.contentType;
    }

    public Long getSize() {
        return this.size;
    }

    public User getOwner() {  // ← ЭТОТ МЕТОД ТОЖЕ НУЖЕН!
        return this.owner;
    }

    public Status getStatus() {
        return this.status;
    }

    public String getStorageKey() {
        return this.storageKey;
    }

    public OffsetDateTime getCreatedAt() {
        return this.createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public Long getVersion() {
        return this.version;
    }

    // === СЕТТЕРЫ ===

    public void setId(UUID id) {
        this.id = id;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    // === БИЗНЕС-МЕТОДЫ ===

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // Метод для изменения статуса с валидацией
    public void changeStatus(Status newStatus) {
        // Валидация перехода статусов
        validateStatusTransition(this.status, newStatus);
        this.status = newStatus;
    }

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

    @Override
    public String toString() {
        return "FileMetadata{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", status=" + status +
                ", ownerId=" + (owner != null ? owner.getId() : "null") +
                '}';
    }
}