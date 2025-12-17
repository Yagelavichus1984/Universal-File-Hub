package com.yaser.metadata_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileStatisticsDTO {
    private long totalFiles;
    private long uploadedFiles;
    private long processingFiles;
    private long readyFiles;
    private long failedFiles;
}
