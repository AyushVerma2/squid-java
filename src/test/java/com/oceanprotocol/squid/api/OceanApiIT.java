/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.api;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OceanApiIT {

    private static final Logger log = LogManager.getLogger(OceanApiIT.class);

    @Test
    public void buildAPIFromConfig() throws Exception {

        Config config = ConfigFactory.load();

        OceanAPI oceanAPI = OceanAPI.getInstance(config);
        assertNotNull(oceanAPI.getMainAccount());
        assertEquals(config.getString("account.main.address"), oceanAPI.getMainAccount().address);
        assertNotNull(oceanAPI.getAssetsAPI());
        assertNotNull(oceanAPI.getAccountsAPI());
        assertNotNull(oceanAPI.getSecretStoreAPI());

    }

}
