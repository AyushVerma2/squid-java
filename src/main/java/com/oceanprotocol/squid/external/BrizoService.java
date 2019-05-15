/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.external;

import com.oceanprotocol.squid.helpers.HttpHelper;
import com.oceanprotocol.squid.helpers.HttpHelper.DownloadResult;
import com.oceanprotocol.squid.helpers.StringsHelper;
import com.oceanprotocol.squid.models.HttpResponse;
import com.oceanprotocol.squid.models.brizo.InitializeAccessSLA;
import com.oceanprotocol.squid.models.service.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for Brizo's Integration
 */
public class BrizoService {

    private static final Logger log = LogManager.getLogger(BrizoService.class);

    public static class ServiceAgreementResult {

        private Boolean ok;
        private Integer code;

        public Boolean getOk() {
            return ok;
        }

        public void setOk(Boolean ok) {
            this.ok = ok;
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }
    }


    /**
     * Calls a Brizo's endpoint to request the initialization of a new Service Agreement
     *
     * @param url     the url
     * @param payload the payload
     * @return an object that indicates if Brizo initialized the Service Agreement correctly
     */
    public static ServiceAgreementResult initializeAccessServiceAgreement(String url, InitializeAccessSLA payload) {

        log.debug("Initializing SLA[" + payload.serviceAgreementId + "]: " + url);

        ServiceAgreementResult result = new ServiceAgreementResult();


        try {
            String payloadJson = payload.toJson();
            log.debug(payloadJson);

            HttpResponse response = HttpHelper.httpClientPost(
                    url, new ArrayList<>(), payloadJson);

            result.setCode(response.getStatusCode());

            if (response.getStatusCode() != 201) {
                log.debug("Unable to Initialize SLA: " + response.toString());
                result.setOk(false);
                return result;
            }
        } catch (Exception e) {
            log.error("Exception Initializing SLA: " + e.getMessage());
            result.setOk(false);
            return result;
        }

        result.setOk(true);
        return result;
    }


    /**
     * Calls a Brizo´s endpoint to download an asset
     *
     * @param serviceEndpoint    the service endpoint
     * @param consumerAddress    the address of the consumer
     * @param serviceAgreementId the serviceAgreement Id
     * @param url                the url
     * @param destinationPath    the path to download the resource
     * @return DownloadResult Instance of DownloadResult that indicates if the download was correct
     * @throws IOException        IOException
     * @throws URISyntaxException URISyntaxException
     */
    public static DownloadResult consumeUrl(String serviceEndpoint, String consumerAddress, String serviceAgreementId, String url, String destinationPath) throws IOException, URISyntaxException {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Service.CONSUMER_ADDRESS_PARAM, consumerAddress);
        parameters.put(Service.SERVICE_AGREEMENT_PARAM, serviceAgreementId);
        parameters.put(Service.URL_PARAM, url);

        String endpoint = StringsHelper.format(serviceEndpoint, parameters);

        log.debug("Consuming URL[" + url + "]: for service Agreement " + serviceAgreementId);

        return HttpHelper.downloadResource(endpoint, destinationPath);

    }

    /**
     * Calls a Brizo´s endpoint to download an asset
     *
     * @param serviceEndpoint    the service endpoint
     * @param consumerAddress    the address of the consumer
     * @param serviceAgreementId the serviceAgreement Id
     * @param url                the url
     * @param destinationPath    the path to download the resource
     * @throws IOException Exception during the download process
     */
    public static void downloadUrl(String serviceEndpoint, String consumerAddress, String serviceAgreementId, String url, String destinationPath) throws IOException {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Service.CONSUMER_ADDRESS_PARAM, consumerAddress);
        parameters.put(Service.SERVICE_AGREEMENT_PARAM, serviceAgreementId);
        parameters.put(Service.URL_PARAM, url);

        String endpoint = StringsHelper.format(serviceEndpoint, parameters);

        log.debug("Consuming URL[" + url + "]: for service Agreement " + serviceAgreementId);

        HttpHelper.download(endpoint, destinationPath);

    }

    /**
     * Calls a Brizo´s endpoint to download an asset
     * @param serviceEndpoint the service endpoint
     * @param consumerAddress the address of the consumer
     * @param serviceAgreementId the serviceAgreement Id
     * @param url the url
     * @return an InputStream that represents the binary content
     * @throws IOException Exception during the download process
     */
    public static InputStream downloadUrl(String serviceEndpoint, String consumerAddress, String serviceAgreementId, String url) throws IOException {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Service.CONSUMER_ADDRESS_PARAM, consumerAddress);
        parameters.put(Service.SERVICE_AGREEMENT_PARAM, serviceAgreementId);
        parameters.put(Service.URL_PARAM, url);

        String endpoint = StringsHelper.format(serviceEndpoint, parameters);

        log.debug("Consuming URL[" + url + "]: for service Agreement " + serviceAgreementId);

        return HttpHelper.download(endpoint);

    }

    /**
     * Calls a Brizo´s endpoint to download an asset
     * @param serviceEndpoint the service endpoint
     * @param consumerAddress the address of the consumer
     * @param serviceAgreementId the serviceAgreement Id
     * @param url the url
     * @return an InputStream that represents the binary content
     * @throws IOException Exception during the download process
     */
    public static InputStream downloadRangeUrl(String serviceEndpoint, String consumerAddress, String serviceAgreementId, String url,  Integer startRange, Integer endRange) throws IOException {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(Service.CONSUMER_ADDRESS_PARAM, consumerAddress);
        parameters.put(Service.SERVICE_AGREEMENT_PARAM, serviceAgreementId);
        parameters.put(Service.URL_PARAM, url);

        String endpoint = StringsHelper.format(serviceEndpoint, parameters);

        log.debug("Consuming URL[" + url + "]: for service Agreement " + serviceAgreementId);

        return HttpHelper.downloadRange(endpoint, startRange, endRange);

    }

}
