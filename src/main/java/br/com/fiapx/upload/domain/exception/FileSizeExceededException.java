package br.com.fiapx.upload.domain.exception;

public class FileSizeExceededException extends RuntimeException {
    public FileSizeExceededException(long maxBytes) {
        super("File size exceeds the maximum allowed limit of " + (maxBytes / 1024 / 1024) + " MB");
    }
}
