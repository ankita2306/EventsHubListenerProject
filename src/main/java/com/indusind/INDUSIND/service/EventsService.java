package com.indusind.INDUSIND.service;

import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.Collection;
import com.couchbase.client.java.Scope;
import com.couchbase.client.java.env.ClusterEnvironment;
import com.couchbase.client.java.json.JsonArray;
import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.kv.GetResult;
import com.indusind.INDUSIND.exceptions.CouchbaseQueryException;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.UUID;

@Service
public class EventsService {

    private static final Logger logger = LoggerFactory.getLogger(EventsService.class);
    private final Cluster cluster;
    private final Bucket bucket;
    private final Scope scope;

    private final JSONParser jsonParser;

    @Autowired
    private BeanFactory beanFactory;

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

    @Autowired
    public EventsService(@Value("${couchbase.connection-string}") String connectionString,
                           @Value("${couchbase.username}") String username,
                           @Value("${couchbase.password}") String password,
                           @Value("${couchbase.bucket-name}") String bucketName,
                           @Value("${couchbase.scope}") String scope) {
        // Connect to Couchbase cluster
        ClusterEnvironment env = ClusterEnvironment.builder().build();
        this.cluster = Cluster.connect(connectionString, username, password);
        // Open the default bucket
        this.bucket = cluster.bucket(bucketName);
        this.scope = bucket.scope(scope);
        // Open the "masters" scope
        this.bucket.waitUntilReady(Duration.ofSeconds(10));

        jsonParser = new JSONParser();
    }

    public void consumePhoneBusinessEvents(PartitionEvent partitionEvent) {
        try {
            //JSONParser parser = new JSONParser();

            Collection custIDReferenceCollection = this.scope.collection(customerIdCollectionName);
            Collection phoneNumberReferenceCollection = this.scope.collection(phoneNumberCollectionName);

            JSONObject completeEventBody = (JSONObject) this.jsonParser.parse(partitionEvent.getData().getBodyAsString());
            JSONObject beforeEventBody = (JSONObject) completeEventBody.get("before");
            JSONObject afterEventBody = (JSONObject) completeEventBody.get("after");
            String beforeOrgKey = beforeEventBody.get("ORGKEY").toString();
            String afterOrgKey = afterEventBody.get("ORGKEY").toString();
            String phoneNoLocalCode = afterEventBody.get("PHONENOLOCALCODE").toString();

            System.out.println(beforeEventBody);
            logger.info("beforeEventBody : " + beforeEventBody);
            System.out.println(afterEventBody);
            logger.info("afterEventBody : " + afterEventBody);

            if(custIDReferenceCollection.exists(afterOrgKey).exists()) {
                String fetchedDocId = custIDReferenceCollection.get(afterOrgKey).contentAsObject().get("docId").toString();
                logger.info("fetched docId from custIDReferenceCollection : " + fetchedDocId);

                if(phoneNumberReferenceCollection.exists(phoneNoLocalCode).exists()) {
                    GetResult phoneNumberRes = phoneNumberReferenceCollection.getAndLock(phoneNoLocalCode, Duration.ofSeconds(5));
                    JsonArray docArray = phoneNumberRes.contentAsArray();     // Step 2 -> fetch the Document Array from phoneNumberReference
                    logger.info("fetched document from phoneNumberReferenceCollection : " + docArray);
                    long phoneNumberCas = phoneNumberRes.cas();

                    boolean isUpdated = false;
                    Iterator<Object> iterator = docArray.iterator();
                    while (iterator.hasNext()) {
                        JsonObject currentDoc  = (JsonObject) iterator.next();

                        if(currentDoc.containsKey("cifId") && (currentDoc.get("cifId").toString()).equals(beforeOrgKey)) {    // Step 3 -> if the doc contains beforeOrgKey then update the values
                            System.out.println("previous doc : " + currentDoc);
                            isUpdated = true;
                            currentDoc.put("docId", fetchedDocId)
                                    .put("cifId", afterOrgKey)
                                    .put("lastUpdated", Instant.now().toString());
                            System.out.println("updatedObj : " + currentDoc);
                        }
                    }
                    phoneNumberReferenceCollection.unlock(phoneNoLocalCode, phoneNumberCas);
                    logger.info("Updated document from phoneNumberReferenceCollection : " + docArray);
                    System.out.println("updated Arr" + docArray);

                    if(isUpdated) {
                        phoneNumberReferenceCollection.upsert(phoneNoLocalCode, docArray); // Step 4 -> Updating the same in couchbase
                        logger.info("Upserted document in phoneNumber_reference with PHONENOLOCALCODE {} : {}", phoneNoLocalCode, docArray);
                    }
                }
                else {
                    logger.error("no document in phoneNumber_reference exist for phoneNoLocalCode : " + phoneNoLocalCode);
                    //throw new Exception("no document in phoneNumber_reference exist for phoneNoLocalCode : " + phoneNoLocalCode);
                }
            }
            else {
                logger.error("no document exist in customerID_reference for afterOrgKey : " + afterOrgKey);
                //throw new Exception("no document exist in customerID_reference for afterOrgKey : " + afterOrgKey);
            }

        }
        catch (Exception e) {
            logger.error(e.getMessage());
            throw new CouchbaseQueryException(e.getMessage(), e);
        }
    }

    public void consumeClosedAccountsBusinessEvents(PartitionEvent partitionEvent) {
        try {
            JSONParser parser = new JSONParser();

            Collection acidReferenceCollection = this.scope.collection(acidCollectionName);
            Collection accountForAcidReferenceCollection = this.scope.collection(accountForAcidCollectionName);
            Collection custIDReferenceCollection = this.scope.collection(customerIdCollectionName);
            Collection customerDetailsReferenceCollection = this.scope.collection(customerDetailsCollectionName);


            JSONObject completeEventBody = (JSONObject) parser.parse(partitionEvent.getData().getBodyAsString());
            JSONObject afterEventBody = (JSONObject) completeEventBody.get("after");
            String cifId = afterEventBody.get("CIF_ID").toString();
            String acid = afterEventBody.get("ACID").toString();
            String forAcid = afterEventBody.get("FORACID").toString();

            logger.info("afterEventBody : " + afterEventBody);
            System.out.println(afterEventBody);
            System.out.println(cifId);

            if(acidReferenceCollection.exists(acid).exists() && accountForAcidReferenceCollection.exists(forAcid).exists()) {
                acidReferenceCollection.remove(acid);
                logger.info("document with the acid {} is removed from acid_reference", acid);
                accountForAcidReferenceCollection.remove(forAcid);
                logger.info("document with the foracid {} is removed from accountforacid_reference", forAcid);
            }
            else {
                logger.error("Document don't exist in acid_reference and accountforacid_reference for acid and forAcid  : "  + acid + " and " + forAcid);
                //throw new Exception("Document don't exist in acid_reference and accountforacid_reference for acid and forAcid  : "  + acid + " and " + forAcid);
            }

            if(custIDReferenceCollection.exists(cifId).exists()) {
                String fetchedDocId = custIDReferenceCollection.get(cifId).contentAsObject().get("docId").toString();
                logger.info("fetched docId from custIDReferenceCollection : " + fetchedDocId);

                if(customerDetailsReferenceCollection.exists(fetchedDocId).exists()) {
                    GetResult custDetailsRes = customerDetailsReferenceCollection.getAndLock(fetchedDocId, Duration.ofSeconds(10));
                    long custDetailsCas = custDetailsRes.cas();

                    JsonObject customerDetailsRecord = custDetailsRes.contentAsObject();
                    logger.info("before updation : " + customerDetailsRecord);
                    System.out.println("before updation : " + customerDetailsRecord);

                    ((JsonObject) customerDetailsRecord.get("accounts")).removeKey(acid);
                    logger.info("after updation : " + customerDetailsRecord);
                    System.out.println("after updation : " + customerDetailsRecord);

                    customerDetailsReferenceCollection.unlock(fetchedDocId, custDetailsCas);
                    customerDetailsReferenceCollection.upsert(fetchedDocId, customerDetailsRecord);
                    logger.info("document upserted in customer_details with docId {} : {}", fetchedDocId, customerDetailsRecord);
                }
                else {
                    logger.error("no document exist in customer_details for fetchedDocId : " + fetchedDocId);
                    //throw new Exception("no document exist in customer_details for fetchedDocId : " + fetchedDocId);
                }
            }
            else {
                logger.error("no document exist in customerID_reference for cifId : " + cifId);
                //throw new Exception("no document exist in customerID_reference for cifId : " + cifId);
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            throw new CouchbaseQueryException(e.getMessage(), e);
        }
    }

    public void consumeAccountStatusChangeBusinessEvents(PartitionEvent partitionEvent) {
        try {
            JSONParser parser = new JSONParser();

            Collection acidReferenceCollection = this.scope.collection(acidCollectionName);
            Collection customerDetailsReferenceCollection = this.scope.collection(customerDetailsCollectionName);

            JSONObject completeEventBody = (JSONObject) parser.parse(partitionEvent.getData().getBodyAsString());
            String currentTS = completeEventBody.get("current_ts").toString();
            JSONObject beforeEventBody = (JSONObject) completeEventBody.get("before");
            JSONObject afterEventBody = (JSONObject) completeEventBody.get("after");
            String acid = afterEventBody.get("ACID").toString();
            String accntStatus = afterEventBody.get("ACCT_STATUS") != null ? afterEventBody.get("ACCT_STATUS").toString() : "NoAccntStatus";
            System.out.println(afterEventBody);

            if(acidReferenceCollection.exists(acid).exists()) {
                String fetchedDocId = acidReferenceCollection.get(acid).contentAsObject().get("docId").toString();
                logger.info("fetched docId from acidReferenceCollection : " + fetchedDocId);

                if(customerDetailsReferenceCollection.exists(fetchedDocId).exists()) {
                    GetResult custDetailsRes = customerDetailsReferenceCollection.getAndLock(fetchedDocId, Duration.ofSeconds(10));
                    long custDetailsCas = custDetailsRes.cas();

                    JsonObject customerDetailsRecord = custDetailsRes.contentAsObject();
                    logger.info("before updation : " + customerDetailsRecord);
                    System.out.println("before updation : " + customerDetailsRecord);

                    JsonObject accountsRecord = (JsonObject) customerDetailsRecord.get("accounts");
                    JsonObject fetchedAcidRecord = (JsonObject) accountsRecord.get(acid);
                    JsonObject smtRecord = (JsonObject) fetchedAcidRecord.get("smt");
                    smtRecord.put("ACCT_STATUS", accntStatus);
                    smtRecord.put("gg_ts", currentTS);
                    smtRecord.put("lastUpdated", Instant.now().toString());
                    logger.info("after updation : " + customerDetailsRecord);
                    System.out.println("after updation : " + customerDetailsRecord);

                    customerDetailsReferenceCollection.unlock(fetchedDocId, custDetailsCas);
                    customerDetailsReferenceCollection.upsert(fetchedDocId, customerDetailsRecord);
                    logger.info("document upserted in customer_details with docId {} : {}", fetchedDocId, customerDetailsRecord);
                }
                else {
                    logger.error("Document don't exist in customer_details for fetchedDocId : "  + fetchedDocId);
                    //throw new Exception("Document don't exist in customer_details for fetchedDocId : "  + fetchedDocId);
                }
            }
            else {
                logger.error("Document don't exist in acid_reference for acid : "  + acid);
                //throw new Exception("Document don't exist in acid_reference for acid : "  + acid);
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            throw new CouchbaseQueryException(e.getMessage(), e);
        }
    }

    public void consumeNewCustomerOnboardedBusinessEvents(PartitionEvent partitionEvent) {
        try {
            JSONParser parser = new JSONParser();

            Collection custIDReferenceCollection = this.scope.collection(customerIdCollectionName);
            Collection customerDetailsReferenceCollection = this.scope.collection(customerDetailsCollectionName);

            JSONObject completeEventBody = (JSONObject) parser.parse(partitionEvent.getData().getBodyAsString());
            JSONObject afterEventBody = (JSONObject) completeEventBody.get("after");
            String orgkey = afterEventBody.get("ORGKEY").toString();
            String uuId = UUID.randomUUID().toString();
            JsonObject custIdReferenceObj = JsonObject.create();
            custIdReferenceObj.put("docId", uuId);
            System.out.println(custIdReferenceObj);

            JsonObject customerDetailsReferenceObj = JsonObject.create();
            customerDetailsReferenceObj.put("customer", afterEventBody);
            System.out.println(customerDetailsReferenceObj);
            custIDReferenceCollection.upsert(orgkey, custIdReferenceObj);
            logger.info("data is inserted in customerID_reference with orgkey {} is : {}", orgkey, custIdReferenceObj);
            customerDetailsReferenceCollection.upsert(uuId,customerDetailsReferenceObj);
            logger.info("data is inserted in customer_details with docId {} is : {}", uuId, customerDetailsReferenceObj);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            throw new CouchbaseQueryException(e.getMessage(), e);
        }
    }

    public void consumeNewAccountCreatedBusinessEvents(PartitionEvent partitionEvent) {
        try {
            JSONParser parser = new JSONParser();

            Collection custIDReferenceCollection = this.scope.collection(customerIdCollectionName);
            Collection acidReferenceCollection = this.scope.collection(acidCollectionName);
            Collection accountForAcidReferenceCollection = this.scope.collection(accountForAcidCollectionName);
            JSONObject completeEventBody = (JSONObject) parser.parse(partitionEvent.getData().getBodyAsString());
            JSONObject afterEventBody = (JSONObject) completeEventBody.get("after");
            //String cifId = afterEventBody.get("CIF_ID").toString();
            String cifId = "11304947";
            String acid = afterEventBody.get("ACID").toString();
            String forAcid = afterEventBody.get("FORACID").toString();
            JsonObject accountReferenceObj = JsonObject.create();

            if(custIDReferenceCollection.exists(cifId).exists()) {
                String fetchedDocId = custIDReferenceCollection.get(cifId).contentAsObject().get("docId").toString();
                logger.info("fetched docId from custIDReferenceCollection : " + fetchedDocId);

                accountReferenceObj.put("cifId", cifId)
                        .put("docId", fetchedDocId)
                        .put("foracid", forAcid)
                        .put("acid", acid);

                System.out.println(accountReferenceObj);
                acidReferenceCollection.upsert(acid, accountReferenceObj);
                logger.info("data is upserted in acid_reference with acid '{}' is : {}", acid, accountReferenceObj);
                accountForAcidReferenceCollection.upsert(forAcid, accountReferenceObj);
                logger.info("data is upserted in accountforacid_reference with forAcid '{}' is : {}", forAcid, accountReferenceObj);
            }
            else {
                logger.error("Document don't exist in customerID_reference for cifId : "  + cifId);
                //throw new Exception("Document don't exist in customerID_reference for cifId : "  + cifId);
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            throw new CouchbaseQueryException(e.getMessage(), e);
        }
    }

    public void consumeUcicReferenceBusinessEvents(PartitionEvent partitionEvent) {
        try {
            JSONParser parser = new JSONParser();

            Collection custIDReferenceCollection = this.scope.collection(customerIdCollectionName);
            Collection customerDetailsReferenceCollection = this.scope.collection(customerDetailsCollectionName);
            Collection ucicReferenceCollection = this.scope.collection(ucicReferenceCollectionName);


            //JSONObject completeEventBody = (JSONObject) parser.parse(partitionEvent.getData().getBodyAsString());
            //JSONObject beforeEventBody = (JSONObject) completeEventBody.get("before");
            //JSONObject afterEventBody = (JSONObject) completeEventBody.get("after");
            //String beforeFinacleCif = beforeEventBody.get("FINACLE_CIF").toString();
            //String afterFinacleCif = afterEventBody.get("FINACLE_CIF").toString();
            String afterFinacleCif = "11304948";
            //String entCif = afterEventBody.get("ENT_CIF").toString();
            String entCif = "12121211";

            //System.out.println(beforeEventBody);
            //System.out.println(afterEventBody);

            // compatibility of app.properties file with the standalone application

            if(custIDReferenceCollection.exists(afterFinacleCif).exists()) {
                String fetchedDocId = custIDReferenceCollection.get(afterFinacleCif).contentAsObject().get("docId").toString();
                logger.info("fetched docId from custIDReferenceCollection : " + fetchedDocId);

                if(customerDetailsReferenceCollection.exists(fetchedDocId).exists()) {
                    GetResult custDetailsRes = customerDetailsReferenceCollection.getAndLock(fetchedDocId, Duration.ofSeconds(10));
                    long custDetailsCas = custDetailsRes.cas();

                    JsonObject customerDetailsRecord = custDetailsRes.contentAsObject();
                    System.out.println("before updation : " + customerDetailsRecord);
                    logger.info("before updation beforeCustomerDetailsRecord : " + customerDetailsRecord);

                    JsonObject custCifObj = JsonObject.create();
                    custCifObj.put("testKey", "testValue");
                    customerDetailsRecord.put("cust_cif", custCifObj);

                    JsonObject ucicObj = JsonObject.create();
                    String uuid = UUID.randomUUID().toString();
                    ucicObj.put("docId", uuid);

                    customerDetailsReferenceCollection.unlock(fetchedDocId, custDetailsCas);
                    customerDetailsReferenceCollection.upsert(fetchedDocId, customerDetailsRecord);
                    logger.info("data upserted in customer_details with docId {} : {}", fetchedDocId, customerDetailsRecord);
                    ucicReferenceCollection.upsert(entCif, ucicObj);
                    logger.info("data upserted in ucic_reference with entCif {} : {}", entCif, ucicObj);
                }
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            throw new CouchbaseQueryException(e.getMessage(), e);
        }
    }

    public void consumeCifIdChangeOnAccountsBusinessEvents(PartitionEvent partitionEvent) {
        try {
            JSONParser parser = new JSONParser();

            Collection custIDReferenceCollection = this.scope.collection(customerIdCollectionName);
            Collection customerDetailsReferenceCollection = this.scope.collection(customerDetailsCollectionName);


            JSONObject completeEventBody = (JSONObject) parser.parse(partitionEvent.getData().getBodyAsString());
            JSONObject beforeEventBody = (JSONObject) completeEventBody.get("before");
            JSONObject afterEventBody = (JSONObject) completeEventBody.get("after");
            String beforeCifId = beforeEventBody.get("CIF_ID").toString();
            String afterCifId = afterEventBody.get("CIF_ID").toString();
            String acid = afterEventBody.get("ACID").toString();

            System.out.println(beforeEventBody);
            System.out.println(afterEventBody);

            if(custIDReferenceCollection.exists(beforeCifId).exists() && custIDReferenceCollection.exists(afterCifId).exists()) {
                String beforeDocId = custIDReferenceCollection.get(beforeCifId).contentAsObject().get("docId").toString();
                logger.info("fetched beforeDocId from custIDReferenceCollection : " + beforeDocId);

                String afterDocId = custIDReferenceCollection.get(afterCifId).contentAsObject().get("docId").toString();
                logger.info("fetched afterDocId from custIDReferenceCollection : " + afterDocId);


                if(customerDetailsReferenceCollection.exists(beforeDocId).exists() && customerDetailsReferenceCollection.exists(afterDocId).exists()) {
                    GetResult beforeCustDetailsRes = customerDetailsReferenceCollection.getAndLock(beforeDocId, Duration.ofSeconds(10));
                    long beforeCustDetailsCas = beforeCustDetailsRes.cas();

                    JsonObject beforeCustomerDetailsRecord = beforeCustDetailsRes.contentAsObject();
                    System.out.println("before updation : " + beforeCustomerDetailsRecord);
                    logger.info("before updation beforeCustomerDetailsRecord : " + beforeCustomerDetailsRecord);

                    JsonObject beforeAccObj = (JsonObject) beforeCustomerDetailsRecord.get("accounts");
                    JsonObject beforeAcidObj = (JsonObject) beforeAccObj.get(acid);
                    JsonObject beforeGamObj = (JsonObject) beforeAcidObj.get("gam");
                    beforeGamObj.put("CIF_ID", afterCifId);
                    System.out.println("after updation : " + beforeCustomerDetailsRecord);
                    logger.info("after updation beforeCustomerDetailsRecord : " + beforeCustomerDetailsRecord);

                    GetResult afterCustDetailsRes = customerDetailsReferenceCollection.getAndLock(afterDocId, Duration.ofSeconds(10));
                    long afterCustDetailsCas = afterCustDetailsRes.cas();

                    JsonObject afterCustomerDetailsRecord = afterCustDetailsRes.contentAsObject();
                    System.out.println("before updation : " + afterCustomerDetailsRecord);
                    logger.info("before updation afterCustomerDetailsRecord : " + afterCustomerDetailsRecord);

                    JsonObject afterAccObj = (JsonObject) afterCustomerDetailsRecord.get("accounts");
                    afterAccObj.put(acid, beforeAcidObj);
                    System.out.println("after updation : " + afterCustomerDetailsRecord);
                    logger.info("after updation afterCustomerDetailsRecord : " + afterCustomerDetailsRecord);

                    beforeAccObj.removeKey(acid);
                    customerDetailsReferenceCollection.unlock(beforeDocId, beforeCustDetailsCas);
                    customerDetailsReferenceCollection.unlock(afterDocId, afterCustDetailsCas);
                    customerDetailsReferenceCollection.upsert(beforeDocId, beforeCustomerDetailsRecord);
                    logger.info("data upserted in customer_details with docId {} : {}", beforeDocId, beforeCustomerDetailsRecord);
                    customerDetailsReferenceCollection.upsert(afterDocId, afterCustomerDetailsRecord);
                    logger.info("data upserted in customer_details with docId {} : {}", afterDocId, afterCustomerDetailsRecord);
                }
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            throw new CouchbaseQueryException(e.getMessage(), e);
        }
    }
}
