package com.yaser.metadata_service.service;

import com.yaser.metadata_service.entity.User;
import com.yaser.metadata_service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import com.yaser.metadata_service.exception.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAccessService {

    private final UserRepository userRepository;

    /**
     * Получение пользователя с проверкой существования
     */
    public User getUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    /**
     * Проверка существования пользователя
     */
    public boolean userExists(UUID userId) {
        return userRepository.existsById(userId);
    }

    /**
     * Проверка, является ли пользователь владельцем ресурса
     */
    public void validateOwnership(User user, UUID resourceOwnerId, String resourceType) {
        if (!user.getId().equals(resourceOwnerId)) {
            throw new AccessDeniedException(
                    String.format("User %s is not the owner of this %s", user.getId(), resourceType)
            );
        }
    }

    /**
     * Проверка, может ли пользователь загружать файлы
     */
    public void validateCanUploadFiles(User user) {
        boolean hasUploadPermission = user.getRoles().stream()
                .anyMatch(role -> "USER".equals(role.getName()) ||
                        "ADMIN".equals(role.getName()) ||
                        "UPLOADER".equals(role.getName()));

        if (!hasUploadPermission) {
            throw new AccessDeniedException("User does not have permission to upload files");
        }
    }

    /**
     * Проверка, является ли пользователь администратором
     */
    public void validateIsAdmin(User user) {
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getName()));

        if (!isAdmin) {
            throw new AccessDeniedException("Only administrators can perform this action");
        }
    }

    /**
     * Проверка согласованности ownerId в запросе с текущим пользователем
     */
    public void validateOwnerConsistency(UUID requestOwnerId, User currentUser) {
        if (!requestOwnerId.equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    String.format("You can only create files for yourself. Request ownerId: %s, Current userId: %s",
                            requestOwnerId, currentUser.getId())
            );
        }
    }
}
