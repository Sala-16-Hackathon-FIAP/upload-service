CREATE TABLE IF NOT EXISTS uploads (
    id                UUID PRIMARY KEY,
    user_id           UUID NOT NULL,
    original_filename VARCHAR(500) NOT NULL,
    file_size         BIGINT,
    mime_type         VARCHAR(100),
    s3_key            VARCHAR(1000),
    upload_id         VARCHAR(500),
    status            VARCHAR(20) NOT NULL DEFAULT 'INITIATED',
    error_message     TEXT,
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_uploads_user_id ON uploads(user_id);
CREATE INDEX idx_uploads_status  ON uploads(status);

CREATE TABLE IF NOT EXISTS upload_chunks (
    id           UUID PRIMARY KEY,
    upload_id    UUID NOT NULL REFERENCES uploads(id) ON DELETE CASCADE,
    chunk_number INT NOT NULL,
    etag         VARCHAR(500),
    uploaded_at  TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (upload_id, chunk_number)
);

CREATE INDEX idx_upload_chunks_upload_id ON upload_chunks(upload_id);
