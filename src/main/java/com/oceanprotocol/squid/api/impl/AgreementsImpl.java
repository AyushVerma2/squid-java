package com.oceanprotocol.squid.api.impl;

import com.oceanprotocol.keeper.contracts.AgreementStoreManager;
import com.oceanprotocol.squid.api.AgreementsAPI;
import com.oceanprotocol.squid.manager.AgreementsManager;
import com.oceanprotocol.squid.models.Account;
import com.oceanprotocol.squid.models.DID;
import com.oceanprotocol.squid.models.service.AgreementStatus;
import org.web3j.tuples.generated.Tuple2;

public class AgreementsImpl implements AgreementsAPI {

    private AgreementsManager agreementsManager;


    /**
     * Constructor
     *
     * @param accountsManager the accountsManager
     */
    public AgreementsImpl(AgreementsManager accountsManager) {

        this.agreementsManager = accountsManager;
    }

    @Override
    public Tuple2<String, String> prepare(DID did, String serviceDefinitionId, String consumerAccount) {
        return null;
    }

    @Override
    public void send(DID did, String agreementId, String serviceDefinitionId, String signature, String consumerAccount) {

    }

    @Override
    public boolean create(DID did, String agreementId, String serviceDefinitionId, String signature, String consumerAccount, Account account) {
        return true;
    }

    @Override
    public AgreementStatus status(String agreementId) throws Exception {
        return agreementsManager.getStatus(agreementId);
    }
}
