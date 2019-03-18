package com.oceanprotocol.squid.api;

import com.oceanprotocol.keeper.contracts.AccessSecretStoreCondition;
import com.oceanprotocol.squid.exceptions.AccessSecretStoreConditionException;
import com.oceanprotocol.squid.exceptions.EscrowRewardException;
import com.oceanprotocol.squid.exceptions.LockRewardFulfillException;
import com.oceanprotocol.squid.exceptions.ServiceException;

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
    public Boolean lockReward(String agreementId, Integer amount) throws LockRewardFulfillException;


    /**
     * Authorize the consumer defined in the agreement to access (consume) this asset
     * @param agreementId The id of the agreement
     * @param assetId The id of the asset
     * @param granteeAddress the address of the grantee
     * @return a flag that indicates if the condition was executed correctly
     * @throws AccessSecretStoreConditionException  when there is a problem with the transaction
     */
    public Boolean grantAccess(String agreementId, String assetId, String granteeAddress) throws AccessSecretStoreConditionException;


    /**
     * Transfer the escrow or locked tokens from the LockRewardCondition contract to the publisher's account.
     * This should be allowed after access has been given to the consumer and the asset data is downloaded.
     * @param agreementId the agreement id
     * @param amount the amount of tokens
     * @return  a flag that indicates if the condition was executed correctly
     * @throws EscrowRewardException when there is a problem with the transaction
     */
    public Boolean releaseReward(String agreementId, Integer amount) throws EscrowRewardException;


    /**
     * Refund the escrow or locked tokens back to the consumer account.
     * This will only work in the case where access was not granted within the specified timeout in the service agreement.
     * @param agreementId the agreement id
     * @param amount the amount of tokens
     * @return  a flag that indicates if the condition was executed correctly
     * @throws EscrowRewardException when there is a problem with the transaction
     */
    public Boolean refundReward(String agreementId, Integer amount) throws EscrowRewardException;





}
