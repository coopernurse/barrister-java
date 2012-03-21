package com.bitmechanic.barrister;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.ser.CustomSerializerFactory;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import org.codehaus.jackson.util.CharTypes;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.impl.JsonWriteContext;
import org.codehaus.jackson.JsonGenerationException;

public class JacksonSerializer implements Serializer
{

    private JsonFactory jsonFactory;

    public JacksonSerializer() {
        jsonFactory = new JsonFactory();
    }

    /*
    public byte[] serialize(Object o) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.valueToTree(o);
        return node.toString().getBytes("utf-8");
    }
        
    public Map<String,Object> readMap(InputStream is) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(is, Map.class);
    }
    */

    public List<Map<String,Object>> readList(InputStream is) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(is, List.class);
    }

    public RpcRequest readRequest(byte[] input) throws IOException {
        return new JacksonRpcRequest(input);
    }

    public byte[] writeResponse(RpcResponse resp) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode respNode = mapper.createObjectNode();
        respNode.put("jsonrpc", "2.0");

        String id = resp.getId();
        if (id != null) {
            respNode.put("id", id);
        }

        RpcException err = resp.getError();
        if (err != null) {
            ObjectNode errNode = mapper.createObjectNode();
            errNode.put("code", err.getCode());
            errNode.put("message", err.getMessage());
            if (err.getData() != null) {
                errNode.put("data", mapper.valueToTree(err.getData()));
            }
            respNode.put("error", errNode);
        }

        if (resp.getResult() != null) {
            respNode.put("result", mapper.valueToTree(resp.getResult()));
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JsonGenerator gen = jsonFactory.createJsonGenerator(bos);
        gen.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        mapper.writeTree(gen, respNode);
        gen.close();
        byte[] arr = bos.toByteArray();
        bos.close();
        return arr;
    }

}
