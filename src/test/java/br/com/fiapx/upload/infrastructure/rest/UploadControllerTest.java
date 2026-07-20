package br.com.fiapx.upload.infrastructure.rest;

import br.com.fiapx.upload.application.port.input.UploadUseCase;
import br.com.fiapx.upload.domain.exception.FileSizeExceededException;
import br.com.fiapx.upload.domain.exception.UploadNotFoundException;
import br.com.fiapx.upload.domain.model.Upload;
import br.com.fiapx.upload.domain.model.UploadStatus;
import br.com.fiapx.upload.infrastructure.rest.handler.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UploadController.class)
@Import({GlobalExceptionHandler.class, br.com.fiapx.upload.infrastructure.security.SecurityConfig.class})
class UploadControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean UploadUseCase uploadUseCase;
    @MockBean br.com.fiapx.upload.infrastructure.security.JwtAuthFilter jwtAuthFilter;

    private UUID userId;
    private Upload sampleUpload;

    @BeforeEach
    void setUp() throws Exception {
        userId = UUID.randomUUID();
        sampleUpload = new Upload(UUID.randomUUID(), userId, "video.mp4", 1024L,
                "video/mp4", "uploads/key", "mid", UploadStatus.UPLOADING, null,
                LocalDateTime.now(), LocalDateTime.now());
        doAnswer(invocation -> {
            var chain = invocation.getArgument(2, jakarta.servlet.FilterChain.class);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    private UsernamePasswordAuthenticationToken userAuth() {
        return new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void initiateUpload_shouldReturn201_whenValidRequest() throws Exception {
        when(uploadUseCase.initiateUpload(eq(userId), eq("video.mp4"), eq(1024L), any()))
                .thenReturn(sampleUpload);

        mockMvc.perform(post("/api/v1/uploads/initiate")
                        .with(authentication(userAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filename\":\"video.mp4\",\"fileSize\":1024,\"mimeType\":\"video/mp4\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("UPLOADING"));
    }

    @Test
    void initiateUpload_shouldReturn400_whenFilenameIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/uploads/initiate")
                        .with(authentication(userAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filename\":\"\",\"fileSize\":1024}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void initiateUpload_shouldReturn413_whenFileTooLarge() throws Exception {
        when(uploadUseCase.initiateUpload(any(), any(), any(), any()))
                .thenThrow(new FileSizeExceededException(2147483648L));

        mockMvc.perform(post("/api/v1/uploads/initiate")
                        .with(authentication(userAuth()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filename\":\"big.mp4\",\"fileSize\":3000000000}"))
                .andExpect(status().isPayloadTooLarge());
    }

    @Test
    void uploadChunk_shouldReturn200_whenValidChunk() throws Exception {
        when(uploadUseCase.uploadChunk(any(), eq(userId), eq(1), any(), anyLong())).thenReturn(null);
        when(uploadUseCase.getUpload(any(), eq(userId))).thenReturn(sampleUpload);

        MockMultipartFile file = new MockMultipartFile("file", "chunk1.bin",
                "application/octet-stream", new byte[1024]);

        mockMvc.perform(multipart("/api/v1/uploads/{id}/chunks/{num}", sampleUpload.id(), 1)
                        .file(file)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .with(authentication(userAuth())))
                .andExpect(status().isOk());
    }

    @Test
    void getUpload_shouldReturn404_whenNotFound() throws Exception {
        when(uploadUseCase.getUpload(any(), any()))
                .thenThrow(new UploadNotFoundException(UUID.randomUUID()));

        mockMvc.perform(get("/api/v1/uploads/{id}", UUID.randomUUID())
                        .with(authentication(userAuth())))
                .andExpect(status().isNotFound());
    }

    @Test
    void listUploads_shouldReturnUserUploads() throws Exception {
        when(uploadUseCase.getUserUploads(userId)).thenReturn(List.of(sampleUpload));

        mockMvc.perform(get("/api/v1/uploads").with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].originalFilename").value("video.mp4"));
    }

    @Test
    void completeUpload_shouldReturn200_whenCompleted() throws Exception {
        Upload completed = new Upload(sampleUpload.id(), userId, "video.mp4", 1024L,
                "video/mp4", "key", "mid", UploadStatus.COMPLETED, null,
                LocalDateTime.now(), LocalDateTime.now());
        when(uploadUseCase.completeUpload(any(), eq(userId))).thenReturn(completed);

        mockMvc.perform(post("/api/v1/uploads/{id}/complete", sampleUpload.id())
                        .with(authentication(userAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
