/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.external;

import com.fasterxml.jackson.core.type.TypeReference;
import com.oceanprotocol.squid.exceptions.DDOException;
import com.oceanprotocol.squid.helpers.HttpHelper;
import com.oceanprotocol.squid.models.AbstractModel;
import com.oceanprotocol.squid.models.DDO;
import com.oceanprotocol.squid.models.HttpResponse;
import com.oceanprotocol.squid.models.aquarius.SearchQuery;
import org.apache.commons.httpclient.HttpException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Service for Aquarius's Integration
 */
public class AquariusService {

    private static final Logger log = LogManager.getLogger(AquariusService.class);

    private static final String DDO_URI = "/api/v1/aquarius/assets/ddo";
    private String ddoEndpoint;

    private String url;

    /**
     * Builds an instance of AquariusService
     * @param url url of aquarius
     * @return AquariusService instance
     */
    public static AquariusService getInstance(String url)    {
        log.debug("Getting Aquarius instance: " + url);
        return new AquariusService(url);
    }

    /**
     * Constructor
     * @param url the url of aquarius
     */
    private AquariusService(String url) {
        this.url= url.replaceAll("/$", "");
        this.ddoEndpoint = this.url + DDO_URI;
    }

    public String getDdoEndpoint() {
        return ddoEndpoint;
    }

    /**
     * Registers a new DDO in Aquarius
     * @param ddo the ddo
     * @return the created DDO
     * @throws DDOException DDOException
     */
    public DDO createDDO(DDO ddo) throws DDOException {

        log.debug("Creating DDO: " + ddo.id);

        try {

            HttpResponse response= HttpHelper.httpClientPost(
                    this.ddoEndpoint, new ArrayList<>(), ddo.toJson());

            if (response.getStatusCode() != 201)    {
                throw new DDOException("Unable to create DDO: " + response.toString());
            }

            return DDO.fromJSON(new TypeReference<DDO>() {}, response.getBody());

        } catch (Exception e){
            throw new DDOException("Error building DDO from JSON", e);
        }

    }

    /**
     * Gets a DDO from an URL
     * @param url the url
     * @return the DDO
     * @throws DDOException DDOException
     */
    public DDO getDDO(String url) throws DDOException {

        log.debug("Getting DDO: " + url);
        HttpResponse response;

        try {
             response = HttpHelper.httpClientGet(url);
        } catch (HttpException e) {
            throw new DDOException("Unable to get DDO", e);
        }

        if (response.getStatusCode() != 200)    {
            throw new DDOException("Unable to get DDO: " + response.toString());
        }
        try {
            return DDO.fromJSON(new TypeReference<DDO>() {}, response.getBody());
        } catch (Exception e){
            throw new DDOException("Error building DDO from JSON", e);
        }
    }

    /**
     * Gets a DDO from the DID
     * @param id the DID
     * @return the DDO
     * @throws Exception Exception
     */
    public DDO getDDOUsingId(String id) throws Exception {
        return getDDO(this.ddoEndpoint + "/" +  id);

    }


    /**
     * Updates the metadata of a DDO
     * @param id the did
     * @param ddo the DDO
     * @return a flag that indicates if the update operation was executed correctly
     * @throws Exception Exception
     */
    public boolean updateDDO(String id, DDO ddo) throws Exception {
        HttpResponse response= HttpHelper.httpClientPut(
                this.ddoEndpoint + "/" + id, new ArrayList<>(), ddo.toJson());

        if (response.getStatusCode() == 200 || response.getStatusCode() == 201)    {
            return true;
        }
        throw new Exception("Unable to update DDO: " + response.toString());
    }

    /**
     * Search all the DDOs that match the text passed as a parameter
     * @param param the criteria
     * @param offset parameter to paginate
     * @param page parameter to paginate
     * @return a List of all the DDOs found
     * @throws DDOException DDOException
     */
    public ArrayList<DDO> searchDDO(String param, int offset, int page) throws DDOException  {

        String url= this.ddoEndpoint + "/query?text=" + param + "&page=" + page + "&offset=" + offset;
        HttpResponse response;

        try {
            response= HttpHelper.httpClientGet(url);
        } catch (HttpException e) {
            throw new DDOException("Unable to get DDO: ", e);
        }

        if (response.getStatusCode() != 200)    {
            throw new DDOException("Unable to search for DDO's: " + response.toString());
        }

        try {
            return AbstractModel
                    .getMapperInstance()
                    .readValue(response.getBody(), new TypeReference<ArrayList<DDO>>() {});
        } catch (IOException e) {
            throw new DDOException("Unable to search for DDO's: ", e);
        }

    }

    /**
     * Search all the DDOs that match the query passed as a parameter
     * @param searchQuery the query
     * @return a List of all the DDOs found
     * @throws DDOException DDOException
     */
    public ArrayList<DDO> searchDDO(SearchQuery searchQuery) throws DDOException {

        HttpResponse response;

        try {
            response = HttpHelper.httpClientPost(
                     this.ddoEndpoint + "/query", new ArrayList<>(), searchQuery.toJson());
        } catch (Exception e) {
            throw new DDOException("Unable to get DDO", e);
        }

        if (response.getStatusCode() != 200)    {
            throw new DDOException("Unable to search for DDO's: " + response.toString());
        }

        try {
            return AbstractModel
                    .getMapperInstance()
                    .readValue(response.getBody(), new TypeReference<ArrayList<DDO>>() {});
        } catch (IOException e) {
            throw new DDOException("Unable to search for DDO's", e);
        }

    }

}
