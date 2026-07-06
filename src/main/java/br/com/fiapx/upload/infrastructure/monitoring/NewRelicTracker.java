package br.com.fiapx.upload.infrastructure.monitoring;

import com.newrelic.api.agent.NewRelic;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
public class NewRelicTracker {

    public void trackUploadInitiated(UUID uploadId, UUID userId) {
        NewRelic.getAgent().getInsights().recordCustomEvent("UploadInitiated", Map.of(
                "uploadId", uploadId.toString(), "userId", userId.toString()));
    }

    public void trackUploadCompleted(UUID uploadId, UUID userId) {
        NewRelic.getAgent().getInsights().recordCustomEvent("UploadCompleted", Map.of(
                "uploadId", uploadId.toString(), "userId", userId.toString()));
        NewRelic.incrementCounter("Custom/Upload/Completed");
    }

    public void trackUploadFailed(UUID uploadId, String reason) {
        NewRelic.getAgent().getInsights().recordCustomEvent("UploadFailed", Map.of(
                "uploadId", uploadId.toString(), "reason", reason));
        NewRelic.incrementCounter("Custom/Upload/Failed");
    }
}
