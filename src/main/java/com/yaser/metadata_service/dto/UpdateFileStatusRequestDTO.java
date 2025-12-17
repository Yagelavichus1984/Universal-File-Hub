package com.yaser.metadata_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data  // Эта аннотация создаст getStatus()
public class UpdateFileStatusRequestDTO {

    @NotBlank(message = "Статус обязателен")
    private String status;

    // Если @Data не работает, добавьте геттер вручную:
    public String getStatus() {
        return this.status;
    }

    // И сеттер:
    public void setStatus(String status) {
        this.status = status;
    }
}