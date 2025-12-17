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

    // Поиск по имени файла (если нужен по бизнес-правилам)
    Optional<FileMetadata> findByFileName(String fileName);

    // Поиск по владельцу (User entity вместо ownerId)
    List<FileMetadata> findByOwner(User owner);

    // Поиск по статусу
    List<FileMetadata> findByStatus(Status status);

    // Поиск по статусу и времени создания (для устаревших файлов)
    List<FileMetadata> findByStatusAndCreatedAtBefore(Status status, OffsetDateTime threshold);

    // Поиск по storage key (уникальному ключу)
    Optional<FileMetadata> findByStorageKey(String storageKey);

    // Проверка существования storage key для другого файла
    boolean existsByStorageKeyAndIdNot(String storageKey, UUID fileId);

    // Нативный запрос для поиска по ownerId (если нужна обратная совместимость)
    @Query("SELECT f FROM FileMetadata f WHERE f.owner.id = :ownerId")
    List<FileMetadata> findByOwnerId(@Param("ownerId") UUID ownerId);

    // Поиск файлов по типу контента
    List<FileMetadata> findByContentType(String contentType);

    // Поиск файлов по размеру (больше указанного)
    List<FileMetadata> findBySizeGreaterThan(Long size);

    // Поиск файлов по диапазону дат создания
    List<FileMetadata> findByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end);

    // Поиск файлов определенного владельца со статусом
    List<FileMetadata> findByOwnerAndStatus(User owner, Status status);

    // Подсчет файлов по владельцу
    long countByOwner(User owner);

    // Подсчет файлов по статусу
    long countByStatus(Status status);

    // Удаление файлов по владельцу
    void deleteByOwner(User owner);
}