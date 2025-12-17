package com.yaser.metadata_service.command;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class CreateFileCommand {

    private final UUID ownerId;
    private final String filename;
    private final String contentType;
    private final long size;

    @NotNull
    public CreateFileCommand(
            UUID ownerId,
            String filename,
            String contentType,
            long size
    ) {
        this.ownerId = ownerId;
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getFilename() {
        return filename;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }
}
