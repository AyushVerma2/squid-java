/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.external;

import com.oceanprotocol.squid.helpers.HttpHelper;
import com.oceanprotocol.squid.helpers.StringsHelper;
import com.oceanprotocol.squid.models.HttpResponse;
import com.oceanprotocol.squid.models.brizo.InitializeAccessSLA;
import com.oceanprotocol.squid.models.service.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.oceanprotocol.squid.helpers.HttpHelper.*;

/**
 * Service for Brizo's Integration
 */
public class BrizoService {

    private static final Logger log = LogManager.getLogger(BrizoService.class);


    /**
     * Calls a Brizo's endpoint to request the initialization of a new Service Agreement
     * @param url the url
     * @param payload the payload
     * @return a flag that indicates if Brizo initialized the Service Agreement correctly
     */
    public static boolean initializeAccessServiceAgreement(String url, InitializeAccessSLA payload)  {

        log.debug("Initializing SLA[" + payload.serviceAgreementId + "]: " + url);

        try {
            String payloadJson= payload.toJson();
            log.debug(payloadJson);

            HttpResponse response = HttpHelper.httpClientPost(
                    url, new ArrayList<>(), payloadJson);

            if (response.getStatusCode() != 201) {
                log.error("Unable to Initialize SLA: " + response.toString());
                return false;
            }
        } catch (Exception e)   {
            log.error("Exception Initializing SLA: " + e.getMessage());

            return false;
        }
        return true;
    }


    /**
     * Calls a Brizo´s endpoint to download an asset
     * @param serviceEndpoint the service endpoint
     * @param consumerAddress the address of the consumer
     * @param serviceAgreementId the serviceAgreement Id
     * @param url the url
     * @param destinationPath the path to download the resource
     * @return a DownloadResult object that indicates if the download was correct
     * @throws IOException IOException
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


}
