package com.yaser.metadata_service.repository;

import com.yaser.metadata_service.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {

    Optional<FileMetadata> findByFileName(String fileName);

    List<FileMetadata> findByOwnerId(UUID ownerId);
}
