# upload-service

Handles chunked video uploads using S3 multipart upload. Publishes a `video.upload.completed` event to RabbitMQ when all chunks have been assembled.

## Technology Stack

- **Java 21** + **Spring Boot 3.5.0**
- **Spring Security** with JWT (JJWT 0.12.6)
- **PostgreSQL 16** with **Flyway** migrations
- **AWS SDK v2** (S3 multipart upload) — **LocalStack** for local development
- **RabbitMQ** via `rabbit-topic-lib` (choreographed saga)
- **New Relic** APM (Java Agent v8.15.0)
- **SpringDoc OpenAPI** (Swagger UI)
- **JaCoCo** for code coverage (minimum 80%)
- **Hexagonal Architecture** (ports and adapters)

## Responsibility

- Initiate an S3 multipart upload session
- Accept individual chunks (up to 10 MB each) and forward them to S3
- Complete the multipart upload on S3 when all chunks are received
- Persist upload metadata in its own PostgreSQL database
- Publish `video.upload.completed` to the `fiapx.events` RabbitMQ exchange

## Architecture

Hexagonal (ports and adapters):

```
infrastructure/rest        -> HTTP layer (UploadController)
application/port/input     -> UploadUseCase interface
application/service        -> UploadService (use-case implementation)
infrastructure/persistence -> Spring Data JPA + Flyway
infrastructure/s3          -> AWS SDK v2 S3Client adapter
infrastructure/messaging   -> RabbitMQ publisher (rabbit-topic-lib)
infrastructure/security    -> JwtAuthFilter, SecurityConfig
infrastructure/monitoring  -> NewRelicTracker
```

## API Endpoints

All endpoints require `Authorization: Bearer <JWT>`.

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/uploads/initiate` | Start a multipart upload session |
| `PUT` | `/api/v1/uploads/{id}/chunks/{n}` | Upload chunk number `n` (1-based) |
| `POST` | `/api/v1/uploads/{id}/complete` | Assemble all chunks and trigger processing |
| `GET` | `/api/v1/uploads/{id}` | Get upload details |
| `GET` | `/api/v1/uploads` | List all uploads for the authenticated user |

### Initiate upload

```bash
curl -s -X POST http://localhost:8082/api/v1/uploads/initiate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "filename": "video.mp4",
    "fileSize": 52428800,
    "mimeType": "video/mp4"
  }' | jq
```

Response `201 Created`:
```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "userId": "...",
  "originalFilename": "video.mp4",
  "fileSize": 52428800,
  "status": "UPLOADING",
  "createdAt": "2025-05-28T10:00:00"
}
```

### Upload a chunk

```bash
curl -s -X PUT "http://localhost:8082/api/v1/uploads/$UPLOAD_ID/chunks/1" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@chunk_aa"
```

### Complete upload

```bash
curl -s -X POST "http://localhost:8082/api/v1/uploads/$UPLOAD_ID/complete" \
  -H "Authorization: Bearer $TOKEN" | jq
```

Response:
```json
{
  "id": "...",
  "status": "COMPLETED"
}
```

### Swagger UI

http://localhost:8082/swagger-ui.html

## RabbitMQ Events

**Exchange:** `fiapx.events` (topic)

| Direction | Routing key | Payload fields |
|---|---|---|
| **Publishes** | `video.upload.completed` | `uploadId`, `userId`, `filename`, `s3Key`, `mimeType`, `uploadedAt` |

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5434/fiapx_upload` | JDBC connection URL |
| `DB_USERNAME` | `fiapx` | Database user |
| `DB_PASSWORD` | `fiapx123` | Database password |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ host |
| `RABBITMQ_PORT` | `5672` | RabbitMQ AMQP port |
| `RABBITMQ_USER` | `fiapx` | RabbitMQ user |
| `RABBITMQ_PASS` | `fiapx123` | RabbitMQ password |
| `JWT_SECRET` | *(dev key)* | Must match auth-service |
| `AWS_REGION` | `us-east-1` | S3 region |
| `S3_BUCKET_NAME` | `fiapx-videos` | Target bucket |
| `S3_ENDPOINT` | `http://localhost:4566` | Override for LocalStack |
| `S3_PATH_STYLE` | `true` | Required for LocalStack |
| `AWS_ACCESS_KEY_ID` | — | `test` for LocalStack |
| `AWS_SECRET_ACCESS_KEY` | — | `test` for LocalStack |
| `UPLOAD_MAX_FILE_SIZE` | `2147483648` (2 GB) | Maximum total file size |
| `UPLOAD_MAX_CHUNK_SIZE` | `10485760` (10 MB) | Maximum single chunk size |

## Running Locally

### Prerequisites

- Docker and Docker Compose installed
- Java 21 and Maven installed
- AWS CLI installed (to create the S3 bucket on LocalStack)
- auth-service running on port 8080 (needed to generate valid JWT tokens)

### 1. Start infrastructure

```bash
cd upload-service
docker-compose up -d
```

This starts:
- **PostgreSQL** on port `5434` (database `fiapx_upload`, user `fiapx`, password `fiapx123`)
- **RabbitMQ** on port `5672` (AMQP) and `15672` (Management UI — login: `fiapx` / `fiapx123`)
- **LocalStack** (S3 emulator) on port `4566`

Verify all containers are healthy:

```bash
docker-compose ps
```

### 2. Create the S3 bucket on LocalStack

```bash
AWS_ACCESS_KEY_ID=test AWS_SECRET_ACCESS_KEY=test \
  aws --endpoint-url=http://localhost:4566 --region us-east-1 s3 mb s3://fiapx-videos
```

Verify the bucket was created:

```bash
AWS_ACCESS_KEY_ID=test AWS_SECRET_ACCESS_KEY=test \
  aws --endpoint-url=http://localhost:4566 --region us-east-1 s3 ls
```

### 3. Run the application

**Option A — Maven (terminal):**

```bash
AWS_ACCESS_KEY_ID=test AWS_SECRET_ACCESS_KEY=test mvn spring-boot:run
```

**Option B — IDE (IntelliJ / Eclipse):**

Run the main class `UploadServiceApplication` directly using the Run/Debug button. Add the following environment variables in the Run configuration:
- `AWS_ACCESS_KEY_ID=test`
- `AWS_SECRET_ACCESS_KEY=test`

The application starts on **port 8082**. Flyway automatically runs the database migration on startup.

Verify the service is running:

```
http://localhost:8082/actuator/health
```

### 4. Obtain a JWT token (via auth-service)

The auth-service must be running on port `8080`. Authenticate to get a token:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"useradmin@email.com","password":"Admin@12345"}' | jq -r '.bearerToken')

echo $TOKEN
```

## Local Testing — Happy Path

The full upload flow has three steps: **initiate → chunk → complete**.

### Test 1 — Initiate a multipart upload

```bash
curl -s -X POST http://localhost:8082/api/v1/uploads/initiate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "filename": "my-video.mov",
    "fileSize": 986112,
    "mimeType": "video/quicktime"
  }' | jq
```

Expected: `201 Created` with `"status": "UPLOADING"`. Save the returned `id`:

```bash
UPLOAD_ID="<returned-id>"
```

### Test 2 — Upload a chunk

You can use any file (video, binary, etc.). If you don't have one handy, generate a test file:

```bash
dd if=/dev/urandom of=/tmp/test-chunk.bin bs=1024 count=1024
```

Send it as chunk 1:

```bash
curl -s -X PUT "http://localhost:8082/api/v1/uploads/$UPLOAD_ID/chunks/1" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@/tmp/test-chunk.bin" | jq
```

Expected: `200 OK` with the upload details.

### Test 3 — Complete the upload

```bash
curl -s -X POST "http://localhost:8082/api/v1/uploads/$UPLOAD_ID/complete" \
  -H "Authorization: Bearer $TOKEN" | jq
```

Expected: `"status": "COMPLETED"`. At this point the service:
1. Finalizes the multipart upload on S3 (LocalStack)
2. Publishes a `video.upload.completed` event to RabbitMQ

### Test 4 — Get upload details

```bash
curl -s "http://localhost:8082/api/v1/uploads/$UPLOAD_ID" \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Test 5 — List all uploads for the user

```bash
curl -s http://localhost:8082/api/v1/uploads \
  -H "Authorization: Bearer $TOKEN" | jq
```

### Test 6 — Verify the file in S3 (LocalStack)

```bash
AWS_ACCESS_KEY_ID=test AWS_SECRET_ACCESS_KEY=test \
  aws --endpoint-url=http://localhost:4566 --region us-east-1 s3 ls s3://fiapx-videos/ --recursive
```

Expected: the file appears with a path like `uploads/<userId>/my-video.mov`.

### Test 7 — Verify the RabbitMQ event

Open `http://localhost:15672` → **Queues**. If the status-service or notification-service are running, they will have consumed the `video.upload.completed` event. Otherwise, the event remains in the queues if they were previously declared.

### Test 8 — Test without a token (should return 401/403)

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/api/v1/uploads
```

Expected: `401` or `403`.

### Swagger UI

Access: `http://localhost:8082/swagger-ui.html`

Click **Authorize** and enter `Bearer <your-token>`. You can execute the full flow (initiate → chunk → complete) directly from the Swagger UI.

### Summary of tested scenarios

| Step | Endpoint | What it validates |
|---|---|---|
| Initiate | `POST /api/v1/uploads/initiate` | Creates multipart session on S3 + DB record |
| Chunk | `PUT /uploads/{id}/chunks/1` | Uploads part to S3 via multipart |
| Complete | `POST /uploads/{id}/complete` | Finalizes on S3 + publishes RabbitMQ event |
| GET by ID | `GET /uploads/{id}` | Individual upload query |
| GET list | `GET /uploads` | List by authenticated user |
| S3 ls | `aws s3 ls` | File is actually stored in LocalStack |
| No token | `GET /uploads` | JWT security works |

## Automated Tests

```bash
mvn test
```

JaCoCo enforces **>= 80% instruction coverage**. Coverage report: `target/site/jacoco/index.html`.

## CI/CD

GitHub Actions workflow: build -> test -> SonarCloud -> GHCR push -> EKS deploy.

The `GITHUB_TOKEN` secret is required in CI to download `rabbit-topic-lib` from GitHub Packages. Docker image is **public** on GHCR.

## Database

- PostgreSQL 16, schema: `fiapx_upload`
- Migrations managed by Flyway (`src/main/resources/db/migration`)

## Acknowledgments

This project was developed with the assistance of [Claude](https://claude.com/claude-code) (Anthropic) as an AI pair-programming tool for code implementation, debugging, and documentation.
