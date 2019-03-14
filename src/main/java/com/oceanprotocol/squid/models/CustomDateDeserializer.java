/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.models;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.oceanprotocol.squid.models.AbstractModel.DATE_PATTERN;

public class CustomDateDeserializer extends StdDeserializer<Date> {

    private SimpleDateFormat formatter =
            new SimpleDateFormat(DATE_PATTERN);

    public CustomDateDeserializer() {
        this(null);
    }

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, RuntimeException {
        String date = jsonParser.getText();
        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public CustomDateDeserializer(Class<?> vc) {
        super(vc);
    }

}