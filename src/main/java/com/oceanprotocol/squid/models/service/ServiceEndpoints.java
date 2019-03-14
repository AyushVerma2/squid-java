/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models.service;

public class ServiceEndpoints {

    private String accessEndpoint;
    private String purchaseEndpoint;
    private String metadataEndpoint;
    private String secretStoreEndpoint;

    public ServiceEndpoints() {
    }

    public ServiceEndpoints(String accessEndpoint, String purchaseEndpoint, String metadataEndpoint) {
        this.accessEndpoint = accessEndpoint;
        this.purchaseEndpoint = purchaseEndpoint;
        this.metadataEndpoint = metadataEndpoint;
    }

    public ServiceEndpoints(String accessEndpoint, String purchaseEndpoint, String metadataEndpoint, String secretStoreEndpoint) {
        this(accessEndpoint, purchaseEndpoint, metadataEndpoint);
        this.secretStoreEndpoint = secretStoreEndpoint;
    }

    public String getAccessEndpoint() {
        return accessEndpoint;
    }

    public ServiceEndpoints setAccessEndpoint(String accessEndpoint) {
        this.accessEndpoint = accessEndpoint;
        return this;
    }

    public String getPurchaseEndpoint() {
        return purchaseEndpoint;
    }

    public ServiceEndpoints setPurchaseEndpoint(String purchaseEndpoint) {
        this.purchaseEndpoint = purchaseEndpoint;
        return this;
    }

    public String getMetadataEndpoint() {
        return metadataEndpoint;
    }

    public ServiceEndpoints setMetadataEndpoint(String metadataEndpoint) {
        this.metadataEndpoint = metadataEndpoint;
        return this;
    }

    public String getSecretStoreEndpoint() {
        return secretStoreEndpoint;
    }

    public void setSecretStoreEndpoint(String secretStoreEndpoint) {
        this.secretStoreEndpoint = secretStoreEndpoint;
    }
}