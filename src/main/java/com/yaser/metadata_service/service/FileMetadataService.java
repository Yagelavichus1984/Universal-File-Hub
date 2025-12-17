package com.yaser.metadata_service.service;

import com.yaser.metadata_service.entity.FileMetadata;
import com.yaser.metadata_service.command.CreateFileCommand;
import com.yaser.metadata_service.entity.Status;
import com.yaser.metadata_service.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface FileMetadataService {

    FileMetadata createFile(CreateFileCommand command, User owner);

    FileMetadata getFileById(UUID fileId);

    @Transactional(readOnly = true)
    List<FileMetadata> getFilesByOwner(User owner);

    void deleteFile(UUID fileId);

    FileMetadata updateFileStatus(UUID fileId, Status status);
}
