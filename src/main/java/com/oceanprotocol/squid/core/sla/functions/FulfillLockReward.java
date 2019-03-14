/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.core.sla.functions;

import com.oceanprotocol.keeper.contracts.LockRewardCondition;
import com.oceanprotocol.squid.exceptions.LockRewardFulfillException;
import com.oceanprotocol.squid.helpers.EncodingHelper;
import com.oceanprotocol.squid.models.asset.BasicAssetInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;

public class FulfillLockReward {

    static final Logger log= LogManager.getLogger(FulfillLockReward.class);

    /**
     * Executes a fulfill function of a LockReward Condition
     * @param lockRewardCondition  LockRewardCondition contract
     * @param serviceAgreementId the service agreement id
     * @param escrowRewardAddress the address of the EscrowReward Contract
     * @param assetInfo basic info of the asset
     * @return a flag that indicates if the function was executed correctly
     * @throws LockRewardFulfillException LockRewardFulfillException
     */
    public static Boolean executeFulfill(LockRewardCondition lockRewardCondition,
                                         String serviceAgreementId,
                                         String escrowRewardAddress,
                                         BasicAssetInfo assetInfo) throws LockRewardFulfillException {

        byte[] serviceId;
        Integer price = -1;

        try {

            escrowRewardAddress = Keys.toChecksumAddress(escrowRewardAddress);
            serviceId = EncodingHelper.hexStringToBytes(serviceAgreementId);

            TransactionReceipt receipt= lockRewardCondition.fulfill(
                    serviceId,
                    escrowRewardAddress,
                    BigInteger.valueOf(assetInfo.getPrice())
            ).send();

            if (!receipt.getStatus().equals("0x1")) {
                String msg = "The Status received is not valid executing LockRewardCondition.Fulfill: " + receipt.getStatus() + " for serviceAgreement " + serviceAgreementId;
                log.error(msg);
                throw new LockRewardFulfillException(msg);
            }

            log.debug("LockRewardCondition.Fulfill transactionReceipt OK for serviceAgreement " + serviceAgreementId);
            return true;

        } catch (Exception e) {

            String msg = "Error executing LockRewardCondition.Fulfill for serviceAgreement " + serviceAgreementId;
            log.error(msg+ ": " + e.getMessage());
            throw new LockRewardFulfillException(msg, e);
        }

    }
}
