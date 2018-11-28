package com.oceanprotocol.squid.core.sla;

import com.fasterxml.jackson.core.type.TypeReference;
import com.oceanprotocol.keeper.contracts.ServiceAgreement;
import com.oceanprotocol.squid.helpers.CryptoHelper;
import com.oceanprotocol.squid.helpers.EncodingHelper;
import com.oceanprotocol.squid.helpers.EthereumHelper;
import com.oceanprotocol.squid.models.AbstractModel;
import com.oceanprotocol.squid.models.service.Condition;
import io.reactivex.Flowable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AccessSLA implements SlaFunctions {

    private static final String ACCESS_CONDITIONS_FILE_TEMPLATE= "src/main/resources/sla/sla-access-conditions-template.json";
    private String conditionsTemplate= null;

    public static Flowable<ServiceAgreement.ExecuteAgreementEventResponse> listenExecuteAgreement(ServiceAgreement slaContract, String serviceAgreementId)   {
        EthFilter slaFilter = new EthFilter(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST,
                slaContract.getContractAddress()
        );

        final Event event= slaContract.EXECUTEAGREEMENT_EVENT;
        final String eventSignature= EventEncoder.encode(event);
        String slaTopic= "0x" + serviceAgreementId;
        slaFilter.addSingleTopic(eventSignature);
        slaFilter.addOptionalTopics(slaTopic);

        return slaContract.executeAgreementEventFlowable(slaFilter);
    }

    public List<Condition> initializeConditions(String templateId, String address, Map<String, Object> params) throws IOException {
        params.putAll(getFunctionsFingerprints(templateId, address));

        if (conditionsTemplate == null)
            conditionsTemplate = new String(Files.readAllBytes(Paths.get(ACCESS_CONDITIONS_FILE_TEMPLATE)));

        params.forEach( (_name, _func) -> {
            if (_func instanceof byte[])
                conditionsTemplate= conditionsTemplate.replaceAll("\\{" + _name + "\\}", CryptoHelper.getHex((byte[]) _func));
            else
                conditionsTemplate= conditionsTemplate.replaceAll("\\{" + _name + "\\}", _func.toString());
        });

        return AbstractModel
                .getMapperInstance()
                .readValue(conditionsTemplate, new TypeReference<List<Condition>>() {});
    }

    private static final String FUNCTION_LOCKPAYMENT_DEF= "lockPayment(bytes32,uint)";
    private static final String FUNCTION_GRANTACCESS_DEF= "grantAccess(bytes32,bytes32)";
    private static final String FUNCTION_RELEASEPAYMENT_DEF= "releasePayment(bytes32,uint)";
    private static final String FUNCTION_REFUNDPAYMENT_DEF= "refundPayment(bytes32,uint)";

    /**
     * Compose the different conditionKey hashes using:
     * (serviceAgreementTemplateId, address, signature)
     * @return Map of (varible name => conditionKeys)
     */
    public Map<String, Object> getFunctionsFingerprints(String templateId, String address) throws UnsupportedEncodingException {
        byte[] bytesTemplateId= EncodingHelper.hexStringToBytes(templateId);
        Address _address= new Address(address);

                Map<String, Object> fingerprints= new HashMap<>();
        fingerprints.put("function.lockPayment.fingerprint", EthereumHelper.getFunctionSelector(
                FUNCTION_LOCKPAYMENT_DEF));

        fingerprints.put("function.grantAccess.fingerprint", EthereumHelper.getFunctionSelector(
                FUNCTION_GRANTACCESS_DEF));

        fingerprints.put("function.releasePayment.fingerprint", EthereumHelper.getFunctionSelector(
                FUNCTION_RELEASEPAYMENT_DEF));

        fingerprints.put("function.refundPayment.fingerprint", EthereumHelper.getFunctionSelector(
                FUNCTION_REFUNDPAYMENT_DEF));

        fingerprints.put("function.lockPayment.conditionKey", CryptoHelper.soliditySha3(
                bytesTemplateId, _address, EthereumHelper.getFunctionSelectorBytes(FUNCTION_LOCKPAYMENT_DEF)
        ));

        fingerprints.put("function.grantAccess.conditionKey", CryptoHelper.soliditySha3(
                bytesTemplateId, _address, EthereumHelper.getFunctionSelectorBytes(FUNCTION_GRANTACCESS_DEF)
        ));

        fingerprints.put("function.releasePayment.conditionKey", CryptoHelper.soliditySha3(
                bytesTemplateId, _address, EthereumHelper.getFunctionSelectorBytes(FUNCTION_RELEASEPAYMENT_DEF)
        ));

        fingerprints.put("function.refundPayment.conditionKey", CryptoHelper.soliditySha3(
                bytesTemplateId, _address, EthereumHelper.getFunctionSelectorBytes(FUNCTION_REFUNDPAYMENT_DEF)
        ));

        return fingerprints;
    }


}