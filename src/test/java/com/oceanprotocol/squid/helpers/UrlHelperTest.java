/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.helpers;

import org.junit.Test;
import java.net.URISyntaxException;
import static org.junit.Assert.*;

public class UrlHelperTest {

    @Test
    public void getBaseUrl() throws URISyntaxException {
        String fullUrl= "http://localhost:5000/api/v1/provider";

        assertEquals("http://localhost:5000", UrlHelper.getBaseUrl(fullUrl));
    }

    @Test
    public void parseDDOUrl() {
        String did= "did:op:12345";
        String fullUrl= "http://localhost:5000/api/v1/provider/{did}";

        assertEquals("http://localhost:5000/api/v1/provider/did:op:12345",
                UrlHelper.parseDDOUrl(fullUrl, did));
    }
}