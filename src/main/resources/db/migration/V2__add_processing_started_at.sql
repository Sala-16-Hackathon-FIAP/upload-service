-- Reliability bookkeeping for the reconciliation safety net.
-- Set when the upload-service observes a video.processing.started event for an
-- upload. NULL means the processor never picked the upload up (e.g. the broker
-- lost the message), which the reconciler uses to detect and re-publish.
ALTER TABLE uploads ADD COLUMN processing_started_at TIMESTAMP;

-- Bounds reconciliation so a permanently stuck upload cannot be re-published
-- forever; after a maximum number of attempts it is left for manual inspection.
ALTER TABLE uploads ADD COLUMN reconciliation_attempts INT NOT NULL DEFAULT 0;

-- Supports the reconciliation query: COMPLETED uploads with no processing ack
-- that have been idle past the stale threshold.
CREATE INDEX idx_uploads_reconcile ON uploads(status, processing_started_at, updated_at);
