package br.com.fiapx.upload.infrastructure.monitoring;

import com.newrelic.api.agent.Agent;
import com.newrelic.api.agent.Insights;
import com.newrelic.api.agent.NewRelic;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NewRelicTrackerTest {

    @Test
    void trackUploadInitiated_shouldRecordCustomEvent() {
        NewRelicTracker tracker = new NewRelicTracker();
        UUID uploadId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        try (MockedStatic<NewRelic> mocked = mockStatic(NewRelic.class)) {
            Agent agent = mock(Agent.class);
            Insights insights = mock(Insights.class);
            mocked.when(NewRelic::getAgent).thenReturn(agent);
            when(agent.getInsights()).thenReturn(insights);

            tracker.trackUploadInitiated(uploadId, userId);

            verify(insights).recordCustomEvent(eq("UploadInitiated"), anyMap());
        }
    }

    @Test
    void trackUploadCompleted_shouldRecordEventAndIncrementCounter() {
        NewRelicTracker tracker = new NewRelicTracker();
        UUID uploadId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        try (MockedStatic<NewRelic> mocked = mockStatic(NewRelic.class)) {
            Agent agent = mock(Agent.class);
            Insights insights = mock(Insights.class);
            mocked.when(NewRelic::getAgent).thenReturn(agent);
            when(agent.getInsights()).thenReturn(insights);

            tracker.trackUploadCompleted(uploadId, userId);

            verify(insights).recordCustomEvent(eq("UploadCompleted"), anyMap());
            mocked.verify(() -> NewRelic.incrementCounter("Custom/Upload/Completed"));
        }
    }

    @Test
    void trackUploadFailed_shouldRecordEventAndIncrementCounter() {
        NewRelicTracker tracker = new NewRelicTracker();
        UUID uploadId = UUID.randomUUID();

        try (MockedStatic<NewRelic> mocked = mockStatic(NewRelic.class)) {
            Agent agent = mock(Agent.class);
            Insights insights = mock(Insights.class);
            mocked.when(NewRelic::getAgent).thenReturn(agent);
            when(agent.getInsights()).thenReturn(insights);

            tracker.trackUploadFailed(uploadId, "error reason");

            verify(insights).recordCustomEvent(eq("UploadFailed"), anyMap());
            mocked.verify(() -> NewRelic.incrementCounter("Custom/Upload/Failed"));
        }
    }
}
