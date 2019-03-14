/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.api;

import com.oceanprotocol.squid.exceptions.ConsumeServiceException;
import com.oceanprotocol.squid.exceptions.DDOException;
import com.oceanprotocol.squid.exceptions.EthereumException;
import com.oceanprotocol.squid.exceptions.OrderException;
import com.oceanprotocol.squid.models.Account;
import com.oceanprotocol.squid.models.DDO;
import com.oceanprotocol.squid.models.DID;
import com.oceanprotocol.squid.models.asset.AssetMetadata;
import com.oceanprotocol.squid.models.asset.OrderResult;
import com.oceanprotocol.squid.models.service.ServiceEndpoints;
import io.reactivex.Flowable;

import java.util.List;
import java.util.Map;

/**
 * Exposes the Public API related with Assets
 */
public interface AssetsAPI {

    /**
     * Creates a new DDO, registering it on-chain through DidRegistry contract and off-chain in Aquarius
     * @param metadata the metadata of the DDO
     * @param serviceEndpoints the endpoints of the DDO's services
     * @param threshold the secret store threshold
     * @return an instance of the DDO created
     * @throws DDOException DDOException
     */
    public DDO create(AssetMetadata metadata,  ServiceEndpoints serviceEndpoints, int threshold) throws DDOException;

    /**
     *  Creates a new DDO, registering it on-chain through DidRegistry contract and off-chain in Aquarius
     * @param metadata the metadata of the DDO
     * @param serviceEndpoints the endpoints of the DDO's services
     * @return an instance of the DDO created
     * @throws DDOException DDOException
     */
    public DDO create(AssetMetadata metadata, ServiceEndpoints serviceEndpoints) throws DDOException;

    /**
     * Gets a DDO from a DID
     * @param did the DID to resolve
     * @return an instance of the DDO represented by the DID
     * @throws EthereumException EthereumException
     * @throws DDOException DDOException
     */
    public DDO resolve(DID did) throws EthereumException, DDOException;

    /**
     * Gets all the DDO that match the search criteria
     * @param text the criteria
     * @return a List with all the DDOs found
     * @throws DDOException DDOException
     */
    public List<DDO> search(String text) throws DDOException;

    /**
     * Gets all the DDOs that match the search criteria
     * @param text the criteria
     * @param offset parameter to paginate
     * @param page parameter to paginate
     * @return a List with all the DDOs found
     * @throws DDOException DDOException
     */
    public List<DDO> search(String text,  int offset, int page) throws DDOException;

    /**
     * Gets all the DDOs that match the parameters of the query
     * @param params the criteria
     * @param offset parameter to paginate
     * @param page parameter to paginate
     * @param sort parameter to sort
     * @return a List with all the DDOs found
     * @throws DDOException DDOException
     */
    public List<DDO> query(Map<String, Object> params, int offset, int page, int sort) throws DDOException;

    /**
     * Gets all the DDOs that match the parameters of the query
     * @param params the criteria
     * @return a List with all the DDOs found
     * @throws DDOException DDOException
     */
    public List<DDO> query(Map<String, Object> params) throws DDOException;

    /**
     * Downloads an Asset previously ordered through a Service Agreement
     * @param serviceAgreementId the service agreement id of the asset
     * @param did the did
     * @param serviceDefinitionId the service definition id
     * @param basePath  the path where the asset will be downloaded
     * @param threshold secret store threshold to decrypt the urls of the asset
     * @return a flag that indicates if the consume flow was executed correctly
     * @throws ConsumeServiceException ConsumeServiceException
     */
    public Boolean consume(String serviceAgreementId, DID did, String serviceDefinitionId, String basePath, int threshold) throws ConsumeServiceException;

    /**
     *  Downloads an Asset previously ordered through a Service Agreement
     * @param serviceAgreementId the service agreement id of the asset
     * @param did the did
     * @param serviceDefinitionId the service definition id
     * @param basePath the path where the asset will be downloaded
     * @return a flag that indicates if the consume flow was executed correctly
     * @throws ConsumeServiceException ConsumeServiceException
     */
    public Boolean consume(String serviceAgreementId, DID did, String serviceDefinitionId, String basePath) throws ConsumeServiceException;

    /**
     * Purchases an Asset represented by a DID. It implies to initialize a Service Agreement between publisher and consumer
     * @param did the did of the DDO
     * @param serviceDefinitionId the service definition id
     * @return a Flowable instance over an OrderResult to get the result of the flow in an asynchronous fashion
     * @throws OrderException OrderException
     */
    Flowable<OrderResult> order(DID did, String serviceDefinitionId) throws OrderException;



}
