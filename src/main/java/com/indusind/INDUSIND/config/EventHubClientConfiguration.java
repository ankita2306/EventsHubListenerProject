package com.indusind.INDUSIND.config;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class EventHubClientConfiguration {

    private static final Logger lOGGER = LoggerFactory.getLogger(com.indusind.INDUSIND.config.EventHubClientConfiguration.class);
    @Value("${eventhub.connection-string}")
    private String CONNECTION_STRING;

    @Bean
    @Scope(value = "prototype")
    public EventHubConsumerClient getEventHubConsumerClient(String consumerGroup, String eventHubName) {
        lOGGER.info("Connection String : " + CONNECTION_STRING);
        lOGGER.info("Consumer Group : " + consumerGroup);
        lOGGER.info("Event Hub : " + eventHubName);

        return new EventHubClientBuilder()
                .connectionString(CONNECTION_STRING)
                .consumerGroup(consumerGroup)
                .eventHubName(eventHubName).buildConsumerClient();
    }

    @Bean
    @Scope(value = "prototype")
    public EventHubConsumerAsyncClient getEventHubAsyncConsumerClient(String consumerGroup, String eventHubName) {
        lOGGER.info("Connection String : " + CONNECTION_STRING);
        lOGGER.info("Consumer Group : " + consumerGroup);
        lOGGER.info("Event Hub : " + eventHubName);

        return new EventHubClientBuilder()
                .connectionString(CONNECTION_STRING)
                .consumerGroup(consumerGroup)
                .eventHubName(eventHubName).buildAsyncConsumerClient();
    }
}
