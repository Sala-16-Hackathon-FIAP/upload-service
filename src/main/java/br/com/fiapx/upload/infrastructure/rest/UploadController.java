package br.com.fiapx.upload.infrastructure.rest;

import br.com.fiapx.upload.application.port.input.UploadUseCase;
import br.com.fiapx.upload.infrastructure.rest.dto.InitiateUploadRequest;
import br.com.fiapx.upload.infrastructure.rest.dto.UploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/uploads")
@Tag(name = "Upload", description = "Chunked video upload endpoints")
public class UploadController {

    private final UploadUseCase uploadUseCase;

    public UploadController(UploadUseCase uploadUseCase) {
        this.uploadUseCase = uploadUseCase;
    }

    @PostMapping("/initiate")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Initiate a multipart upload session")
    public UploadResponse initiateUpload(@Valid @RequestBody InitiateUploadRequest request,
                                         Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        var upload = uploadUseCase.initiateUpload(userId, request.filename(), request.fileSize(), request.mimeType());
        return UploadResponse.fromDomain(upload);
    }

    @PutMapping(value = "/{uploadId}/chunks/{chunkNumber}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a single chunk (max 10MB)")
    public UploadResponse uploadChunk(@PathVariable UUID uploadId,
                                      @PathVariable int chunkNumber,
                                      @Parameter(description = "Chunk file") @RequestPart("file") MultipartFile file,
                                      Authentication auth) throws IOException {
        UUID userId = (UUID) auth.getPrincipal();
        uploadUseCase.uploadChunk(uploadId, userId, chunkNumber, file.getInputStream(), file.getSize());
        var upload = uploadUseCase.getUpload(uploadId, userId);
        return UploadResponse.fromDomain(upload);
    }

    @PostMapping("/{uploadId}/complete")
    @Operation(summary = "Complete the multipart upload")
    public UploadResponse completeUpload(@PathVariable UUID uploadId, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        var upload = uploadUseCase.completeUpload(uploadId, userId);
        return UploadResponse.fromDomain(upload);
    }

    @GetMapping("/{uploadId}")
    @Operation(summary = "Get upload status")
    public UploadResponse getUpload(@PathVariable UUID uploadId, Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return UploadResponse.fromDomain(uploadUseCase.getUpload(uploadId, userId));
    }

    @GetMapping
    @Operation(summary = "List all uploads for the authenticated user")
    public List<UploadResponse> listUploads(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return uploadUseCase.getUserUploads(userId).stream().map(UploadResponse::fromDomain).toList();
    }
}
