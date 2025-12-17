package com.yaser.metadata_service.repository;

import com.yaser.metadata_service.entity.FileMetadata;
import com.yaser.metadata_service.entity.Status;
import com.yaser.metadata_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {

    // === 1. Методы поиска ===

    Optional<FileMetadata> findByFileName(String fileName);

    // Поиск по владельцу (User entity)
    List<FileMetadata> findByOwner(User owner);

    // Поиск по статусу
    List<FileMetadata> findByStatus(Status status);

    List<FileMetadata> findByStatusAndCreatedAtBefore(Status status, OffsetDateTime threshold);

    Optional<FileMetadata> findByStorageKey(String storageKey);

    boolean existsByStorageKeyAndIdNot(String storageKey, UUID fileId);

    List<FileMetadata> findByContentType(String contentType);

    List<FileMetadata> findBySizeGreaterThan(Long size);

    List<FileMetadata> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end);

    List<FileMetadata> findByOwnerAndStatus(User owner, Status status);

    // === 2. Методы подсчета ===

    // Подсчет файлов по владельцу (User entity)
    long countByOwner(User owner);

    // Подсчет файлов по статусу
    long countByStatus(Status status);

    // === 3. Методы удаления ===

    void deleteByOwner(User owner);

    // === 4. Кастомные запросы через @Query ===

    // Поиск файлов по ID владельца (через @Query)
    @Query("SELECT f FROM FileMetadata f WHERE f.owner.id = :ownerId")
    List<FileMetadata> findByOwnerId(@Param("ownerId") UUID ownerId);

    // Подсчет файлов по ID владельца (через @Query)
    @Query("SELECT COUNT(f) FROM FileMetadata f WHERE f.owner.id = :ownerId")
    long countByOwnerId(@Param("ownerId") UUID ownerId);

    // Подсчет файлов по статусу (альтернатива naming convention)
    @Query("SELECT COUNT(f) FROM FileMetadata f WHERE f.status = :status")
    long countByStatusQuery(@Param("status") Status status);

    // Поиск файлов по ID владельца и статусу
    @Query("SELECT f FROM FileMetadata f WHERE f.owner.id = :ownerId AND f.status = :status")
    List<FileMetadata> findByOwnerIdAndStatus(@Param("ownerId") UUID ownerId, @Param("status") Status status);
}