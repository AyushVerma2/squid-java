package com.oceanprotocol.squid.manager;

import com.oceanprotocol.squid.external.AquariusService;
import com.oceanprotocol.squid.external.KeeperService;
import com.oceanprotocol.squid.helpers.EncodingHelper;
import com.oceanprotocol.squid.models.DID;
import com.oceanprotocol.squid.models.service.Agreement;
import com.oceanprotocol.squid.models.service.AgreementStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.List;

public class AgreementsManager extends BaseManager {

    private static final Logger log = LogManager.getLogger(AgreementsManager.class);

    public AgreementsManager(KeeperService keeperService, AquariusService aquariusService) {
        super(keeperService, aquariusService);
    }

    /**
     * Given the KeeperService and AquariusService, returns a new instance of AgreementsManager
     * using them as attributes
     *
     * @param keeperService   Keeper Dto
     * @param aquariusService Provider Dto
     * @return AgreementsManager
     */
    public static AgreementsManager getInstance(KeeperService keeperService, AquariusService aquariusService) {
        return new AgreementsManager(keeperService, aquariusService);
    }

    public Boolean createAgreement(String agreementId, DID did, List<byte[]> conditionIds, List<BigInteger> timeLocks,
                                   List<BigInteger> timeOuts) throws Exception {
        TransactionReceipt txReceipt = this.escrowAccessSecretStoreTemplate.createAgreement(EncodingHelper.hexStringToBytes(agreementId),
                EncodingHelper.hexStringToBytes(did.getHash()),
                conditionIds,
                timeLocks,
                timeOuts).send();
        return txReceipt.isStatusOK();
    }

    /**
     * Retrieve the agreement for a agreement_id.
     * @param agreementId id of the agreement
     * @return Agreement
     * @throws Exception Exception
     */
    public Agreement getAgreement(String agreementId) throws Exception {
        return new Agreement(agreementStoreManager.getAgreement(EncodingHelper.hexStringToBytes(agreementId)).send());
    }

    /**
     * Get the status of a service agreement.
     *
     * @param agreementId id of the agreement
     * @return AgreementStatus with condition status of each of the agreement's conditions.
     */
    public AgreementStatus getStatus(String agreementId) throws Exception {
        List<byte[]> condition_ids = agreementStoreManager.getAgreement(EncodingHelper.hexStringToBytes(agreementId)).send().getValue4();
        AgreementStatus agreementStatus = new AgreementStatus();
        agreementStatus.agreementId = agreementId;
        AgreementStatus.ConditionStatusMap condition = new AgreementStatus.ConditionStatusMap();
        for (int i = 0; i <= condition_ids.size() - 1; i++) {
            String address = conditionStoreManager.getCondition(condition_ids.get(i)).send().getValue1();
            String conditionName = getConditionNameByAddress(Keys.toChecksumAddress(address));
            BigInteger state = conditionStoreManager.getCondition(condition_ids.get(i)).send().getValue2();
            condition.conditions.put(conditionName, state);

        }
        agreementStatus.conditions.add(condition);
        return agreementStatus;
    }

    private String getConditionNameByAddress(String address) throws Exception {
        if (this.lockRewardCondition.getContractAddress().equals(address)) return "lockReward";
        else if (this.accessSecretStoreCondition.getContractAddress().equals(address)) return "accessSecretStore";
        else if (this.escrowReward.getContractAddress().equals(address)) return "escrowReward";
        else log.error("The current address" + address + "is not a condition address.");
        throw new Exception("The current address" + address + "is not a condition address.");
    }

}
