package com.yaser.metadata_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
public class FileUploadRequestDTO {

    @NotBlank(message = "Имя файла обязательно")
    private String fileName;

    @NotBlank(message = "Тип контента обязателен")
    private String contentType;

    @NotNull(message = "Размер файла обязателен")
    @Positive(message = "Размер файла должен быть положительным")
    private Long size;

    @NotNull(message = "Владелец файла обязателен")
    private UUID ownerId;

    // Ручные геттеры если @Data не работает:
    public String getFileName() {
        return this.fileName;
    }

    public String getContentType() {
        return this.contentType;
    }

    public Long getSize() {
        return this.size;
    }

    public UUID getOwnerId() {
        return this.ownerId;
    }

    // Сеттеры:
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }
}