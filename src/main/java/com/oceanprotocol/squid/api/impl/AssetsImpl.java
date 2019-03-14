/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.api.impl;

import com.oceanprotocol.squid.api.AssetsAPI;
import com.oceanprotocol.squid.exceptions.ConsumeServiceException;
import com.oceanprotocol.squid.exceptions.DDOException;
import com.oceanprotocol.squid.exceptions.EthereumException;
import com.oceanprotocol.squid.exceptions.OrderException;
import com.oceanprotocol.squid.manager.AssetsManager;
import com.oceanprotocol.squid.manager.OceanManager;
import com.oceanprotocol.squid.models.Account;
import com.oceanprotocol.squid.models.DDO;
import com.oceanprotocol.squid.models.DID;
import com.oceanprotocol.squid.models.asset.AssetMetadata;
import com.oceanprotocol.squid.models.asset.OrderResult;
import com.oceanprotocol.squid.models.service.ServiceEndpoints;
import io.reactivex.Flowable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Implementation of AssetsAPI
 */
public class AssetsImpl implements AssetsAPI {

    private OceanManager oceanManager;
    private AssetsManager assetsManager;

    /**
     * Constructor
     * @param oceanManager the oceanManager
     * @param assetsManager the assetsManager
     */
    public AssetsImpl(OceanManager oceanManager, AssetsManager assetsManager) {

        this.oceanManager = oceanManager;
        this.assetsManager = assetsManager;
    }


    @Override
    public DDO create(AssetMetadata metadata, ServiceEndpoints serviceEndpoints, int threshold) throws DDOException{
        return oceanManager.registerAsset(metadata, serviceEndpoints, threshold);
    }

    @Override
    public DDO create(AssetMetadata metadata, ServiceEndpoints serviceEndpoints) throws DDOException{
        return this.create(metadata, serviceEndpoints, 0);
    }

    @Override
    public DDO resolve(DID did) throws EthereumException, DDOException {
        return oceanManager.resolveDID(did);
    }

    @Override
    public List<DDO> search(String text) throws DDOException{
        return this.search(text, 0, 0);
    }

    @Override
    public List<DDO> search(String text, int offset, int page) throws DDOException {
        return assetsManager.searchAssets(text, offset, page);
    }

    @Override
    public List<DDO> query(Map<String, Object> params, int offset, int page, int sort)  throws DDOException {
        return assetsManager.searchAssets(params, offset, page, sort);
    }

    @Override
    public List<DDO> query(Map<String, Object> params)  throws DDOException {
        return this.query(params, 0, 0, 1);
    }

    @Override
    public Boolean consume(String serviceAgreementId, DID did, String serviceDefinitionId, String basePath, int threshold) throws ConsumeServiceException {
        return oceanManager.consume(serviceAgreementId, did, serviceDefinitionId,  basePath, threshold);
    }

    @Override
    public Boolean consume(String serviceAgreementId, DID did, String serviceDefinitionId,  String basePath) throws ConsumeServiceException {
        return this.consume(serviceAgreementId, did, serviceDefinitionId,  basePath, 0);
    }

    @Override
    public Flowable<OrderResult> order(DID did, String serviceDefinitionId) throws OrderException{
        return oceanManager.purchaseAsset(did, serviceDefinitionId);
    }
}
