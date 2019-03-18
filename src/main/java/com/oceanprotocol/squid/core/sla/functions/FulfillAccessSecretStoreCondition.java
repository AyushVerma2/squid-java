package com.oceanprotocol.squid.core.sla.functions;

import com.oceanprotocol.keeper.contracts.AccessSecretStoreCondition;
import com.oceanprotocol.squid.exceptions.AccessSecretStoreConditionException;
import com.oceanprotocol.squid.helpers.EncodingHelper;
import com.oceanprotocol.squid.helpers.EthereumHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

/**
 * Executes a fulfill function of a AccessSecretStore Condition
 */
public class FulfillAccessSecretStoreCondition {

    static final Logger log= LogManager.getLogger(FulfillAccessSecretStoreCondition.class);

    /**
     *Executes a fulfill function of a AccessSecretStore Condition
     * @param accessSecretStoreCondition the AccessSecretStoreCondition Contract
     * @param agreementId the id of the agreement
     * @param assetId the id of the asset
     * @param granteeAddress the address of the grantee
     * @return a flag that indicates if the function was executed correctly
     * @throws AccessSecretStoreConditionException AccessSecretStoreConditionException
     */
    public static Boolean executeFulfill(AccessSecretStoreCondition accessSecretStoreCondition,
                                         String agreementId,
                                         String assetId,
                                         String granteeAddress) throws AccessSecretStoreConditionException
    {

        byte[] serviceIdBytes;
        byte[] assetIdBytes;

        try {

            granteeAddress = Keys.toChecksumAddress(granteeAddress);
            serviceIdBytes = EncodingHelper.hexStringToBytes(EthereumHelper.add0x(agreementId));
            assetIdBytes = EncodingHelper.hexStringToBytes(EthereumHelper.add0x(assetId));

            TransactionReceipt receipt= accessSecretStoreCondition.fulfill(
                    serviceIdBytes,
                    assetIdBytes,
                    granteeAddress
            ).send();

            if (!receipt.getStatus().equals("0x1")) {
                String msg = "The Status received is not valid executing AccessSecretStoreCondition.Fulfill: " + receipt.getStatus() + " for serviceAgreement " + agreementId;
                log.error(msg);
                throw new AccessSecretStoreConditionException(msg);
            }

            log.debug("AccessSecretStoreCondition.Fulfill transactionReceipt OK for serviceAgreement " + agreementId);
            return true;

        } catch (Exception e) {

            String msg = "Error executing AccessSecretStoreCondition.Fulfill for serviceAgreement " + agreementId;
            log.error(msg+ ": " + e.getMessage());
            throw new AccessSecretStoreConditionException(msg, e);
        }


    }
}
