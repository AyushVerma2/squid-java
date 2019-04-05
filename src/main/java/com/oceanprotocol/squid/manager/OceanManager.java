/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.manager;

import com.oceanprotocol.keeper.contracts.EscrowAccessSecretStoreTemplate;
import com.oceanprotocol.squid.core.sla.ServiceAgreementHandler;
import com.oceanprotocol.squid.core.sla.functions.FulfillEscrowReward;
import com.oceanprotocol.squid.core.sla.functions.FulfillLockReward;
import com.oceanprotocol.squid.exceptions.*;
import com.oceanprotocol.squid.external.AquariusService;
import com.oceanprotocol.squid.external.BrizoService;
import com.oceanprotocol.squid.external.KeeperService;
import com.oceanprotocol.squid.helpers.EncodingHelper;
import com.oceanprotocol.squid.helpers.EthereumHelper;
import com.oceanprotocol.squid.helpers.UrlHelper;
import com.oceanprotocol.squid.models.DDO;
import com.oceanprotocol.squid.models.DID;
import com.oceanprotocol.squid.models.Order;
import com.oceanprotocol.squid.models.asset.AssetMetadata;
import com.oceanprotocol.squid.models.asset.BasicAssetInfo;
import com.oceanprotocol.squid.models.asset.OrderResult;
import com.oceanprotocol.squid.models.brizo.InitializeAccessSLA;
import com.oceanprotocol.squid.models.service.*;
import io.reactivex.Flowable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Handles several operations related with Ocean's flow
 */
public class OceanManager extends BaseManager {

    private static final Logger log= LogManager.getLogger(OceanManager.class);

    protected OceanManager(KeeperService keeperService, AquariusService aquariusService) {
        super(keeperService, aquariusService);
    }

    /**
     * Given the KeeperService and AquariusService, returns a new instance of OceanManager
     * using them as attributes
     * @param keeperService Keeper Dto
     * @param aquariusService Provider Dto
     * @return OceanManager
     */
    public static OceanManager getInstance(KeeperService keeperService, AquariusService aquariusService) {
        return new OceanManager(keeperService, aquariusService);
    }

    /**
     * Given a DDO, returns a DID created using the ddo
     * @param ddo the DDO
     * @return DID
     * @throws DIDFormatException DIDFormatException
     */
    public DID generateDID(DDO ddo) throws DIDFormatException {
        return DID.builder();
    }

    /**
     * Given a DID, scans the DIDRegistry events on-chain to resolve the
     * Metadata API url and return the DDO found
     * @param did the did
     * @return DDO
     * @throws EthereumException EthereumException
     * @throws DDOException  DDOException
     */
    public DDO resolveDID(DID did) throws EthereumException, DDOException {

        EthFilter didFilter = new EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                didRegistry.getContractAddress()
        );

        try{

            final Event event= didRegistry.DIDATTRIBUTEREGISTERED_EVENT;
            final String eventSignature= EventEncoder.encode(event);
            didFilter.addSingleTopic(eventSignature);

            String didTopic= "0x" + did.getHash();
            didFilter.addOptionalTopics(didTopic);

            EthLog ethLog;

            try {
                ethLog = getKeeperService().getWeb3().ethGetLogs(didFilter).send();
            }catch(IOException e){
                throw new EthereumException("Error searching DID " + did.toString() + " onchain: " + e.getMessage());
            }

            List<EthLog.LogResult> logs = ethLog.getLogs();

            int numLogs= logs.size();
            if (numLogs<1)
                throw new DDOException("No events found for " + did.toString());

            EthLog.LogResult logResult= logs.get(numLogs-1);
            List<Type> nonIndexed= FunctionReturnDecoder.decode(((EthLog.LogObject) logResult).getData(), event.getNonIndexedParameters());
            String ddoUrl= nonIndexed.get(0).getValue().toString();
            String didUrl= UrlHelper.parseDDOUrl(ddoUrl, did.toString());

            AquariusService ddoAquariosDto= AquariusService.getInstance(UrlHelper.getBaseUrl(didUrl));
            return ddoAquariosDto.getDDO(didUrl);

        } catch (Exception ex)  {
            log.error("Unable to retrieve DDO " + ex.getMessage());
            throw new DDOException("Unable to retrieve DDO " + ex.getMessage());
        }
    }


    /**
     * Given a DID and a Metadata API url, register on-chain the DID.
     * It allows to resolve DDO's using DID's as input
     * @param did the did
     * @param url metadata url
     * @param checksum calculated hash of the metadata
     * @param providers list of providers addresses to give access
     * @return boolean success
     * @throws DIDRegisterException DIDRegisterException
     */
    public boolean registerDID(DID did, String url, String checksum, List<String> providers) throws DIDRegisterException{
        log.debug("Registering DID " + did.getHash() + " into Registry " + didRegistry.getContractAddress());


        try {

            TransactionReceipt receipt = didRegistry.registerAttribute(
                    EncodingHelper.hexStringToBytes(did.getHash()),
                    EncodingHelper.hexStringToBytes(checksum.replace("0x", "")),
                    providers,
                    url
            ).send();

            return receipt.getStatus().equals("0x1");

        } catch (Exception e){
            throw new DIDRegisterException("Error registering DID " + did.getHash(), e);
        }
    }

    /**
     * Creates a new DDO, registering it on-chain through DidRegistry contract and off-chain in Aquarius
     * @param metadata the metadata
     * @param providerConfig the service Endpoints
     * @param threshold secret store threshold
     * @return an instance of the DDO created
     * @throws DDOException DDOException
     */
    public DDO registerAsset(AssetMetadata metadata, ProviderConfig providerConfig, int threshold) throws DDOException {

        try {

            // Definition of service endpoints
            String metadataEndpoint;
            if (providerConfig.getMetadataEndpoint() == null)
                metadataEndpoint = getAquariusService().getDdoEndpoint() + "/{did}";
            else
                metadataEndpoint = providerConfig.getMetadataEndpoint();

            // Initialization of services supported for this asset
            MetadataService metadataService = new MetadataService(metadata, metadataEndpoint, Service.DEFAULT_METADATA_SERVICE_ID);


            AuthorizationService authorizationService = null;
            //Adding the authorization service if the endpoint is defined
            if (providerConfig.getSecretStoreEndpoint()!=null && !providerConfig.getSecretStoreEndpoint().equals("")){
                authorizationService = new AuthorizationService(Service.serviceTypes.Authorization, providerConfig.getSecretStoreEndpoint(), Service.DEFAULT_AUTHORIZATION_SERVICE_ID);
            }

            // Initializing DDO
            DDO ddo = this.buildDDO(metadataService, authorizationService, getMainAccount().address, threshold);

            // Definition of a DEFAULT ServiceAgreement Contract
            AccessService.ServiceAgreementTemplate serviceAgreementTemplate = new AccessService.ServiceAgreementTemplate();
            serviceAgreementTemplate.contractName = "EscrowAccessSecretStoreTemplate";

            // AgreementCreated Event
            Condition.Event executeAgreementEvent = new Condition.Event();
            executeAgreementEvent.name = "AgreementCreated";
            executeAgreementEvent.actorType = "consumer";
            // Handler
            Condition.Handler handler = new Condition.Handler();
            handler.moduleName = "escrowAccessSecretStoreTemplate";
            handler.functionName = "escrowAccessSecretStoreTemplate";
            handler.version = "0.1";
            executeAgreementEvent.handler = handler;

            serviceAgreementTemplate.events = Arrays.asList(executeAgreementEvent);

            // The templateId of the AccessService is the address of the escrowAccessSecretStoreTemplate contract
            String accessServiceTemplateId = escrowAccessSecretStoreTemplate.getContractAddress();
            AccessService accessService = new AccessService(providerConfig.getAccessEndpoint(),
                    Service.DEFAULT_ACCESS_SERVICE_ID,
                    serviceAgreementTemplate,
                    accessServiceTemplateId);
            accessService.purchaseEndpoint = providerConfig.getPurchaseEndpoint();
            accessService.name = "dataAssetAccessServiceAgreement";

            // Initializing conditions and adding to Access service
            ServiceAgreementHandler sla = new ServiceAgreementHandler();
            accessService.serviceAgreementTemplate.conditions = sla.initializeConditions(
                    //accessService.templateId,
                    //getContractAddresses(),
                    getAccessConditionParams(ddo.getDid().toString(), Integer.parseInt(metadata.base.price)));

            // Adding services to DDO
            ddo.addService(accessService);
            if (authorizationService!= null)
                ddo.addService(authorizationService);

            // Storing DDO
            DDO createdDDO = getAquariusService().createDDO(ddo);

            // Registering DID
            registerDID(ddo.getDid(), metadataEndpoint, metadata.base.checksum, providerConfig.getProviderAddresses());

            return createdDDO;
        }catch (DDOException e) {
            throw e;
        }catch (InitializeConditionsException|DIDRegisterException e) {
            throw new DDOException("Error registering Asset." , e);
        }

    }


    /**
     * Purchases an Asset represented by a DID. It implies to initialize a Service Agreement between publisher and consumer
     * @param did the did
     * @param serviceDefinitionId the service definition id
     * @return a Flowable instance over an OrderResult to get the result of the flow in an asynchronous fashion
     * @throws OrderException OrderException
     */
    public Flowable<OrderResult> purchaseAsset(DID did, String serviceDefinitionId)
            throws OrderException {

        String serviceAgreementId = ServiceAgreementHandler.generateSlaId();
        DDO ddo;
        // Checking if DDO is already there and serviceDefinitionId is included
        try {

            ddo= resolveDID(did);
        } catch (DDOException|EthereumException e) {
            log.error("Error resolving did[" + did.getHash() + "]: " + e.getMessage());
            throw new OrderException("Error processing Order with DID " + did.getDid(), e);
        }

        try {

            return this.initializeServiceAgreement(did, ddo, serviceDefinitionId, serviceAgreementId)
                    .map(event -> EncodingHelper.toHexString(event._agreementId))
                    .firstOrError()
                    .toFlowable()
                    .switchMap(eventServiceAgreementId -> {
                        if (eventServiceAgreementId.isEmpty())
                            return Flowable.empty();
                        else {
                            log.debug("Received AgreementCreated Event with Id: " + eventServiceAgreementId);
                            getKeeperService().tokenApprove(this.tokenContract, lockRewardCondition.getContractAddress(), Integer.valueOf(ddo.metadata.base.price));
                            BigInteger balance = this.tokenContract.balanceOf(getMainAccount().address).send();
                            if (balance.compareTo(BigInteger.valueOf(Long.parseLong(ddo.metadata.base.price))) < 0) {
                                log.warn("Consumer account does not have sufficient token balance to fulfill the " +
                                        "LockRewardCondition. Do `requestTokens` using the `dispenser` contract then try this again.");
                                log.info("token balance is: " + balance + " price is: " + ddo.metadata.base.price);
                                throw new Exception("LockRewardCondition.fulfill will fail due to insufficient token balance in the consumer account.");
                            }
                            this.fulfillLockReward(ddo, serviceDefinitionId, eventServiceAgreementId);
                            return ServiceAgreementHandler.listenForFulfilledEvent(accessSecretStoreCondition, serviceAgreementId);
                        }
                    })
                    .map( event ->  new OrderResult(serviceAgreementId, true, false))
                    // TODO timout of the condition
                    .timeout(60, TimeUnit.SECONDS)
                    .onErrorReturn(throwable -> {

                        if (throwable instanceof TimeoutException){
                            // If we get a timeout listening for an AccessSecretStoreCondition Fulfilled Event,
                            // we must perform a refund executing escrowReward.fulfill
                            this.fulfillEscrowReward(ddo, serviceDefinitionId, serviceAgreementId);
                            return new OrderResult(serviceAgreementId, false, true);
                        }

                        String msg = "There was a problem executing the Service Agreement " + serviceAgreementId;
                        throw new ServiceAgreementException(serviceAgreementId, msg, throwable);
                    });

        }catch (DDOException|ServiceException|ServiceAgreementException e){
            String msg = "Error processing Order with DID " + did.getDid() + "and ServiceAgreementID " + serviceAgreementId;
            log.error(msg  + ": " + e.getMessage());
            throw new OrderException(msg, e);
        }

    }

    /**
     * Initialize a new ServiceExecutionAgreement between a publisher and a consumer
     * @param did the did
     * @param ddo the ddi
     * @param serviceDefinitionId the service definition id
     * @param serviceAgreementId the service agreement id
     * @return a Flowable over an AgreementInitializedEventResponse
     * @throws DDOException DDOException
     * @throws ServiceException ServiceException
     * @throws ServiceAgreementException ServiceAgreementException
     */
    private Flowable<EscrowAccessSecretStoreTemplate.AgreementCreatedEventResponse> initializeServiceAgreement(DID did, DDO ddo, String serviceDefinitionId, String serviceAgreementId)
            throws  DDOException, ServiceException, ServiceAgreementException {

        AccessService accessService= ddo.getAccessService(serviceDefinitionId);

        //  Consumer sign service details. It includes:
        // (templateId, conditionKeys, valuesHashList, timeoutValues, serviceAgreementId)
        String agreementSignature;
        try {
            agreementSignature = accessService.generateServiceAgreementSignature(
                    getKeeperService().getWeb3(),
                    getMainAccount().getAddress(),
                    getMainAccount().getPassword(),
                    ddo.proof.creator,
                    serviceAgreementId,
                    lockRewardCondition.getContractAddress(),
                    accessSecretStoreCondition.getContractAddress(),
                    escrowReward.getContractAddress()
            );
        }catch(IOException e){
            String msg = "Error generating signature for Service Agreement: " + serviceAgreementId;
            log.error(msg+ ": " + e.getMessage());
            throw new ServiceAgreementException(serviceAgreementId, msg, e);
        }

        InitializeAccessSLA initializePayload= new InitializeAccessSLA(
                did.toString(),
                "0x".concat(serviceAgreementId),
                serviceDefinitionId,
                agreementSignature,
                Keys.toChecksumAddress(getMainAccount().getAddress())
        );

        // 3. Send agreement details to Publisher (Brizo endpoint)
        boolean isInitialized= BrizoService.initializeAccessServiceAgreement(accessService.purchaseEndpoint, initializePayload);

        if (!isInitialized)  {
            throw new ServiceAgreementException(serviceAgreementId, "Unable to initialize SLA using Brizo. Payload: " + initializePayload);
        }

        // 4. Listening of events
        return  ServiceAgreementHandler.listenExecuteAgreement(escrowAccessSecretStoreTemplate, serviceAgreementId);

    }


    /**
     * Executes the fulfill of the LockRewardCondition
     * @param ddo the ddo
     * @param serviceDefinitionId the serviceDefinition id
     * @param serviceAgreementId service agreement id
     * @return a flag that indicates if the function was executed correctly
     * @throws ServiceException ServiceException
     * @throws LockRewardFulfillException LockRewardFulfillException
     */
    private boolean fulfillLockReward(DDO ddo, String serviceDefinitionId, String serviceAgreementId) throws ServiceException, LockRewardFulfillException {

        AccessService accessService= ddo.getAccessService(serviceDefinitionId);
        BasicAssetInfo assetInfo = getBasicAssetInfo(accessService);

        return FulfillLockReward.executeFulfill(lockRewardCondition, serviceAgreementId, this.escrowReward.getContractAddress(), assetInfo);
    }

    /**
     * Executes the fulfill of the EscrowReward
     * @param ddo the ddo
     * @param serviceDefinitionId the serviceDefinition id
     * @param serviceAgreementId service agreement id
     * @return a flag that indicates if the function was executed correctly
     * @throws ServiceException ServiceException
     * @throws EscrowRewardException EscrowRewardException
     */
    private boolean fulfillEscrowReward(DDO ddo, String serviceDefinitionId, String serviceAgreementId) throws ServiceException, EscrowRewardException {

        AccessService accessService= ddo.getAccessService(serviceDefinitionId);
        BasicAssetInfo assetInfo = getBasicAssetInfo(accessService);

        String lockRewardConditionId = "";
        String accessSecretStoreConditionId = "";

        try {

            lockRewardConditionId = accessService.generateLockRewardId(serviceAgreementId, escrowReward.getContractAddress(), lockRewardCondition.getContractAddress());
            accessSecretStoreConditionId = accessService.generateAccessSecretStoreConditionId(serviceAgreementId, getMainAccount().getAddress(), accessSecretStoreCondition.getContractAddress());
        }
        catch (UnsupportedEncodingException e) {
            throw new EscrowRewardException("Error generating the condition Ids ", e);
        }


        return FulfillEscrowReward.executeFulfill(escrowReward,
                serviceAgreementId,
                this.lockRewardCondition.getContractAddress(),
                assetInfo,
                this.getMainAccount().address,
                lockRewardConditionId,
                accessSecretStoreConditionId);
    }


    /**
     *  Downloads an Asset previously ordered through a Service Agreement
     * @param serviceAgreementId the service agreement id
     * @param did the did
     * @param serviceDefinitionId the service definition id
     * @param basePath the path where the asset will be downloaded
     * @return a flag that indicates if the consume operation was executed correctly
     * @throws ConsumeServiceException ConsumeServiceException
     */
    public boolean consume(String serviceAgreementId, DID did, String serviceDefinitionId, String basePath) throws ConsumeServiceException {

        return consume(serviceAgreementId, did, serviceDefinitionId, basePath,0);
    }


    /**
     * Downloads an Asset previously ordered through a Service Agreement
     * @param serviceAgreementId the service agreement id
     * @param did the did
     * @param serviceDefinitionId the service definition id
     * @param basePath the path where the asset will be downloaded
     * @param threshold secret store threshold
     * @return a flag that indicates if the consume operation was executed correctly
     * @throws ConsumeServiceException ConsumeServiceException
     */
    public boolean consume(String serviceAgreementId, DID did, String serviceDefinitionId, String basePath, int threshold) throws ConsumeServiceException{

        DDO ddo;
        String checkConsumerAddress = Keys.toChecksumAddress(getMainAccount().address);
        String serviceEndpoint;
        List<AssetMetadata.File> files;

        String agreementId = EthereumHelper.add0x(serviceAgreementId);

        try {

            ddo = resolveDID(did);
            serviceEndpoint = ddo.getAccessService(serviceDefinitionId).serviceEndpoint;

            files = this.getMetadataFiles(ddo);

        }catch (EthereumException|DDOException|ServiceException|EncryptionException|IOException e) {
            String msg = "Error consuming asset with DID " + did.getDid() +" and Service Agreement " + agreementId;
            log.error(msg+ ": " + e.getMessage());
            throw new ConsumeServiceException(msg, e);
        }

        for (AssetMetadata.File file: files) {

            // For each url we call to consume Brizo endpoint that requires consumerAddress, serviceAgreementId and url as a parameters
            try {

                if (null == file.url)    {
                    String msg = "Error Decrypting URL for Asset: " + did.getDid() +" and Service Agreement " + agreementId
                            + " URL received: " + file.url;
                    log.error(msg);
                    throw new ConsumeServiceException(msg);
                }
                String fileName = file.url.substring(file.url.lastIndexOf("/") + 1);
                String destinationPath = basePath + File.separator + fileName;

                BrizoService.downloadUrl(serviceEndpoint, checkConsumerAddress, serviceAgreementId, file.url, destinationPath);

            } catch (IOException e) {
                String msg = "Error consuming asset with DID " + did.getDid() +" and Service Agreement " + serviceAgreementId;

                log.error(msg+ ": " + e.getMessage());
                throw new ConsumeServiceException(msg, e);
            }

        }

        return true;
    }

    // TODO: to be implemented
    public Order getOrder(String orderId)   {
        return null;
    }

    // TODO: to be implemented
    public List<AssetMetadata> searchOrders()   {
        return new ArrayList<>();
    }


    /**
     * Gets the Access Conditions Params of a DDO
     * @param did the did
     * @param price the price
     * @return a Map with the params of the Access Conditions
     */
    private Map<String, Object> getAccessConditionParams(String did, int price)  {
        Map<String, Object> params= new HashMap<>();
        params.put("parameter.did", did);
        params.put("parameter.price", price);

        //config.getString("")
        params.put("contract.EscrowReward.address", escrowReward.getContractAddress());
        params.put("contract.LockRewardCondition.address", lockRewardCondition.getContractAddress());
        params.put("contract.AccessSecretStoreCondition.address", accessSecretStoreCondition.getContractAddress());

        params.put("parameter.assetId", did.replace("did:op:", "0x"));

        return params;
    }


    /**
     * Gets some basic info of an Access Service
     * @param accessService the access service
     * @return BasicAssetInfo
     */
    private  BasicAssetInfo getBasicAssetInfo( AccessService accessService) {

        BasicAssetInfo assetInfo =  new BasicAssetInfo();

        try {

            Condition lockRewardCondition = accessService.getConditionbyName("lockReward");
            Condition.ConditionParameter amount = lockRewardCondition.getParameterByName("_amount");

            Condition accessSecretStoreCondition = accessService.getConditionbyName("accessSecretStore");
            Condition.ConditionParameter documentId = accessSecretStoreCondition.getParameterByName("_documentId");

            assetInfo.setPrice((Integer) amount.value);
            assetInfo.setAssetId(EncodingHelper.hexStringToBytes((String) documentId.value));


        } catch (UnsupportedEncodingException e) {
            log.error("Exception encoding serviceAgreement " + e.getMessage());

        }

        return assetInfo;

    }


}
