package br.com.fiapx.upload.domain.exception;

public class UploadNotOwnedByUserException extends RuntimeException {
    public UploadNotOwnedByUserException() {
        super("Upload does not belong to the requesting user");
    }
}
