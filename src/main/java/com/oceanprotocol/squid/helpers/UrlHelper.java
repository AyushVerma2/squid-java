/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.helpers;

import java.net.URI;
import java.net.URISyntaxException;

public abstract class UrlHelper {

    private static final String DID_TOKEN= "\\{did\\}";

    public static String getBaseUrl(String fullUrl) throws URISyntaxException {
        URI uri = new URI(fullUrl);
        return uri.getScheme() + "://" + uri.getAuthority();
    }


    public static String parseDDOUrl(String template, String id)  {
        return template.replaceAll(DID_TOKEN, id);
    }
}
