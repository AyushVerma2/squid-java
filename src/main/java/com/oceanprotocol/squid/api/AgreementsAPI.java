package com.oceanprotocol.squid.api;

import com.oceanprotocol.squid.models.Account;
import com.oceanprotocol.squid.models.DID;
import com.oceanprotocol.squid.models.service.AgreementStatus;
import org.web3j.tuples.generated.Tuple2;

/**
 * Exposes the Public API related with the management of Agreements
 */
public interface AgreementsAPI {

    public Tuple2<String, String> prepare(DID did, String serviceDefinitionId, String consumerAccount);

    public void send(DID did, String agreementId, String serviceDefinitionId, String signature, String consumerAccount);

    public boolean create(DID did, String agreementId, String serviceDefinitionId, String signature, String consumerAccount, Account account);

    /**
     * Get the status of a service agreement.
     *
     * @param agreementId id of the agreement
     * @return AgreementStatus with condition status of each of the agreement's conditions.
     */
    public AgreementStatus status(String agreementId) throws Exception;
}
