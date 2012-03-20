package com.bitmechanic.barrister;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import java.io.InputStream;
import java.io.IOException;

public class JacksonSerializer implements Serializer
{

    public JacksonSerializer() {

    }

    public byte[] serialize(Object o) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.valueToTree(o);
        return node.toString().getBytes("utf-8");
    }
        
    public List<Map<String,Object>> readList(InputStream is) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(is, List.class);
    }

    public Map<String,Object> readMap(InputStream is) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(is, Map.class);
    }

}