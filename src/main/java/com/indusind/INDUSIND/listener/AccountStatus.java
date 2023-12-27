/*
package com.indusind.INDUSIND.listener;


import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
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

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;


@Component
public class AccountStatus {



        private static final Logger logger = LoggerFactory.getLogger(EventsService.class);

        private final Bucket bucket;


        private final String eventHubConsumerGroup;

        private final String eventHubConnectionString;


        private final String accountStatusChangeTopic;


        @Autowired
        private BeanFactory beanFactory;

        @Autowired
        private EventsService eventsService;

        private final String newAccountCreatedTopic;



        @Autowired
        public AccountStatus(Cluster cluster,
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
            this.bucket.waitUntilReady(Duration.ofSeconds(10));
            this.eventHubConsumerGroup = eventHubConsumerGroup;
            this.accountStatusChangeTopic = accountStatusChangeTopic;
            this.eventHubConnectionString=eventHubConnectionString;
            this.newAccountCreatedTopic=newAccountCreatedTopic;
        }


        @PostConstruct
        public void initialize()
        {
            System.out.println("initialise AccouStatusCange");

            CountDownLatch countDownLatch = new CountDownLatch(1);
     List<String> topics= Arrays.asList("ucic_change","closed_accounts");


            for (int i = 0; i < topics.size(); i++) {
                new EventHubClientBuilder()
                        .consumerGroup(eventHubConsumerGroup)
                        .connectionString(eventHubConnectionString,topics.get(i))
                        .buildAsyncConsumerClient()
                        .receive()
                        .subscribe(partitionEvent->{
                                    System.out.println("-----"+"------"+ partitionEvent.getData().getBodyAsString());
                                    //eventsService.consumeAccountStatusChangeBusinessEvents();
                                },
                                throwable -> {
                                    System.err.println("Error occurred: " + throwable.getMessage());
                                },
                                countDownLatch::countDown
                        );
            }



            try {
                countDownLatch.await();
            }
            catch (InterruptedException e)
            {
                logger.error("Error waiting for latch: {}", e.getMessage());
            }
        }
        @PreDestroy
        public void cleanup() {
            System.out.println("Cleanup method invoked. Closing resources...");

        }

    }


*/
