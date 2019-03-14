/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models.asset;

public class BasicAssetInfo {

    byte[] assetId;
    Integer price;

    public byte[] getAssetId() {
        return assetId;
    }

    public void setAssetId(byte[] assetId) {
        this.assetId = assetId;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

}
