package com.oceanprotocol.squid.api.impl;

import com.oceanprotocol.squid.api.AgreementConditionsAPI;
import com.oceanprotocol.squid.exceptions.AccessSecretStoreConditionException;
import com.oceanprotocol.squid.exceptions.EscrowRewardException;
import com.oceanprotocol.squid.exceptions.LockRewardFulfillException;
import com.oceanprotocol.squid.exceptions.ServiceException;
import com.oceanprotocol.squid.manager.OceanManager;

import java.math.BigInteger;

public class AgreementConditionsImpl implements AgreementConditionsAPI{

    private OceanManager oceanManager;

    public AgreementConditionsImpl(OceanManager oceanManager){
        this.oceanManager = oceanManager;
    }


    @Override
    public Boolean lockReward(String agreementId, Integer amount) throws LockRewardFulfillException {

        return oceanManager.fulfillLockReward(agreementId, amount);

    }

    @Override
    public Boolean grantAccess(String agreementId, String assetId, String granteeAddress) throws AccessSecretStoreConditionException {

        return oceanManager.fulfillAccessSecretStoreCondition(agreementId, assetId, granteeAddress);

    }

    @Override
    public Boolean releaseReward(String agreementId, Integer amount) throws EscrowRewardException {

        return oceanManager.fulfillEscrowReward(agreementId, amount);
    }

    @Override
    public Boolean refundReward(String agreementId, Integer amount) throws EscrowRewardException {

        return  oceanManager.fulfillEscrowReward(agreementId, amount);
    }

}
