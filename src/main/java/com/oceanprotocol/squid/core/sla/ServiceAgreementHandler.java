/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.core.sla;

import com.fasterxml.jackson.core.type.TypeReference;
import com.oceanprotocol.keeper.contracts.AccessSecretStoreCondition;
import com.oceanprotocol.keeper.contracts.EscrowAccessSecretStoreTemplate;
import com.oceanprotocol.squid.exceptions.InitializeConditionsException;
import com.oceanprotocol.squid.helpers.CryptoHelper;
import com.oceanprotocol.squid.helpers.EthereumHelper;
import com.oceanprotocol.squid.models.AbstractModel;
import com.oceanprotocol.squid.models.service.Condition;
import io.reactivex.Flowable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.crypto.Hash;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


/**
 * Handles functionality related with the execution of a Service Agreement
 */
public class ServiceAgreementHandler {

    static final Logger log= LogManager.getLogger(ServiceAgreementHandler.class);

    private static final String ACCESS_CONDITIONS_FILE_TEMPLATE= "src/main/resources/sla/sla-access-conditions-template.json";
    private String conditionsTemplate= null;

    public static final String FUNCTION_LOCKREWARD_DEF= "fulfill(bytes32,address,uint256)";
    public static final String FUNCTION_ACCESSSECRETSTORE_DEF= "grantAccess(bytes32,bytes32,address)";
    public static final String FUNCTION_ESCROWREWARD_DEF= "escrowReward(bytes32,uint256,address,address,bytes32,bytes32)";



    /**
     * Generates a new and random Service Agreement Id
     * @return a String with the new Service Agreement Id
     */
    public static String generateSlaId()    {
        String token= UUID.randomUUID().toString() + UUID.randomUUID().toString();
        return token.replaceAll("-", "");
    }

    /**
     * Define and execute a Filter over the Service Agreement Contract to listen for an AgreementInitialized event
     * @param slaContract the address of the service agreement contract
     * @param serviceAgreementId the service agreement Id
     * @return a Flowable over the Event to handle it in an asynchronous fashion
     */
    public static Flowable<EscrowAccessSecretStoreTemplate.AgreementCreatedEventResponse> listenExecuteAgreement(EscrowAccessSecretStoreTemplate slaContract, String serviceAgreementId)   {
        EthFilter slaFilter = new EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                slaContract.getContractAddress()
        );

        final Event event= slaContract.AGREEMENTCREATED_EVENT;
        final String eventSignature= EventEncoder.encode(event);
        String slaTopic= "0x" + serviceAgreementId;
        slaFilter.addSingleTopic(eventSignature);
        slaFilter.addOptionalTopics(slaTopic);

        return slaContract.agreementCreatedEventFlowable(slaFilter);
    }


    /**
     * Define and execute a Filter over the AccessSecretStoreCondition Contract to listen for an Fulfilled event
     * @param accessCondition the address of the AccessSecretStoreCondition contract
     * @param serviceAgreementId the serviceAgreement Id
     * @return a Flowable over the Event to handle it in an asynchronous fashion
     */
    public static Flowable<AccessSecretStoreCondition.FulfilledEventResponse> listenForFulfilledEvent(AccessSecretStoreCondition accessCondition,
                                                                                                      String serviceAgreementId)   {

        EthFilter grantedFilter = new EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                accessCondition.getContractAddress()
        );

        final Event event= AccessSecretStoreCondition.FULFILLED_EVENT;
        final String eventSignature= EventEncoder.encode(event);
        String slaTopic= "0x" + serviceAgreementId;

        grantedFilter.addSingleTopic(eventSignature);
        grantedFilter.addOptionalTopics(slaTopic);


        return accessCondition.fulfilledEventFlowable(grantedFilter);
    }


    /*
     * Define and execute a Filter over the Payment Condition Contract to listen for an PaymentRefund event
     * @param paymentConditions the address of the PaymentConditions
     * @param serviceAgreementId the service Agreement Id
     * @return a Flowable over the Event to handle it in an asynchronous fashion
     */
    /*
    public static Flowable<PaymentConditions.PaymentRefundEventResponse> listenForPaymentRefund(PaymentConditions paymentConditions,
                                                                                                           String serviceAgreementId)   {
        EthFilter refundFilter = new EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                paymentConditions.getContractAddress()
        );

        final Event event= PaymentConditions.PAYMENTREFUND_EVENT;
        final String eventSignature= EventEncoder.encode(event);
        String slaTopic= "0x" + serviceAgreementId;

        refundFilter.addSingleTopic(eventSignature);
        refundFilter.addOptionalTopics(slaTopic);

        return paymentConditions.paymentRefundEventFlowable(refundFilter);

    }
    */

    /**
     * Gets and Initializes all the conditions associated with a template
     * @param params params to fill the conditions
     * @return a List with all the conditions of the template
     * @throws InitializeConditionsException InitializeConditionsException
     */
    public List<Condition> initializeConditions(Map<String, Object> params) throws InitializeConditionsException {

        try {
            params.putAll(getFunctionsFingerprints());

            if (conditionsTemplate == null)
                conditionsTemplate = new String(Files.readAllBytes(Paths.get(ACCESS_CONDITIONS_FILE_TEMPLATE)));

            params.forEach((_name, _func) -> {
                if (_func instanceof byte[])
                    conditionsTemplate = conditionsTemplate.replaceAll("\\{" + _name + "\\}", CryptoHelper.getHex((byte[]) _func));
                else
                    conditionsTemplate = conditionsTemplate.replaceAll("\\{" + _name + "\\}", _func.toString());
            });

            return AbstractModel
                    .getMapperInstance()
                    .readValue(conditionsTemplate, new TypeReference<List<Condition>>() {
                    });
        }catch (Exception e) {
            String msg = "Error initializing conditions for template";
            log.error(msg);
            throw new InitializeConditionsException(msg, e);
        }
    }

    /**
     * Compose the different function fingerprint hashes
     * @return Map of (varible name, function fingerprint)
     * @throws UnsupportedEncodingException UnsupportedEncodingException
     */
    public static Map<String, Object> getFunctionsFingerprints() throws UnsupportedEncodingException {


        //String checksumLockConditionsAddress = Keys.toChecksumAddress(addresses.getLockRewardConditionAddress());
        //String checksumAccessSecretStoreConditionsAddress = Keys.toChecksumAddress(addresses.getAccessSecretStoreConditionAddress());

        Map<String, Object> fingerprints= new HashMap<>();

        fingerprints.put("function.lockReward.fingerprint", EthereumHelper.getFunctionSelector(FUNCTION_LOCKREWARD_DEF));
        log.debug("lockReward fingerprint: " + fingerprints.get("function.lockReward.fingerprint"));

        fingerprints.put("function.accessSecretStore.fingerprint", EthereumHelper.getFunctionSelector(FUNCTION_ACCESSSECRETSTORE_DEF));
        log.debug("accessSecretStore fingerprint: " + fingerprints.get("function.accessSecretStore.fingerprint"));

        fingerprints.put("function.escrowReward.fingerprint", EthereumHelper.getFunctionSelector(FUNCTION_ESCROWREWARD_DEF));
        log.debug("escrowReward fingerprint: " + fingerprints.get("function.escrowReward.fingerprint"));


        return fingerprints;
    }

    /**
     * Calculates the conditionKey
     * @param templateId the id of the template
     * @param address Checksum address
     * @param fingerprint the fingerprint of the condition
     * @return a String with the condition key

    public static String fetchConditionKey(String templateId, String address, String fingerprint)   {

        templateId = templateId.replaceAll("0x", "");
        address = address.replaceAll("0x", "");
        fingerprint = fingerprint.replaceAll("0x", "");

        String params= templateId
                + address
                + fingerprint;

        return Hash.sha3(params);
    }
     */

/*    public static List<BigInteger> getFullfillmentIndices(List<Condition> conditions)   {
        List<BigInteger> dependenciesBits= new ArrayList<>();
        BigInteger counter= BigInteger.ZERO;

        for (Condition condition: conditions)    {
            //if (condition.isTerminalCondition == 1)
            //    dependenciesBits.add(counter);
            counter= counter.add(BigInteger.ONE);
        }
        return dependenciesBits;
    }*/

/*

    public static List<BigInteger> getDependenciesBits()   {
        List<BigInteger> compressedDeps= new ArrayList<>();
        compressedDeps.add(BigInteger.valueOf(0));
        compressedDeps.add(BigInteger.valueOf(1));
        compressedDeps.add(BigInteger.valueOf(4));
        compressedDeps.add(BigInteger.valueOf(13));
        return compressedDeps;
    }

*/


}
