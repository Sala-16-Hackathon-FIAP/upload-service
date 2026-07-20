package br.com.fiapx.upload.domain.exception;

import java.util.UUID;

public class UploadNotFoundException extends RuntimeException {
    public UploadNotFoundException(UUID id) {
        super("Upload not found: " + id);
    }
}
