package com.yaser.metadata_service.repository;

import com.yaser.metadata_service.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    Optional<FileMetadata> findByFileName(String fileName);
}
