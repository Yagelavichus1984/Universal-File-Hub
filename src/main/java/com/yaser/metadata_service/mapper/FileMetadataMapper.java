package com.yaser.metadata_service.mapper;

import com.yaser.metadata_service.dto.FileMetadataResponseDTO;
import com.yaser.metadata_service.dto.FileUploadRequestDTO;
import com.yaser.metadata_service.entity.FileMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface FileMetadataMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "UPLOADED")
    @Mapping(target = "storageKey", expression = "java(generateStorageKey())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "owner", ignore = true)
    FileMetadata toEntity(FileUploadRequestDTO dto);

    @Mapping(source = "owner.id", target = "ownerId")
    @Mapping(source = "owner.username", target = "ownerUsername")
    FileMetadataResponseDTO toResponseDTO(FileMetadata entity);

    @Named("generateStorageKey")
    default String generateStorageKey() {
        return "files/" + UUID.randomUUID();
    }
}