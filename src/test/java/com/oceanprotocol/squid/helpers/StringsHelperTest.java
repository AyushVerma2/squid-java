/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.helpers;

import org.junit.Test;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class StringsHelperTest {

    @Test
    public void wrapWithQuotesAndJoin() {
        List<String> listOfStrings = Arrays.asList("http://localhost:5000/1", "http://localhost:5000/2", "http://localhost:5000/3");
        String result= StringsHelper.wrapWithQuotesAndJoin(listOfStrings);

        assertEquals("\"http://localhost:5000/1\",\"http://localhost:5000/2\",\"http://localhost:5000/3\"",
                result);
    }

    @Test
    public void getStringsFromJoin() {
        String input= "http://url1\"\",http://url2";
        List<String> output= StringsHelper.getStringsFromJoin(input);

        assertEquals(2, output.size());
        assertEquals("http://url1", output.get(0));
    }

    @Test
    public void format() {
        Map<String, Object> params= new HashMap<>();
        params.put("pubKey", "v1");
        params.put("serviceId", "v2");
        params.put("url", "v3");

        String input= "http://xx.org/?pubKey=${pubKey}&serviceId=${serviceId}&url=${url}";

        String output= StringsHelper.format(input, params);

        assertEquals("http://xx.org/?pubKey=v1&serviceId=v2&url=v3", output);
    }
}