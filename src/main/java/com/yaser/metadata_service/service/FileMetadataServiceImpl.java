package com.yaser.metadata_service.service;

import com.yaser.metadata_service.dto.FileMetadataResponseDTO;
import com.yaser.metadata_service.dto.FileUploadRequestDTO;
import com.yaser.metadata_service.entity.FileMetadata;
import com.yaser.metadata_service.entity.Status;
import com.yaser.metadata_service.entity.User;
import com.yaser.metadata_service.mapper.FileMetadataMapper;
import com.yaser.metadata_service.repository.FileMetadataRepository;
import com.yaser.metadata_service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FileMetadataServiceImpl implements FileMetadataService {

    private final FileMetadataRepository fileMetadataRepository;
    private final UserRepository userRepository;
    private final FileMetadataMapper fileMetadataMapper;

    @Override
    public FileMetadataResponseDTO createFile(FileUploadRequestDTO requestDTO, UUID ownerId) {
        log.info("Creating file for owner: {}", ownerId);

        // 1. Находим владельца
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + ownerId));

        // 2. Преобразуем DTO в Entity
        FileMetadata fileMetadata = fileMetadataMapper.toEntity(requestDTO);
        fileMetadata.setOwner(owner); // Устанавливаем владельца

        // 3. Сохраняем в БД
        FileMetadata savedFile = fileMetadataRepository.save(fileMetadata);
        log.info("File created with id: {}", savedFile.getId());

        // 4. Возвращаем ResponseDTO
        return fileMetadataMapper.toResponseDTO(savedFile);
    }

    @Override
    @Transactional(readOnly = true)
    public FileMetadataResponseDTO getFileById(UUID fileId) {
        log.info("Getting file by id: {}", fileId);

        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("File not found with id: " + fileId));

        return fileMetadataMapper.toResponseDTO(fileMetadata);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileMetadataResponseDTO> getFilesByOwner(UUID ownerId) {
        log.info("Getting files for owner: {}", ownerId);

        List<FileMetadata> files = fileMetadataRepository.findByOwnerId(ownerId);

        return files.stream()
                .map(fileMetadataMapper::toResponseDTO)
                .toList();
    }

    @Override
    public void deleteFile(UUID fileId, UUID ownerId) {
        log.info("Deleting file {} by owner {}", fileId, ownerId);

        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("File not found with id: " + fileId));

        // Проверяем, что удаляет владелец
        if (!fileMetadata.getOwner().getId().equals(ownerId)) {
            throw new SecurityException("You can only delete your own files");
        }

        fileMetadataRepository.delete(fileMetadata);
        log.info("File deleted: {}", fileId);
    }

    @Override
    public FileMetadataResponseDTO updateFileStatus(UUID fileId, String status, UUID ownerId) {
        log.info("Updating file {} status to {} by owner {}", fileId, status, ownerId);

        FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new EntityNotFoundException("File not found with id: " + fileId));

        // Проверяем права
        if (!fileMetadata.getOwner().getId().equals(ownerId)) {
            throw new SecurityException("You can only update your own files");
        }

        // Обновляем статус
        fileMetadata.setStatus(Status.valueOf(status.toUpperCase()));

        FileMetadata updatedFile = fileMetadataRepository.save(fileMetadata);

        return fileMetadataMapper.toResponseDTO(updatedFile);
    }
}
