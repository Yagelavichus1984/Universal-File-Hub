package com.yaser.metadata_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter  // ← Создает геттеры для всех полей
@Setter  // ← Создает сеттеры для всех полей
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"files", "roles", "passwordHash"})
@EqualsAndHashCode(exclude = {"files", "roles"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(nullable = false, name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileMetadata> files = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    // === РУЧНЫЕ ГЕТТЕРЫ (если Lombok не работает) ===

    public UUID getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPasswordHash() {
        return this.passwordHash;
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

    public List<FileMetadata> getFiles() {
        return this.files;
    }

    public Set<Role> getRoles() {  // ← ВОТ ЭТОТ МЕТОД НУЖЕН!
        return this.roles;
    }

    // === РУЧНЫЕ СЕТТЕРЫ ===

    public void setId(UUID id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public void setFiles(List<FileMetadata> files) {
        this.files = files;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    // === ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ ===
    public User() {
    }

    public User(UUID id) {
        this.id = id;
    }

    public boolean hasRole(String roleName) {
        if (roles == null) return false;
        return roles.stream()
                .anyMatch(role -> role != null && roleName.equals(role.getName()));
    }

    public boolean isActive() {
        // Логика проверки активности пользователя
        return true; // Замените на реальную логику
    }
}