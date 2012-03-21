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
                throw err("array", p);
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
            String jt = type(p);
            if (t == String.class) {
                if (jt.equals("string"))
                    return p.getTextValue();
                else
                    throw err("string", p);
            }
            else if (t == Long.class || t == long.class) {
                if (jt.equals("long"))
                    return p.asLong();
                else
                    throw err("long", p);
            }
            else if (t == Double.class || t == double.class) {
                if (jt.equals("double") || jt.equals("long") || jt.equals("number"))
                    return p.asDouble();
                else
                    throw err("double", p);
            }
            else if (t == Boolean.class || t == boolean.class) {
                if (jt.equals("bool"))
                    return p.asBoolean();
                else
                    throw err("bool",  p);
            }
            else {
                if (jt.equals("object"))
                    return mapper.treeToValue(p, t);
                else
                    throw err(t.getName(), p);
            }
        }
    }

    private IOException err(String expected, JsonNode p) {
        return new IOException("Expected type: " + expected + " but '" + p.asText() +
                               "' is type: " + type(p));
    }

    private String type(JsonNode p) {
        if (p.isNull()) { return "null"; }
        else if (p.isArray()) { return "array"; }
        else if (p.isObject()) { return "object"; }
        else if (p.isTextual()) { return "string"; }
        else if (p.isBoolean()) { return "bool"; }
        else if (p.isInt() || p.isLong()) { return "long"; }
        else if (p.isFloatingPointNumber() || p.isDouble()) { return "double"; }
        else if (p.isNumber()) { return "number"; }
        else return "unknown";
    }

}