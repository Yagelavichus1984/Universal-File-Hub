package com.yaser.metadata_service.entity;

public enum Status {
    UPLOADED, // метаданные сохранены
    PROCESSING,  // обработка в процессе
    READY,  // обработка завершена
    FAILED  // обработка не удалась
}
