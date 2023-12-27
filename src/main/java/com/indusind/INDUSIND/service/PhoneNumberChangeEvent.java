/*
package com.indusind.INDUSIND.service;


import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
//import com.indusind.INDUSIND.service.EventsService;
import com.indusind.INDUSIND.service.EventsService;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;


@Service
public class PhoneNumberChangeEvent
{

    private CheckpointStore checkpointStore;


    private static final Logger logger = LoggerFactory.getLogger(EventsService.class);
    private final Cluster cluster;
    private final Bucket bucket;
    private final Scope scope;
    private final String customerIdCollectionName;
    private final String phoneNumberCollectionName;
    private final String acidCollectionName;
    private final String accountForAcidCollectionName;
    private final String customerDetailsCollectionName;
    private final String ucicReferenceCollectionName;

    private final String eventHubConsumerGroup;

    private final String eventHubConnectionString;

    private final String phoneChangeTopic;
    private final String closedAccountsTopic;
    private final String accountStatusChangeTopic;
    private final String newCustomerOnboardedTopic;
    private final String newAccountCreatedTopic;
    private final String cifIdChangeTopic;
    private final String ucicChangeTopic;

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private EventsService eventsService;



    @Autowired
    public PhoneNumberChangeEvent(Cluster cluster,
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
        this.cluster = cluster;
        this.bucket = bucket;
        this.scope = scope;
        this.bucket.waitUntilReady(Duration.ofSeconds(10));
        this.customerIdCollectionName = customerIdCollectionName;
        this.phoneNumberCollectionName = phoneNumberCollectionName;
        this.acidCollectionName = acidCollectionName;
        this.accountForAcidCollectionName = accountForAcidCollectionName;
        this.customerDetailsCollectionName = customerDetailsCollectionName;
        this.ucicReferenceCollectionName = ucicReferenceCollectionName;
        this.eventHubConsumerGroup = eventHubConsumerGroup;
        this.phoneChangeTopic = phoneChangeTopic;
        this.closedAccountsTopic = closedAccountsTopic;
        this.accountStatusChangeTopic = accountStatusChangeTopic;
        this.newCustomerOnboardedTopic = newCustomerOnboardedTopic;
        this.newAccountCreatedTopic = newAccountCreatedTopic;
        this.cifIdChangeTopic = cifIdChangeTopic;
        this.ucicChangeTopic = ucicChangeTopic;
        this.eventHubConnectionString=eventHubConnectionString;
    }

private EventProcessorClient eventProcessorClient;

    @PostConstruct
    public void initialize()
    {
        eventProcessorClient= new EventProcessorClientBuilder()
                .consumerGroup(eventHubConsumerGroup)
                .connectionString(eventHubConnectionString,phoneChangeTopic)
                .processEvent(this::processEvent)
                .processError(this::processError)
                .buildEventProcessorClient();


        Disposable subscription=eventProcessorClient.start();

        CountDownLatch latch=new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(()-> {
            eventProcessorClient.stop();
            subscription.dispose();
            latch.countDown();
        }));
    }

    private void processError(ErrorContext errorContext) {
        System.out.println(errorContext.getThrowable().getMessage());
    }

    private void processEvent(EventContext eventContext)
    {
        System.out.println("Received"+eventContext.getEventData().getBodyAsString());
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("Cleanup method invoked. Closing resources...");

    }

}
*/
