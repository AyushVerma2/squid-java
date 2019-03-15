package com.oceanprotocol.squid.api;

import com.oceanprotocol.keeper.contracts.AccessSecretStoreCondition;
import com.oceanprotocol.squid.exceptions.AccessSecretStoreConditionException;
import com.oceanprotocol.squid.exceptions.LockRewardFulfillException;

import java.math.BigInteger;

/**
 * Exposes the Public API related with the conditions of Agreements
 */
public interface AgreementConditionsAPI {

    /**
     * Transfers tokens to the EscrowRewardCondition contract as an escrow payment. This is required before access can be given to the asset data.
     * @param agreementId The id of the agreement
     * @param amount the amount of tokens to transfer
     * @return a flag that indicates if the condition was executed correctly
     * @throws LockRewardFulfillException when there is a problem with the transaction
     */
    public Boolean lockReward(String agreementId, BigInteger amount) throws LockRewardFulfillException;


    /**
     * Authorize the consumer defined in the agreement to access (consume) this asset
     * @param agreementId The id of the agreement
     * @param assetId The id of the asset
     * @param granteeAddress the address of the grantee
     * @return a flag that indicates if the condition was executed correctly
     * @throws AccessSecretStoreConditionException  when there is a problem with the transaction
     */
    public Boolean grantAccess(String agreementId, String assetId, String granteeAddress) throws AccessSecretStoreConditionException;



}
