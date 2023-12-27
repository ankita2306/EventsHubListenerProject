package com.indusind.INDUSIND.config;

import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Scope;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Data
@Configuration

public class CouchbaseConfig {

    @Bean
    public Cluster cluster(@Value("${couchbase.connection-string}") String connectionString,
                           @Value("${couchbase.username}") String username,
                           @Value("${couchbase.password}") String password) {
        return Cluster.connect(connectionString, username, password);
    }

    @Bean
    public Bucket bucket(Cluster cluster, @Value("${couchbase.bucket-name}") String bucketName) {
        return cluster.bucket(bucketName);
    }

    @Bean
    public Scope scope(Bucket bucket, @Value("${couchbase.scope}") String scopeName) {

        return bucket.scope(scopeName);
    }

    @Bean
    public ClusterEnvironment clusterEnvironment() {

        return ClusterEnvironment.builder().build();
    }

    @Value("${couchbase.collection.customer-id-reference}")
    private String customerIdCollectionName;

    @Value("${couchbase.collection.phone-number-reference}")
    private String phoneNumberCollectionName;

    @Value("${couchbase.collection.acid-reference}")
    private String acidCollectionName;

    @Value("${couchbase.collection.account-foracid-reference}")
    private String accountForAcidCollectionName;

    @Value("${couchbase.collection.customer-details}")
    private String customerDetailsCollectionName;

    @Value("${couchbase.collection.ucic-reference}")
    private String ucicReferenceCollectionName;


    @Value("${eventhub.consumer-group}")
    private String eventHubConsumerGroup;

    @Value("${eventhub.connection-string}")
    private String eventHubConnectionString;

    @Value("${eventhub.topic.phone-change}")
    private String phoneChangeTopic;

    @Value("${eventhub.topic.closed-accounts}")
    private String closedAccountsTopic;

    @Value("${eventhub.topic.accounts-status-change}")
    private String accountStatusChangeTopic;

    @Value("${eventhub.topic.new-customer-onboarded}")
    private String newCustomerOnboardedTopic;

    @Value("${eventhub.topic.new-account-created}")
    private String newAccountCreatedTopic;

    @Value("${eventhub.topic.cifid-change-on-accounts}")
    private String cifIdChangeTopic;

    @Value("${eventhub.topic.ucic-change}")
    private String ucicChangeTopic;


}
