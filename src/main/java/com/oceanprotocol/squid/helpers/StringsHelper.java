/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class StringsHelper {

    /**
     * Given a list of strings join all of them using quotes wrapping each item with quotes
     * @param listOfStrings list of the strings
     * @return output string
     */
    public static String wrapWithQuotesAndJoin(List<String> listOfStrings)   {
        return listOfStrings.isEmpty() ? "" : "\"" + String.join("\",\"", listOfStrings) + "\"";
    }

    /**
     * Given a string with joined items by comma, return a list of items. Each item will have replaced the double quoutes
     * @param joinedString the joined string
     * @return list of items
     */
    public static List<String> getStringsFromJoin(String joinedString) {

        return Stream.of(joinedString.split(","))
                .map(url -> url.replaceAll("\"", ""))
                .collect(toList());

    }


    /**
     * Given a String and a map of key values, search in the string the variables using the ${xxx} format
     * and replace by the correspondant value of the map
     * Example: given: xxx${key1}yyy and "key1","000" the output will be xxx000yyy
     * @param format input string with ${xxx} variables
     * @param values map with key values to replace in the string
     * @return output string with the variables replaced
     */
    public static String format(String format, Map<String, Object> values) {

        StringBuilder formatter = new StringBuilder(format);
        List<Object> valueList = new ArrayList<Object>();

        Matcher matcher = Pattern.compile("\\$\\{(\\w+)}").matcher(format);

        while (matcher.find()) {
            String key = matcher.group(1);

            String formatKey = String.format("${%s}", key);
            int index = formatter.indexOf(formatKey);

            if (index != -1) {
                formatter.replace(index, index + formatKey.length(), "%s");
                valueList.add(values.get(key));
            }
        }

        return String.format(formatter.toString(), valueList.toArray());
    }
}
