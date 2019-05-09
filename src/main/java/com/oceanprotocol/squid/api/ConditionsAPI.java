package com.oceanprotocol.squid.api;

import com.oceanprotocol.squid.models.DID;

import java.math.BigInteger;

/**
 * Exposes the Public API related with the management of Conditions
 */
public interface ConditionsAPI {

    /**
     * Lock the amount of token that are going to be paid for the asset.
     *
     * @param agreementId the agreement id
     * @param amount      the amount to be locked
     * @return a flag if the execution was good
     * @throws Exception
     */
    public boolean lockReward(String agreementId, BigInteger amount) throws Exception;

    /**
     * Grant access to an address using the parity secret store.
     *
     * @param agreementId the agreement id
     * @param did         the did
     * @param grantee     the address that is going to be granted to access the data.
     * @return a flag if the execution was good
     * @throws Exception
     */
    public boolean grantAccess(String agreementId, DID did, String grantee) throws Exception;

    /**
     * Release the payment to the provider address.
     *
     * @param agreementId the agreement id.
     * @param amount      the amount to be released.
     * @return a flag if the execution was good
     * @throws Exception
     */
    public boolean releaseReward(String agreementId, BigInteger amount) throws Exception;

    /**
     * Refund the payment to the consumer.
     *
     * @param agreementId the agreement id.
     * @param amount      the amount to be refund.
     * @return a flag if the execution was good
     * @throws Exception
     */
    public boolean refundReward(String agreementId, BigInteger amount) throws Exception;

}
