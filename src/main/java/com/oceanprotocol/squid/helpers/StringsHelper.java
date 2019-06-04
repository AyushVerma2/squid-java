/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.helpers;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class StringsHelper {

    /**
     * Given a list of strings join all of them using quotes wrapping each item with quotes
     *
     * @param listOfStrings list of the strings
     * @return output string
     */
    public static String wrapWithQuotesAndJoin(List<String> listOfStrings) {
        return listOfStrings.isEmpty() ? "" : "\"" + String.join("\",\"", listOfStrings) + "\"";
    }

    /**
     * Given a string with joined items by comma, return a list of items. Each item will have replaced the double quoutes
     *
     * @param joinedString the joined string
     * @return list of items
     */
    public static List<String> getStringsFromJoin(String joinedString) {

        return Stream.of(joinedString.split(","))
                .map(url -> url.replaceAll("\"", ""))
                .collect(toList());

    }


    /**
     * Given a base url an the parameters form the final url.
     *
     * @param base input base url.
     * @param values map with key values to replace in the string
     * @return output string with the variables replaced
     */
    public static String formUrl(String base, Map<String, Object> values) {
        StringBuilder result = new StringBuilder();
        if (!values.isEmpty()) {
            base = base.concat("?");
            values.forEach((key, value) -> result.append((String) key + "=" + (String) value + "&"));
        }
        result.deleteCharAt(result.length() - 1);

        return base + result.toString();

    }
}
