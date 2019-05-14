package com.oceanprotocol.squid.api.impl;

import com.oceanprotocol.squid.api.AccountsAPI;
import com.oceanprotocol.squid.api.AgreementsAPI;
import com.oceanprotocol.squid.external.KeeperService;
import com.oceanprotocol.squid.manager.AgreementsManager;
import com.oceanprotocol.squid.manager.OceanManager;
import com.oceanprotocol.squid.models.DDO;
import com.oceanprotocol.squid.models.DID;
import com.oceanprotocol.squid.models.service.AccessService;
import com.oceanprotocol.squid.models.service.AgreementStatus;
import org.web3j.crypto.Keys;
import org.web3j.tuples.generated.Tuple2;

public class AgreementsImpl implements AgreementsAPI {

    private AgreementsManager agreementsManager;
    private OceanManager oceanManager;


    /**
     * Constructor
     *
     * @param agreementsManager the accountsManager
     */
    public AgreementsImpl(AgreementsManager agreementsManager, OceanManager oceanManager) {
        this.oceanManager = oceanManager;
        this.agreementsManager = agreementsManager;
    }

    @Override
    public Tuple2<String, String> prepare(DID did, String serviceDefinitionId, String consumerAddress) {
        return null;
    }

    @Override
    public void send(DID did, String agreementId, String serviceDefinitionId, String signature, String consumerAddress) {

    }

    @Override
    public boolean create(DID did, String agreementId, String serviceDefinitionId, String signature, String consumerAddress) throws Exception {
        DDO ddo = oceanManager.resolveDID(did);
        AccessService accessService = ddo.getAccessService(serviceDefinitionId);
        return agreementsManager.createAgreement(agreementId,
                ddo,
                accessService.generateConditionIds(agreementId, oceanManager, ddo, Keys.toChecksumAddress(consumerAddress)),
                Keys.toChecksumAddress(consumerAddress),
                signature,
                accessService
        );
    }

    @Override
    public AgreementStatus status(String agreementId) throws Exception {
        return agreementsManager.getStatus(agreementId);
    }
}
