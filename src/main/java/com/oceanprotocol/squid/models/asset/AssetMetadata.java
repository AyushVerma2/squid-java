/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models.asset;


import com.fasterxml.jackson.annotation.*;
import com.oceanprotocol.squid.models.DID;
import com.oceanprotocol.squid.models.Metadata;
import org.web3j.crypto.Hash;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.oceanprotocol.squid.models.AbstractModel.DATE_PATTERN;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic=true)
public class AssetMetadata extends Metadata {

    public enum assetTypes {dataset, algorithm, container, workflow, other};

    @JsonProperty
    public DID did;

    @JsonProperty
    public Base base;

    @JsonProperty
    public Curation curation;

    @JsonProperty
    public Map<String, Object> additionalInformation= new HashMap<>();

    public AssetMetadata() {
    }

    public AssetMetadata(DID did)   {
        this.did= did;
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic=true)
    public static class Base {

        @JsonProperty
        public String name;

        @JsonProperty
        public String type;

        @JsonProperty
        public String description;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
        @JsonProperty
        public Date dateCreated;

        @JsonProperty
        public String size;

        @JsonProperty
        public String author;

        @JsonProperty
        public String license;

        @JsonProperty
        public String copyrightHolder;

        @JsonProperty
        public String encoding;

        @JsonProperty
        public String compression;

        @JsonProperty
        public String contentType;

        @JsonProperty
        public String workExample;

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        public ArrayList<File> files;

        @JsonProperty
        public String encryptedFiles=null;

        @JsonProperty
        public ArrayList<Link> links;

        @JsonProperty
        public String inLanguage;

        @JsonProperty
        public String tags;

        @JsonProperty
        public String price;

        @JsonProperty
        public String checksum;

        public Base() {}

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic=true)
    public static class Link {

        @JsonProperty
        public String name;

        @JsonProperty
        public String type;

        @JsonProperty
        public String url;

        public Link() {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic=true)
    public static class Curation {

        @JsonProperty
        public float rating;

        @JsonProperty
        public int numVotes;

        @JsonProperty
        public String schema;

        @JsonProperty
        public boolean isListed;

        public Curation() {}
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic=true)
    public static class File {

        @JsonProperty
        public String url;

        @JsonProperty
        public String checksum;

        @JsonProperty
        public String contentLength;

        public File() {}
    }

    public String generateMetadataChecksum(String did) {

        String concatFields = this.base.files.stream()
                .map( file -> file.checksum!=null?file.checksum:"")
                .collect(Collectors.joining(""))
                .concat(this.base.name)
                .concat(this.base.author)
                .concat(this.base.license)
                .concat(did);

        return Hash.sha3(concatFields);

    }



}