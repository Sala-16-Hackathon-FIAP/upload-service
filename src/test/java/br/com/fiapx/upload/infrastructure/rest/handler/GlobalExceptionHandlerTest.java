package br.com.fiapx.upload.infrastructure.rest.handler;

import br.com.fiapx.upload.domain.exception.FileSizeExceededException;
import br.com.fiapx.upload.domain.exception.UploadNotFoundException;
import br.com.fiapx.upload.domain.exception.UploadNotOwnedByUserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleNotFound_shouldReturn404() {
        UploadNotFoundException ex = new UploadNotFoundException(UUID.randomUUID());
        ProblemDetail detail = handler.handleNotFound(ex);

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(detail.getDetail()).contains("Upload not found");
    }

    @Test
    void handleForbidden_shouldReturn403() {
        UploadNotOwnedByUserException ex = new UploadNotOwnedByUserException();
        ProblemDetail detail = handler.handleForbidden(ex);

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void handleFileTooLarge_shouldReturn413() {
        FileSizeExceededException ex = new FileSizeExceededException(1048576L);
        ProblemDetail detail = handler.handleFileTooLarge(ex);

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE.value());
    }

    @Test
    void handleValidation_shouldReturn400WithErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("request", "filename", "must not be blank");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ProblemDetail detail = handler.handleValidation(ex);

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(detail.getProperties()).containsKey("errors");
    }

    @Test
    void handleValidation_shouldHandleNullDefaultMessage() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("request", "fileSize", null);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ProblemDetail detail = handler.handleValidation(ex);

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void handleGeneral_shouldReturn500() {
        Exception ex = new RuntimeException("unexpected");
        ProblemDetail detail = handler.handleGeneral(ex);

        assertThat(detail.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(detail.getDetail()).isEqualTo("An unexpected error occurred");
    }
}
