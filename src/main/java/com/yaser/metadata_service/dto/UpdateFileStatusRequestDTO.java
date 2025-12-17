package com.yaser.metadata_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateFileStatusRequestDTO {
    @NotBlank(message = "Статус обязателен")
    private String status;
}