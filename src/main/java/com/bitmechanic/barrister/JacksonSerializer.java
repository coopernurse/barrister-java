package com.bitmechanic.barrister;

import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

import java.util.ArrayList;

import java.util.List;

import org.codehaus.jackson.JsonNode;

import org.codehaus.jackson.JsonToken;

import java.io.IOException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonFactory;
import java.io.InputStream;

public class JacksonSerializer implements Serializer
{

    public JacksonSerializer() {

    }

    public byte[] serialize(Object o) {
        // TODO: Stub
        return null;
    }
        
    public List<Map<String,Object>> readList(InputStream is) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(is, List.class);
    }

}