package com.bitmechanic.barrister;

import java.io.IOException;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.reflect.Array;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class JacksonRpcRequest implements RpcRequest {

    private ObjectMapper mapper;
    private JsonNode root;
    private String id;
    private MethodParser method;

    private JsonNode param;
    private boolean haveRead;

    private Iterator<JsonNode> params;

    public JacksonRpcRequest(byte[] json) throws IOException {
        mapper = new ObjectMapper();
        root   = this.mapper.readTree(json);

        if (root.has("id")) {
            id = root.get("id").asText();
        }

        if (root.has("method")) {
            method = new MethodParser(root.get("method").asText());
        }

        if (root.has("params")) {
            param = root.get("params");
            if (param.isArray()) {
                params = param.getElements();
                param = null;
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getFunc() {
        return method == null ? null : method.getFunc();
    }

    public String getIface() {
        return method == null ? null : method.getIface();
    }

    public boolean hasNextParam() {
        if (params != null)
            return params.hasNext();
        else if (param != null)
            return !haveRead;
        else
            return false;
    }

    public Object nextParam(Class t) throws IOException {
        if (!hasNextParam()) {
            throw new IllegalStateException("No more params");
        }

        if (params == null) {
            haveRead = true;
            return convert(param, t);
        }
        else {
            return convert(params.next(), t);
        }
    }

    private Object convert(JsonNode p, Class t) throws IOException {
        if (t.isArray()) {
            if (!p.isArray()) {
                throw new IOException("JSON node is not an array for req id: " + id);
            }

            Class arrType = t.getComponentType();
            ArrayList list = new ArrayList();
            for (JsonNode n : p) {
                list.add(convert(n, arrType));
            }

            //System.out.println("Returning arr: " + Arrays.toString(list.toArray()));
            return list.toArray((Object[])Array.newInstance(arrType, 0));
        }
        else {
            if (t == String.class) 
                return p.getTextValue();
            else if (t == Long.class || t == long.class)
                return p.asLong();
            else if (t == Double.class || t == double.class)
                return p.asDouble();
            else if (t == Boolean.class || t == boolean.class)
                return p.asBoolean();
            else
                return mapper.treeToValue(p, t);
        }
    }

}