package com.yaser.metadata_service.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter  // ← Добавьте
@Setter  // ← Добавьте
@Entity
@Table(name = "roles")
@ToString(exclude = "users")
@EqualsAndHashCode(exclude = "users")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    // Ручные геттеры (если Lombok не работает)
    public UUID getId() {
        return this.id;
    }

    public String getName() {  // ← Этот метод тоже нужен!
        return this.name;
    }

    public Set<User> getUsers() {
        return this.users;
    }

    // Ручные сеттеры
    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }
}