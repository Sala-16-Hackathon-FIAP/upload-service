package br.com.fiapx.upload.application.port.output;

import br.com.fiapx.upload.domain.model.Upload;

public interface EventPublisherPort {
    void publishUploadCompleted(Upload upload);
}
