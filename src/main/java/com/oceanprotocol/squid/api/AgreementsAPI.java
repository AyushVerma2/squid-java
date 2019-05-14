package com.oceanprotocol.squid.api;

import com.oceanprotocol.squid.models.Account;
import com.oceanprotocol.squid.models.DID;
import com.oceanprotocol.squid.models.service.AgreementStatus;
import org.web3j.tuples.generated.Tuple2;

/**
 * Exposes the Public API related with the management of Agreements
 */
public interface AgreementsAPI {

    /**
     * Prepare the service agreement.
     *
     * @param did                 the did
     * @param serviceDefinitionId the service definition id of the agreement
     * @param consumerAccount     the address of the consumer
     * @return Tuple with agreement id and signature.
     * @throws Exception Exception
     */
    public Tuple2<String, String> prepare(DID did, String serviceDefinitionId, Account consumerAccount) throws Exception;

    /**
     * Send a signed service agreement to the publisher Brizo instance to consume/access the service.
     *
     * @param did                 the did
     * @param agreementId         the agreement id
     * @param serviceDefinitionId the service definition id of the agreement
     * @param signature           the signature
     * @param consumerAccount     the account of the consumer
     * @throws Exception Exception
     */
    public void send(DID did, String agreementId, String serviceDefinitionId, String signature, Account consumerAccount) throws Exception;

    /**
     * Create a service agreement.
     *
     * @param did                 the did
     * @param agreementId         the agreement id
     * @param serviceDefinitionId the service definition id of the agreement
     * @param signature           the signature
     * @param consumerAddress     the address of the consumer
     * @return a flag a true if the creation of the agreement was successful.
     * @throws Exception Exception
     */
    public boolean create(DID did, String agreementId, String serviceDefinitionId, String signature, String consumerAddress) throws Exception;

    /**
     * Get the status of a service agreement.
     *
     * @param agreementId id of the agreement
     * @return AgreementStatus with condition status of each of the agreement's conditions.
     */
    public AgreementStatus status(String agreementId) throws Exception;
}
