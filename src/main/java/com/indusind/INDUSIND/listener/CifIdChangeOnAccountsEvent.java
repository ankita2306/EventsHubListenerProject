/*
package com.indusind.INDUSIND.listener;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Scope;
import com.indusind.INDUSIND.service.EventsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;

@Component
public class CifIdChangeOnAccountsEvent {
    private static final Logger logger = LoggerFactory.getLogger(EventsService.class);

    private final Bucket bucket;
    private final String eventHubConsumerGroup;
    private final String eventHubConnectionString;

    private final String cifIdChangeTopic;


    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private EventsService eventsService;


    @Autowired
    public CifIdChangeOnAccountsEvent(Cluster cluster,
                                  Bucket bucket,
                                  Scope scope,
                                  @Value("${couchbase.collection.customer-id-reference}") String customerIdCollectionName,
                                  @Value("${couchbase.collection.phone-number-reference}") String phoneNumberCollectionName,
                                  @Value("${couchbase.collection.acid-reference}") String acidCollectionName,
                                  @Value("${couchbase.collection.account-foracid-reference}") String accountForAcidCollectionName,
                                  @Value("${couchbase.collection.customer-details}") String customerDetailsCollectionName,
                                  @Value("${couchbase.collection.ucic-reference}") String ucicReferenceCollectionName,
                                  @Value("${eventhub.consumer-group}") String eventHubConsumerGroup,
                                  @Value("${eventhub.connection-string}")   String eventHubConnectionString,
                                  @Value("${eventhub.topic.phone-change}") String phoneChangeTopic,
                                  @Value("${eventhub.topic.closed-accounts}") String closedAccountsTopic,
                                  @Value("${eventhub.topic.accounts-status-change}") String accountStatusChangeTopic,
                                  @Value("${eventhub.topic.new-customer-onboarded}") String newCustomerOnboardedTopic,
                                  @Value("${eventhub.topic.new-account-created}") String newAccountCreatedTopic,
                                  @Value("${eventhub.topic.cifid-change-on-accounts}") String cifIdChangeTopic,
                                  @Value("${eventhub.topic.ucic-change}") String ucicChangeTopic
    ) {
        this.bucket = bucket;
        this.bucket.waitUntilReady(Duration.ofSeconds(5));
        this.eventHubConsumerGroup = eventHubConsumerGroup;
        this.eventHubConnectionString=eventHubConnectionString;
        this.cifIdChangeTopic=cifIdChangeTopic;
    }


    @PostConstruct
    public void initialize()
    {
        System.out.println("initialise inside newAccoCreated");
        CountDownLatch countDownLatch = new CountDownLatch(1);

        new EventHubClientBuilder()
                .consumerGroup(eventHubConsumerGroup)
                .connectionString(eventHubConnectionString, cifIdChangeTopic)
                .buildAsyncConsumerClient()
                .receive()
                .subscribe(partitionEvent ->{
                            System.out.println("new account created"+partitionEvent.getData().getBodyAsString());
                            eventsService.consumeCifIdChangeOnAccountsBusinessEvents();
                        },
                        throwable -> {
                            System.err.println("Error occurred: " + throwable.getMessage());
                        },
                        countDownLatch::countDown
                );

        try {
            countDownLatch.await();
        }
        catch (InterruptedException e)
        {
            logger.error("Error waiting for latch: {}", e.getMessage());
        }
        cleanup();
    }
    @PreDestroy
    public void cleanup() {
        System.out.println("Cleanup method invoked. Closing resources...");

    }
}
*/
