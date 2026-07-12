package br.com.fiapx.upload.infrastructure.scheduling;

import br.com.fiapx.upload.application.port.input.UploadReconciliationUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadReconciliationSchedulerTest {

    @Mock private UploadReconciliationUseCase reconciliationUseCase;

    @InjectMocks
    private UploadReconciliationScheduler scheduler;

    @Test
    void run_triggersReconciliation() {
        when(reconciliationUseCase.reconcileStuckUploads()).thenReturn(3);
        scheduler.run();
        verify(reconciliationUseCase).reconcileStuckUploads();
    }

    @Test
    void run_swallowsExceptions() {
        doThrow(new RuntimeException("boom")).when(reconciliationUseCase).reconcileStuckUploads();
        assertThatCode(scheduler::run).doesNotThrowAnyException();
    }
}
