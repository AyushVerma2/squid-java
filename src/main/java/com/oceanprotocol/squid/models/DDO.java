/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.api.client.util.Base64;
import com.oceanprotocol.squid.exceptions.DIDFormatException;
import com.oceanprotocol.squid.exceptions.ServiceException;
import com.oceanprotocol.squid.models.asset.AssetMetadata;
import com.oceanprotocol.squid.models.service.AccessService;
import com.oceanprotocol.squid.models.service.AuthorizationService;
import com.oceanprotocol.squid.models.service.MetadataService;
import com.oceanprotocol.squid.models.service.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.oceanprotocol.squid.models.DDO.PublicKey.ETHEREUM_KEY_TYPE;

@JsonPropertyOrder(alphabetic=true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DDO extends AbstractModel implements FromJsonToModel {

    static final Logger log= LogManager.getLogger(DDO.class);

    private static final String DDO_PROOF_TYPE= "DDODataSHA3Signature";
    private static final String UUID_PROOF_TYPE= "DDOIntegritySignature";

    private static final String MODEL_CHARSET= "UTF-8";

    @JsonProperty("@context")
    public String context= "https://w3id.org/future-method/v1";

    @JsonProperty
    public String id;

    @JsonIgnore
    private DID did;

    @JsonProperty("publicKey")
    public List<PublicKey> publicKeys= new ArrayList<>();

    @JsonProperty
    public List<Authentication> authentication= new ArrayList<>();

    //@JsonProperty("service")
    @JsonIgnore
    public List<Service> services= new ArrayList<>();

    @JsonProperty
    public Proof proof;


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
    @JsonDeserialize(using = CustomDateDeserializer.class)
    public Date created;

    @JsonIgnore
    public AssetMetadata metadata= null;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
    @JsonProperty
    public Date updated;


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic=true)
    static class PublicKey {

        public static final String ETHEREUM_KEY_TYPE= "EthereumECDSAKey";

        @JsonProperty
        public String id;

        @JsonProperty
        public String type;

        @JsonProperty
        public String owner;

        @JsonProperty
        public String publicKeyPem;

        @JsonProperty
        public String publicKeyBase58;

        public PublicKey() {}

        public PublicKey(String id, String type, String owner)  {
            this.id= id;
            this.type= type;
            this.owner= owner;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic=true)
    static class Authentication {

        @JsonProperty
        public String type;

        @JsonProperty
        public String publicKey;

        public Authentication() {}
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic=true)
    static public class Proof {

        @JsonProperty
        public String type;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
        @JsonDeserialize(using = CustomDateDeserializer.class)
        public Date created;

        @JsonProperty
        public String creator;

        @JsonProperty
        public String signatureValue;

        public Proof() {}

        public Proof(String type, String creator, String signature) {
            this.type= type;
            this.creator= creator;
            this.signatureValue= signature;
            this.created= getDateNowFormatted();
        }

        public Proof(String type, String creator, byte[] signature) {
            this(type, creator, Base64.encodeBase64URLSafeString(signature));
        }
    }

    public DDO() throws DIDFormatException {
        this.did= generateDID();
        if (null == this.created)
            this.created= getDateNowFormatted();

        this.id= this.did.toString();
    }


    public DDO(DID did, MetadataService metadataService, String publicKey, String signature) throws DIDFormatException {

        this.did= did;
        this.id= did.toString();

        if (null == this.created)
              this.created= getDateNowFormatted();
        this.metadata= metadataService.metadata;
        this.services.add(metadataService);

        this.proof= new Proof(UUID_PROOF_TYPE, publicKey, signature);
        this.publicKeys.add( new DDO.PublicKey(this.id, ETHEREUM_KEY_TYPE, this.id));
    }

    @JsonSetter("id")
    public void didSetter(String id) throws DIDFormatException{
        this.id = id;
        this.did = new DID(id);
    }

    public DDO addService(Service service)  {
        service.serviceDefinitionId= String.valueOf(services.size());
        services.add(service);
        return this;
    }



    @JsonSetter("service")
    public void servicesSetter(ArrayList<LinkedHashMap> services) {

        try {
            for (LinkedHashMap service: services)   {
                if (service.containsKey("type")) {
                    if (service.get("type").equals(Service.serviceTypes.Metadata.toString()) && service.containsKey("metadata")) {
                        this.metadata = getMapperInstance().convertValue(service.get("metadata"), AssetMetadata.class);
                        this.services.add(getMapperInstance().convertValue(service, MetadataService.class));

                    } else if (service.get("type").equals(Service.serviceTypes.Access.toString())) {
                        this.services.add(getMapperInstance().convertValue(service, AccessService.class));
                    } else if (service.get("type").equals(Service.serviceTypes.Authorization.toString())) {
                        this.services.add(getMapperInstance().convertValue(service, AuthorizationService.class));
                    } else {
                        this.services.add(getMapperInstance().convertValue(service, Service.class));
                    }
                }

            }

        } catch (Exception ex)    {
            log.error("Unable to parse the DDO(services): " + services + ", Exception: " + ex.getMessage());
        }
    }


    @JsonGetter("service")
    public List<Service> servicesGetter()    {

        int counter= 0;
        for (Service service: services) {
            if (service.type != null)   {
                if (service.type.equals(Service.serviceTypes.Metadata.toString()) && this.metadata != null)  {
                    try {
                        ((MetadataService) service).metadata= this.metadata;
                        services.set(counter, service);
                    } catch (Exception e) {
                        log.error("Error getting metadata object");
                    }
                } else {
                    services.set(counter, service);
                }
                counter++;
            }
        }

        return this.services;
    }

    public static DID generateDID() throws DIDFormatException {
        DID did= DID.builder();
        log.debug("Id generated: " + did.toString());
        return did;
    }


    public DID getDid() {
        return did;
    }



    public AccessService getAccessService(String serviceDefinitionId) throws ServiceException {
        for (Service service: services) {
            if (service.serviceDefinitionId.equals(serviceDefinitionId) && service.type.equals(Service.serviceTypes.Access.toString())) {
                return (AccessService) service;
            }
        }
        throw new ServiceException("Access Service with serviceDefinitionId=" + serviceDefinitionId + " not found");
    }

    public AuthorizationService getAuthorizationService(String serviceDefinitionId) {
        for (Service service: services) {
            if (service.serviceDefinitionId.equals(serviceDefinitionId) && service.type.equals(Service.serviceTypes.Authorization.toString())) {
                return (AuthorizationService) service;
            }
        }
        return null;
    }

    public AuthorizationService getAuthorizationService() {
        for (Service service: services) {
            if (service.type.equals(Service.serviceTypes.Authorization.toString())) {
                return (AuthorizationService) service;
            }
        }

       return null;
    }

}