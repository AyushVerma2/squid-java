package com.oceanprotocol.squid.manager;

import com.oceanprotocol.squid.external.AquariusService;
import com.oceanprotocol.squid.external.KeeperService;
import com.oceanprotocol.squid.helpers.EncodingHelper;
import com.oceanprotocol.squid.models.Account;
import com.oceanprotocol.squid.models.DID;
import com.oceanprotocol.squid.models.service.Agreement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;

public class ConditionsManager extends BaseManager {

    private static final Logger log = LogManager.getLogger(ConditionsManager.class);

    public ConditionsManager(KeeperService keeperService, AquariusService aquariusService) {
        super(keeperService, aquariusService);
    }

    /**
     * Given the KeeperService and AquariusService, returns a new instance of ConditionsManager
     * using them as attributes
     *
     * @param keeperService   Keeper Dto
     * @param aquariusService Provider Dto
     * @return ConditionsManager
     */
    public static ConditionsManager getInstance(KeeperService keeperService, AquariusService aquariusService) {
        return new ConditionsManager(keeperService, aquariusService);
    }

    public Boolean lockReward(String agreementId, BigInteger amount) throws Exception {
        TransactionReceipt txReceipt = lockRewardCondition.fulfill(EncodingHelper.hexStringToBytes(agreementId),
                escrowAccessSecretStoreTemplate.getAgreementData(EncodingHelper.hexStringToBytes(agreementId)).send().getValue2(),
                amount).send();
        return txReceipt.isStatusOK();
    }

    public Boolean grantAccess(String agreementId, DID did, String granteeAddress) throws Exception {
        TransactionReceipt txReceipt = accessSecretStoreCondition.fulfill(EncodingHelper.hexStringToBytes(agreementId),
                EncodingHelper.hexStringToBytes("0x" + did.getHash()),
                granteeAddress).send();
        return txReceipt.isStatusOK();
    }

    public Boolean releaseReward(String agreementId, BigInteger amount) throws Exception {
        Agreement agreement = new Agreement(agreementStoreManager.getAgreement(EncodingHelper.hexStringToBytes(agreementId)).send());
        TransactionReceipt txReceipt = escrowReward.fulfill(EncodingHelper.hexStringToBytes(agreementId),
                amount,
                escrowAccessSecretStoreTemplate.getAgreementData(EncodingHelper.hexStringToBytes(agreementId)).send().getValue2(),
                escrowAccessSecretStoreTemplate.getAgreementData(EncodingHelper.hexStringToBytes(agreementId)).send().getValue1(),
                agreement.conditions.get(0),
                agreement.conditions.get(1)).send();
        return txReceipt.isStatusOK();
    }

    public Boolean refundReward(String agreementId, BigInteger amount) throws Exception {
        return releaseReward(agreementId, amount);
    }

}
