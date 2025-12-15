package com.yaser.metadata_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;


import java.util.UUID;

@Getter
@Setter
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
}