package br.com.fiapx.upload.infrastructure.config;

import com.autoflow.rabbit_topic_lib.core.TopicExchangeManager;
import com.autoflow.rabbit_topic_lib.model.TopicBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public RabbitMQConfig(TopicExchangeManager exchangeManager) {
        exchangeManager.declareExchange("fiapx.events");
    }
}
