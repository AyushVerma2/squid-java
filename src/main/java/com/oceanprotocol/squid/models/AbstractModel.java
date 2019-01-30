package com.oceanprotocol.squid.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractModel {

    private static ObjectMapper objectMapper= null;

    public static final String DATE_PATTERN= "yyyy-MM-dd'T'HH:mm:ss";
    protected static final DateFormat DATE_FORMAT= new SimpleDateFormat(DATE_PATTERN);

    public static ObjectMapper getMapperInstance()  {
        if (objectMapper == null) {

            objectMapper = new ObjectMapper();
            objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        }

        return objectMapper;
    }


    public static <T> ObjectReader getReaderInstance(Class<T> clazz)    {
        return getMapperInstance().reader(clazz);
    }

    public static <T> Object convertToModel(Class<T> clazz, String json) throws IOException {
        return getReaderInstance(clazz).readValue(json);
    }

    public static <T> T fromJSON(final TypeReference<T> type, final String json) throws Exception {
        return getMapperInstance().readValue(json, type);
    }

    public String toJson() throws JsonProcessingException {
        return getMapperInstance().writeValueAsString(this);
    }

    public String toJson(Object object) throws JsonProcessingException {
        return getMapperInstance().writeValueAsString(object);
    }

}