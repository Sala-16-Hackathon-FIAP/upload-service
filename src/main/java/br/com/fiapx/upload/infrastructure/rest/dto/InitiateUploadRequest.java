package br.com.fiapx.upload.infrastructure.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record InitiateUploadRequest(
    @NotBlank String filename,
    @Positive Long fileSize,
    String mimeType
) {}
