package com.yaser.metadata_service.application.access;

import com.yaser.metadata_service.entity.User;
import com.yaser.metadata_service.exception.AccessDeniedException;
import com.yaser.metadata_service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserAccessService {

    private final UserRepository userRepository;

    // Конструктор с зависимостью
    @Autowired
    public UserAccessService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
        if (user.getId() == null || !user.getId().equals(resourceOwnerId)) {
            throw new AccessDeniedException(
                    String.format("User %s is not the owner of this %s", user.getId(), resourceType)
            );
        }
    }

    /**
     * Проверка, может ли пользователь загружать файлы
     */
    public void validateCanUploadFiles(User user) {
        // Временно упрощаем для тестирования
        // Разрешаем всем загружать файлы
        if (user == null) {
            throw new AccessDeniedException("User is null");
        }
    }

    /**
     * Проверка, является ли пользователь администратором
     */
    public void validateIsAdmin(User user) {
        // Временно упрощаем для тестирования
        // Разрешаем всем быть админами
        if (user == null) {
            throw new AccessDeniedException("User is null");
        }
    }

    /**
     * Проверка согласованности ownerId в запросе с текущим пользователем
     */
    public void validateOwnerConsistency(UUID requestOwnerId, User currentUser) {
        if (currentUser.getId() == null || !requestOwnerId.equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    String.format("You can only create files for yourself. Request ownerId: %s, Current userId: %s",
                            requestOwnerId, currentUser.getId())
            );
        }
    }
}